package dev.eventmanager.events.domain;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.events.api.EventCreateRequestDto;
import dev.eventmanager.events.api.EventSearchRequestDto;
import dev.eventmanager.events.api.EventUpdateRequestDto;
import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import dev.eventmanager.events.api.kafka.event.FieldChange;
import dev.eventmanager.events.db.EventEntity;
import dev.eventmanager.events.db.EventRepository;
import dev.eventmanager.events.registration.RegistrationRepository;
import dev.eventmanager.locations.Location;
import dev.eventmanager.locations.LocationService;
import dev.eventmanager.users.domain.AuthenticationUserService;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserRole;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final MapperConfig mapperConfig;
    private final AuthenticationUserService authenticationUserService;
    private final KafkaTemplate<Long, EventChangerEvent> kafkaTemplate;
    private final String eventsTopicName;
    private final RegistrationRepository registrationRepository;

    public EventService(
            EventRepository eventRepository,
            LocationService locationService,
            MapperConfig mapperConfig,
            AuthenticationUserService authenticationUserService,
            KafkaTemplate<Long, EventChangerEvent> kafkaTemplate,
            @Value("${events.notifications.topic.name}") String eventsTopicName,
            RegistrationRepository registrationRepository) {

        this.eventRepository = eventRepository;
        this.locationService = locationService;
        this.mapperConfig = mapperConfig;
        this.authenticationUserService = authenticationUserService;
        this.kafkaTemplate = kafkaTemplate;
        this.eventsTopicName = eventsTopicName;
        this.registrationRepository = registrationRepository;
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

        String statusBefore = foundEventEntity.getStatus();
        foundEventEntity.setStatus(EventStatus.CANCELLED.name());
        eventRepository.save(foundEventEntity);

        Event cancelledEvent = mapperConfig.getMapper().map(foundEventEntity, Event.class);

        EventChangerEvent changerEvent = createMessageForKafka(cancelledEvent);
        changerEvent.setFieldStatus(new FieldChange<>(statusBefore,cancelledEvent.status()));

        sendKafkaMessage(eventsTopicName, changerEvent);

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

        Event beforeUpdateEvent = mapperConfig.getMapper().map(eventEntity, Event.class);
        updateEventEntityFromDto(eventEntity, eventUpdateRequestDto);
        eventRepository.save(eventEntity);
        Event updatedEvent = mapperConfig.getMapper().map(eventEntity, Event.class);

        EventChangerEvent eventChangerEvent = createMessageForKafka(beforeUpdateEvent,updatedEvent);
        sendKafkaMessage(eventsTopicName, eventChangerEvent);

        log.info("event updated = {}", updatedEvent);

        return updatedEvent;
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

    private EventChangerEvent createMessageForKafka(Event event) {
        User currentUser = authenticationUserService.getAuthenticatedUser();
        Long changedUserId = null;
        if (currentUser != null) {
            changedUserId = currentUser.id();
        }

        EventChangerEvent changerEvent = new EventChangerEvent();
        changerEvent.setEventId(event.id());
        changerEvent.setOwnerEventId(event.ownerId());
        changerEvent.setChangedEventByUserId(changedUserId);
        changerEvent.setEventSubscribers(registrationRepository.getAllUsersIdWhereEventIdEquals(event.id()));

        return changerEvent;
    }

    private EventChangerEvent createMessageForKafka(Event eventBefore, Event eventAfter) {
        EventChangerEvent changerEvent = createMessageForKafka(eventBefore);

        updateField(eventBefore.startDate(), eventAfter.startDate(), changerEvent::setFieldEventDate);
        updateField(eventBefore.duration(), eventAfter.duration(), changerEvent::setFieldDuration);
        updateField(eventBefore.locationId(), eventAfter.locationId(), changerEvent::setFieldLocationId);
        updateField(eventBefore.name(), eventAfter.name(), changerEvent::setFieldEventName);
        updateField(eventBefore.cost(), eventAfter.cost(), changerEvent::setFieldEventCost);
        updateField(eventBefore.maxPlaces(), eventAfter.maxPlaces(), changerEvent::setFieldMaxPlaces);
        updateField(eventBefore.status(), eventAfter.status(), changerEvent::setFieldStatus);

        return changerEvent;
    }

    private <T> void updateField(T before, T after, Consumer<FieldChange<T>> setter) {
        if (!Objects.equals(before, after)) {
            setter.accept(new FieldChange<>(before, after));
        }
    }

    private void sendKafkaMessage(String eventsTopicName, EventChangerEvent changerEvent) {
        ProducerRecord<Long, EventChangerEvent> record = new ProducerRecord<>(
                eventsTopicName,
                changerEvent.getEventId() + changerEvent.getOwnerEventId(),
                changerEvent
        );

        record.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        CompletableFuture<SendResult<Long, EventChangerEvent>> future = kafkaTemplate.send(record);
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Failed to send message: {}", exception.getMessage());
            } else {
                log.info("Message sent successfully: {}", result.getRecordMetadata());
            }
        });
    }
}