package dev.eventmanager.events;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.locations.LocationService;
import dev.eventmanager.security.jwt.JwtTokenManager;
import dev.eventmanager.users.db.UserEntity;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final UserService userService;
    private final JwtTokenManager jwtTokenManager;
    private final MapperConfig mapperConfig;
    private final Logger logger = LoggerFactory.getLogger(EventService.class);

    public EventService(EventRepository eventRepository, LocationService locationService, UserService userService, JwtTokenManager jwtTokenManager, MapperConfig mapperConfig) {
        this.eventRepository = eventRepository;
        this.locationService = locationService;
        this.userService = userService;
        this.jwtTokenManager = jwtTokenManager;
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
}
