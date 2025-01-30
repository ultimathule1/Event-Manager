package dev.eventmanager.events;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.locations.LocationService;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserService;
import jakarta.persistence.EntityNotFoundException;
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
        Authentication userFromContext = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByLogin(userFromContext.getName());
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
}