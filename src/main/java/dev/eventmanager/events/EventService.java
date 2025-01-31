package dev.eventmanager.events;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.locations.Location;
import dev.eventmanager.locations.LocationService;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserRole;
import dev.eventmanager.users.domain.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

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

    public Event updateEvent(
            Long id,
            EventUpdateRequestDto eventUpdateRequest
    ) {
        EventEntity eventEntity = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=%s not found"));
        Event event = mapperConfig.getMapper().map(eventEntity, Event.class);
        Location location = locationService.getLocationById(eventUpdateRequest.locationId());


        if (event.maxPlaces() > eventUpdateRequest.maxPlaces()) {
            throw new IllegalArgumentException("The maximum number of places is less than before");
        }
        if (location.capacity() < eventUpdateRequest.maxPlaces()) {
            throw new IllegalArgumentException("The maximum number of place is more than location capacity");
        }
        /*
        --------------------------------------------------------------------------
        НУЖНО БУДЕТ ДОДЕЛАТЬ ЭТО МЕСТО КОГДА БУДУТ ЗАРЕГИСТРИРОВАННЫЕ ПОЛЬЗОВАТЕЛИ, Т.Е. ПРОВЕРИТЬ
        МОЖЕТ ЛИ LOCATION.ID() ВМЕСТИТЬ УЖЕ ЗАРЕГИСТРИРОВАННЫХ ПОЛЬЗОВАТЕЛЕЙ.
        --------------------------------------------------------------------------
         */

        /**
         * Тут возможно и не нужен билдер! Нет, он точно не нужен! Просто передаем из eventUpdateRequest и всё.
         * ТАК ЧТО ЭТО НУЖНО УБРАТЬ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         */
        Event newEvent = EventUpdateBuilder.builder(event)
                .changeName(eventUpdateRequest.eventName())
                .changeCost(eventUpdateRequest.cost())
                .changeDuration(eventUpdateRequest.duration())
                .changeLocationId(eventUpdateRequest.locationId())
                .changeMaxPlaces(event.maxPlaces())
                .changeStartDate(eventUpdateRequest.startDate())
                .build();

        return mapperConfig.getMapper().map(eventRepository.updateEventByRequest(
                newEvent.id(),
                newEvent.name(),
                newEvent.maxPlaces(),
                newEvent.startDate(),
                newEvent.cost(),
                newEvent.duration(),
                newEvent.locationId()
                ), Event.class);
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("You are not logged in");
        }
        return userService.getUserByLogin(auth.getName());
    }
}