package dev.eventmanager.events;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.locations.LocationService;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserRole;
import dev.eventmanager.users.domain.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
     * @param eventId
     */
    public void deleteEvent(Long eventId) {
        User currentUser = getAuthenticatedUser();

        EventEntity foundEventEntity = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=%s not found"));
        Event foundEvent = mapperConfig.getMapper().map(foundEventEntity, Event.class);
        //Здесь ошибка
        if (!(currentUser.role() == UserRole.ADMIN) || !(foundEvent.ownerId().equals(currentUser.id()))) {
            throw new AuthorizationDeniedException("You do not have permission to delete this event");
        }

        if (foundEvent.status().equals(EventStatus.CANCELLED.name())) {
            throw new IllegalArgumentException("Event with id=%s is already cancelled");
        }
        if (!foundEvent.status().equals(EventStatus.WAIT_START.name())) {
            throw new IllegalArgumentException("Event with id=%s already cannot be cancelled");
        }
        //-----------------------------------
        // Возможно сделать через Builder
        //-----------------------------------
        Event updatedEvent = new Event(
                foundEvent.id(),
                foundEvent.name(),
                foundEvent.occupiedPlaces(),
                foundEvent.startDate(),
                foundEvent.duration(),
                foundEvent.cost(),
                foundEvent.ownerId(),
                foundEvent.locationId(),
                EventStatus.CANCELLED.name(),
                foundEvent.maxPlaces()
        );

        eventRepository.save(mapperConfig.getMapper().map(updatedEvent, EventEntity.class));
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("You are not logged in");
        }
        return userService.getUserByLogin(auth.getName());
    }
}