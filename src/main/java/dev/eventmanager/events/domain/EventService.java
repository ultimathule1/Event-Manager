package dev.eventmanager.events.domain;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.events.api.EventCreateRequestDto;
import dev.eventmanager.events.api.EventSearchRequestDto;
import dev.eventmanager.events.api.EventUpdateRequestDto;
import dev.eventmanager.events.api.kafka.ChangedEventFields;
import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import dev.eventmanager.events.db.EventEntity;
import dev.eventmanager.events.db.EventRepository;
import dev.eventmanager.locations.Location;
import dev.eventmanager.locations.LocationService;
import dev.eventmanager.users.domain.AuthenticationUserService;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserRole;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final MapperConfig mapperConfig;
    private final AuthenticationUserService authenticationUserService;
    private final KafkaTemplate<Long, EventChangerEvent> kafkaTemplate;
    private final String eventsTopicName;

    public EventService(
            EventRepository eventRepository,
            LocationService locationService,
            MapperConfig mapperConfig,
            AuthenticationUserService authenticationUserService,
            KafkaTemplate<Long, EventChangerEvent> kafkaTemplate,
            @Value("${events.notifications.topic.name}") String eventsTopicName) {

        this.eventRepository = eventRepository;
        this.locationService = locationService;
        this.mapperConfig = mapperConfig;
        this.authenticationUserService = authenticationUserService;
        this.kafkaTemplate = kafkaTemplate;
        this.eventsTopicName = eventsTopicName;
    }

    public Event createEvent(EventCreateRequestDto eventCreateRequestDto) {
        User user = authenticationUserService.getAuthenticatedUser();
        if (!locationService.existsLocationById(eventCreateRequestDto.locationId())) {
            throw new EntityNotFoundException("Location with this id=%s not found"
                    .formatted(eventCreateRequestDto.locationId()));
        }

        EventEntity savedEventEntity = eventRepository.save(new EventEntity(
                eventCreateRequestDto.name(),
                eventCreateRequestDto.maxPlaces(),
                eventCreateRequestDto.date(),
                eventCreateRequestDto.cost(),
                eventCreateRequestDto.duration(),
                eventCreateRequestDto.locationId(),
                EventStatus.WAIT_START.name(),
                user.id()
        ));

        Event savedEvent = mapperConfig.getMapper().map(savedEventEntity, Event.class);

        EventChangerEvent event = new EventChangerEvent();
        event.setChangedEventByUserId(user.id());
        event.setEventId(savedEvent.id());
        event.setOwnerEventId(savedEvent.ownerId());
        event.setEventSubscribers(new ArrayList<>());

        ChangedEventFields changedEventFields = new ChangedEventFields();
        HashMap<String, String> mapEventName = new HashMap<>();
        mapEventName.put(savedEvent.name(), null);
        changedEventFields.setEventName(mapEventName);

        HashMap<Integer, Integer> mapMaxPlaces = new HashMap<>();
        mapMaxPlaces.put(savedEvent.maxPlaces(), null);
        changedEventFields.setMaxPlaces(mapMaxPlaces);

        HashMap<Integer, Integer> mapDuration = new HashMap<>();
        mapDuration.put(savedEvent.duration(), null);
        changedEventFields.setDuration(mapDuration);

        HashMap<Long, Long> mapLocationId = new HashMap<>();
        mapLocationId.put(savedEvent.locationId(), null);
        changedEventFields.setLocationId(mapLocationId);

        HashMap<OffsetDateTime, OffsetDateTime> mapDateTime = new HashMap<>();
        mapDateTime.put(savedEvent.startDate(), null);
        changedEventFields.setStartTime(mapDateTime);

        HashMap<BigDecimal, BigDecimal> mapCost = new HashMap<>();
        mapCost.put(savedEvent.cost(), null);
        changedEventFields.setCost(mapCost);

        event.setChangedEventFields(changedEventFields);

        kafkaTemplate.send(eventsTopicName, event);

        log.info("event created = {}", savedEvent);

        return savedEvent;
    }

    @Transactional
    public Event getEventById(Long id) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=%s not found".formatted(id)));
        return mapperConfig
                .getMapper()
                .map(event, Event.class);
    }

    /**
     * There is a soft removal of the event.
     * The event is not deleted from the database, but goes only into the mode of canceled
     * Can be deleted either an admin or the creator of the event.
     */
    public void cancelEvent(Long eventId) {
        User currentUser = authenticationUserService.getAuthenticatedUser();

        EventEntity foundEventEntity = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=%s not found"));

        if (!(currentUser.role() == UserRole.ADMIN) && !(foundEventEntity.getOwnerId().equals(currentUser.id()))) {
            throw new AuthorizationDeniedException("You do not have permission to delete this event");
        }

        if (foundEventEntity.getStatus().equals(EventStatus.CANCELLED.name())) {
            throw new IllegalArgumentException("Event with id=%s is already cancelled".formatted(eventId));
        }
        if (!foundEventEntity.getStatus().equals(EventStatus.WAIT_START.name())) {
            throw new IllegalArgumentException("Event with id=%s already cannot be cancelled");
        }

        foundEventEntity.setStatus(EventStatus.CANCELLED.name());
        eventRepository.save(foundEventEntity);

        log.info("event cancelled = {}", foundEventEntity);
    }

    @Transactional
    public Event updateEvent(
            Long id,
            EventUpdateRequestDto eventUpdateRequestDto
    ) {
        EventEntity eventEntity = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=%s not found"));

        User currentUser = authenticationUserService.getAuthenticatedUser();
        if (currentUser.role() != UserRole.ADMIN && !currentUser.id().equals(eventEntity.getOwnerId())) {
            throw new AuthorizationDeniedException("You do not have permission to update this event");
        }

        validateMaxPlaces(eventEntity, eventUpdateRequestDto);

        if ((eventUpdateRequestDto.maxPlaces() != null)
                && (eventUpdateRequestDto.maxPlaces() < eventEntity.getRegistrations().size())) {
            throw new IllegalArgumentException("registered users more than the maximum number at the event");
        }

        updateEventEntityFromDto(eventEntity, eventUpdateRequestDto);
        eventRepository.save(eventEntity);

        log.info("event updated = {}", eventEntity);

        return mapperConfig.getMapper().map(eventEntity, Event.class);
    }

    public List<Event> getEventsCreatedByCurrentUser() {
        User currentUser = authenticationUserService.getAuthenticatedUser();
        return eventRepository.findAllByOwnerId(currentUser.id())
                .stream()
                .map(e -> mapperConfig.getMapper().map(e, Event.class))
                .toList();
    }

    public List<Event> searchEvents(EventSearchRequestDto eventSearchRequestDto) {
        List<EventEntity> eventsList = eventRepository.searchEvents(
                eventSearchRequestDto.name(),
                eventSearchRequestDto.placesMin(),
                eventSearchRequestDto.placesMax(),
                eventSearchRequestDto.dateStartBefore(),
                eventSearchRequestDto.dateStartAfter(),
                eventSearchRequestDto.costMin(),
                eventSearchRequestDto.costMax(),
                eventSearchRequestDto.durationMin(),
                eventSearchRequestDto.durationMax(),
                eventSearchRequestDto.locationId(),
                eventSearchRequestDto.eventStatus()
        );

        return eventsList.stream()
                .map(e -> mapperConfig.getMapper().map(e, Event.class))
                .toList();
    }

    private void updateEventEntityFromDto(EventEntity eventEntity, EventUpdateRequestDto dto) {
        Optional.ofNullable(dto.eventName()).ifPresent(eventEntity::setName);
        Optional.ofNullable(dto.maxPlaces()).ifPresent(eventEntity::setMaxPlaces);
        Optional.ofNullable(dto.startDate()).ifPresent(eventEntity::setDate);
        Optional.ofNullable(dto.cost()).ifPresent(eventEntity::setCost);
        Optional.ofNullable(dto.duration()).ifPresent(eventEntity::setDuration);
        Optional.ofNullable(dto.locationId()).ifPresent(eventEntity::setLocationId);
    }

    private void validateMaxPlaces(EventEntity eventEntity, EventUpdateRequestDto dto) {
        if (dto.maxPlaces() == null) {
            return;
        }

        if (eventEntity.getMaxPlaces() > dto.maxPlaces()) {
            throw new IllegalArgumentException("The maximum number of places cannot be reduced");
        }

        Long locationId = dto.locationId() == null ? eventEntity.getLocationId() : dto.locationId();
        if (locationId == null) return;

        if (locationService.existsLocationById(locationId)) {
            Location location = locationService.getLocationById(locationId);
            if (location.capacity() != null && location.capacity() < dto.maxPlaces()) {
                throw new IllegalArgumentException("The maximum number of event is more than location capacity");
            }
        }
    }
}