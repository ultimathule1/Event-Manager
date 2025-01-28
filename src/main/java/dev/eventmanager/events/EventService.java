package dev.eventmanager.events;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.locations.LocationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final MapperConfig mapperConfig;

    public EventService(EventRepository eventRepository, LocationService locationService, MapperConfig mapperConfig) {
        this.eventRepository = eventRepository;
        this.locationService = locationService;
        this.mapperConfig = mapperConfig;
    }

    public Event createEvent(EventCreateRequestDto eventCreateRequestDto) {
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
                EventStatus.WAIT_START.name()
        ));

        return mapperConfig.getMapper().map(savedEventEntity, Event.class);
    }
}
