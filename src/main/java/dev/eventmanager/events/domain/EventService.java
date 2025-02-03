package dev.eventmanager.events.domain;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.events.api.EventCreateRequestDto;
import dev.eventmanager.events.api.EventSearchRequestDto;
import dev.eventmanager.events.api.EventUpdateRequest;
import dev.eventmanager.events.db.EventEntity;
import dev.eventmanager.events.db.EventRepository;
import dev.eventmanager.locations.Location;
import dev.eventmanager.locations.LocationService;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserRole;
import dev.eventmanager.users.domain.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final UserService userService;
    private final MapperConfig mapperConfig;

    public EventService(
            EventRepository eventRepository,
            LocationService locationService,
            UserService userService,
            MapperConfig mapperConfig) {

        this.eventRepository = eventRepository;
        this.locationService = locationService;
        this.userService = userService;
        this.mapperConfig = mapperConfig;
    }

    public Event createEvent(EventCreateRequestDto eventCreateRequestDto) {
        User user = getAuthenticatedUser();
        if (!locationService.existsLocationById(eventCreateRequestDto.locationId())) {
            throw new EntityNotFoundException("Location with this id=%s not found"
                    .formatted(eventCreateRequestDto.locationId()));
        }

        EventEntity savedEventEntity = eventRepository.save(new EventEntity(
                null,
                eventCreateRequestDto.name(),
                eventCreateRequestDto.maxPlaces(),
                0,
                eventCreateRequestDto.date(),
                eventCreateRequestDto.cost(),
                eventCreateRequestDto.duration(),
                eventCreateRequestDto.locationId(),
                EventStatus.WAIT_START.name(),
                user.id()
        ));

        return mapperConfig.getMapper().map(savedEventEntity, Event.class);
    }

    public Event getEventById(Long id) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=%s not found"));

        return mapperConfig
                .getMapper()
                .map(event, Event.class);
    }

    /**
     * There is a soft removal of the event.
     * The event is not deleted from the database, but goes only into the mode of canceled
     * Can be deleted either an admin or the creator of the event.
     *
     * @param eventId
     */
    public void deleteEvent(Long eventId) {
        User currentUser = getAuthenticatedUser();

        EventEntity foundEventEntity = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=%s not found"));

        if (!(currentUser.role() == UserRole.ADMIN) && !(foundEventEntity.getOwnerId().equals(currentUser.id()))) {
            throw new AuthorizationDeniedException("You do not have permission to delete this event");
        }

        if (foundEventEntity.getStatus().equals(EventStatus.CANCELLED.name())) {
            throw new IllegalArgumentException("Event with id=%s is already cancelled");
        }
        if (!foundEventEntity.getStatus().equals(EventStatus.WAIT_START.name())) {
            throw new IllegalArgumentException("Event with id=%s already cannot be cancelled");
        }

        foundEventEntity.setStatus(EventStatus.CANCELLED.name());
        eventRepository.save(foundEventEntity);
    }

    @Transactional
    public Event updateEvent(
            Long id,
            EventUpdateRequest eventUpdateRequest
    ) {
        EventEntity eventEntity = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=%s not found"));

        User currentUser = getAuthenticatedUser();
        if (currentUser.role() != UserRole.ADMIN && !currentUser.id().equals(eventEntity.getOwnerId())) {
            throw new AuthorizationDeniedException("You do not have permission to update this event");
        }

        validateMaxPlaces(eventEntity, eventUpdateRequest);
        /*
        --------------------------------------------------------------------------
        НУЖНО БУДЕТ ДОДЕЛАТЬ ЭТО МЕСТО КОГДА БУДУТ ЗАРЕГИСТРИРОВАННЫЕ ПОЛЬЗОВАТЕЛИ, Т.Е. ПРОВЕРИТЬ
        МОЖЕТ ЛИ LOCATION.ID() ВМЕСТИТЬ УЖЕ ЗАРЕГИСТРИРОВАННЫХ ПОЛЬЗОВАТЕЛЕЙ.
        --------------------------------------------------------------------------
         */

        updateEventEntityFromDto(eventEntity, eventUpdateRequest);
        eventRepository.save(eventEntity);
        return mapperConfig.getMapper().map(eventEntity, Event.class);
    }

    public List<Event> getEventsCreatedByCurrentUser() {
        User currentUser = getAuthenticatedUser();
        return eventRepository.findAllByOwnerId(currentUser.id())
                .stream()
                .map(e -> mapperConfig.getMapper().map(e, Event.class))
                .toList();
    }

    public List<Event> searchEvents(EventSearchRequestDto eventSearchRequestDto) {
        List<EventEntity> eventsList = eventRepository.searchEvents(
                eventSearchRequestDto.getName(),
                eventSearchRequestDto.getPlacesMin(),
                eventSearchRequestDto.getPlacesMax(),
                eventSearchRequestDto.getDateStartBefore(),
                eventSearchRequestDto.getDateStartAfter(),
                eventSearchRequestDto.getCostMin(),
                eventSearchRequestDto.getCostMax(),
                eventSearchRequestDto.getDurationMin(),
                eventSearchRequestDto.getDurationMax(),
                eventSearchRequestDto.getLocationId(),
                eventSearchRequestDto.getEventStatus()
        );

        return eventsList.stream()
                .map(e -> mapperConfig.getMapper().map(e, Event.class))
                .toList();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("You are not logged in");
        }
        return userService.getUserByLogin(auth.getName());
    }

    private void updateEventEntityFromDto(EventEntity eventEntity, EventUpdateRequest dto) {
        Optional.ofNullable(dto.eventName()).ifPresent(eventEntity::setName);
        Optional.ofNullable(dto.maxPlaces()).ifPresent(eventEntity::setMaxPlaces);
        Optional.ofNullable(dto.startDate()).ifPresent(eventEntity::setStartDate);
        Optional.ofNullable(dto.cost()).ifPresent(eventEntity::setCost);
        Optional.ofNullable(dto.duration()).ifPresent(eventEntity::setDuration);
        Optional.ofNullable(dto.locationId()).ifPresent(eventEntity::setLocationId);
    }

    private void validateMaxPlaces(EventEntity eventEntity, EventUpdateRequest dto) {
        if (dto.maxPlaces() == null) {
            return;
        }

        if (eventEntity.getMaxPlaces() > dto.maxPlaces()) {
            throw new IllegalArgumentException("The maximum number of places cannot be reduced");
        }

        Long locationId = dto.locationId() == null ? eventEntity.getLocationId() : dto.locationId();
        if (locationId == null) return;

        try {
            Location location = locationService.getLocationById(dto.locationId());
            if (location.capacity() != null && location.capacity() < dto.maxPlaces()) {
                throw new IllegalArgumentException("The maximum number of event is more than location capacity");
            }
        } catch (EntityNotFoundException ignored) {
        }
    }
}