package dev.eventmanager.events.domain;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.events.api.dto.EventCreateRequestDto;
import dev.eventmanager.events.api.dto.EventSearchRequestDto;
import dev.eventmanager.events.api.dto.EventUpdateRequestDto;
import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import dev.eventmanager.events.db.EventEntity;
import dev.eventmanager.events.db.EventRepository;
import dev.eventmanager.kafka.service.KafkaEventMessageService;
import dev.eventmanager.locations.Location;
import dev.eventmanager.locations.LocationService;
import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.service.RetryableTaskService;
import dev.eventmanager.users.domain.AuthenticationUserService;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserRole;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final MapperConfig mapperConfig;
    private final AuthenticationUserService authenticationUserService;
    private final KafkaEventMessageService kafkaEventMessageService;
    private final RetryableTaskService retryableTaskService;

    public EventService(
            EventRepository eventRepository,
            LocationService locationService,
            MapperConfig mapperConfig,
            AuthenticationUserService authenticationUserService,
            KafkaEventMessageService kafkaEventMessageService, RetryableTaskService retryableTaskService) {

        this.eventRepository = eventRepository;
        this.locationService = locationService;
        this.mapperConfig = mapperConfig;
        this.authenticationUserService = authenticationUserService;
        this.kafkaEventMessageService = kafkaEventMessageService;
        this.retryableTaskService = retryableTaskService;
    }

    @Transactional
    public Event createEvent(EventCreateRequestDto eventCreateRequestDto) {
        User user = authenticationUserService.getAuthenticatedUser();
        if (!locationService.existsLocationById(eventCreateRequestDto.locationId())) {
            throw new EntityNotFoundException("Location with id=%s not found"
                    .formatted(eventCreateRequestDto.locationId()));
        }

        DateTimeWithZone dateTimeZone = parseToCustomOffsetDateTime(eventCreateRequestDto.date());

        EventEntity eventEntityForSave = new EventEntity(
                eventCreateRequestDto.name(),
                eventCreateRequestDto.maxPlaces(),
                dateTimeZone.getDateTimeWithoutOffset(),
                dateTimeZone.getZoneOffset(),
                eventCreateRequestDto.cost(),
                eventCreateRequestDto.duration(),
                eventCreateRequestDto.locationId(),
                EventStatus.WAIT_START.name(),
                user.id()
        );
        validateCorrectDateEvent(eventEntityForSave);

        EventEntity savedEventEntity = eventRepository.save(eventEntityForSave);

        Event savedEvent = mapperConfig.getMapper().map(savedEventEntity, Event.class);

        log.info("event created = {}", savedEvent);

        return savedEvent;
    }

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
    @Transactional
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

        Event eventBefore = mapperConfig.getMapper().map(foundEventEntity, Event.class);
        foundEventEntity.setStatus(EventStatus.CANCELLED.name());
        eventRepository.save(foundEventEntity);
        Event eventAfter = mapperConfig.getMapper().map(foundEventEntity, Event.class);

        EventChangerEvent eventChanger = kafkaEventMessageService.createEventMessageEvent(eventBefore, eventAfter, true);
        retryableTaskService.createRetryableTask(eventChanger, RetryableTaskType.SEND_CREATE_NOTIFICATION_REQUEST);

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

        if (eventEntity.getStatus().equals(EventStatus.STARTED.name())) {
            throw new IllegalArgumentException("Event with id=" + id
                    + " is already started. The event cannot be changed");
        }

        DateTimeWithZone dateTimeZone = parseToCustomOffsetDateTime(eventUpdateRequestDto.startDate());

        validateMaxPlaces(eventEntity, eventUpdateRequestDto);

        if ((eventUpdateRequestDto.maxPlaces() != null)
                && (eventUpdateRequestDto.maxPlaces() < eventEntity.getRegistrations().size())) {
            throw new IllegalArgumentException("registered users more than the maximum number at the event");
        }

        Event beforeUpdateEvent = mapperConfig.getMapper().map(eventEntity, Event.class);
        updateEventEntityFromDto(eventEntity, eventUpdateRequestDto, dateTimeZone);
        validateCorrectDateEvent(eventEntity);
        eventRepository.save(eventEntity);
        Event updatedEvent = mapperConfig.getMapper().map(eventEntity, Event.class);

        EventChangerEvent eventChanger = kafkaEventMessageService.createEventMessageEvent(beforeUpdateEvent, updatedEvent, true);
        retryableTaskService.createRetryableTask(eventChanger, RetryableTaskType.SEND_CREATE_NOTIFICATION_REQUEST);
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

    private void updateEventEntityFromDto(EventEntity eventEntity, EventUpdateRequestDto dto, DateTimeWithZone dateTimeZone) {
        Optional.ofNullable(dto.eventName()).ifPresent(eventEntity::setName);
        Optional.ofNullable(dto.maxPlaces()).ifPresent(eventEntity::setMaxPlaces);
        Optional.ofNullable(dateTimeZone).ifPresent((date) -> {
            eventEntity.setDate(date.getOffsetDateTime());
        });
        Optional.ofNullable(dto.cost())
                .map(cost -> cost.setScale(2, RoundingMode.HALF_UP))
                .ifPresent(eventEntity::setCost);
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

    private void validateCorrectDateEvent(EventEntity event) {
        var date = event.getDate();
        var dateUTC = date.withOffsetSameInstant(ZoneOffset.UTC);
        var offsetDateTimeNow = OffsetDateTime.now(ZoneOffset.UTC);

        if (dateUTC.isBefore(offsetDateTimeNow)) {
            throw new IllegalArgumentException("The event start date cannot be in the past");
        }

        var eventStartedBefore = eventRepository.findFirstByLocationIdAndDateBeforeOrderByDateDesc(event.getLocationId(), date);
        var findEventIdsBusyDate = eventRepository.findEventsWhereDateIsBusyWithoutId(
                event.getLocationId(),
                dateUTC,
                dateUTC.plusMinutes(event.getDuration()),
                event.getId()
        );

        eventStartedBefore.ifPresent(e -> {
            if (e.getDate().plusMinutes(e.getDuration()).isAfter(dateUTC)) {
                if (e.getStatus().equals(EventStatus.STARTED.name())) {
                    throw new IllegalArgumentException("For the time selected, an event is currently in progress");
                }
                throw new IllegalArgumentException("A certain event at location with id=" + event.getLocationId()
                        + " is already booked at this time");
            }
        });

        if (!findEventIdsBusyDate.isEmpty()) {
            throw new IllegalArgumentException("Some events have already been booked for the selected time");
        }
    }

    private DateTimeWithZone parseToCustomOffsetDateTime(String date) {
        if (date == null) {
            throw new IllegalArgumentException("The date for parsing cannot be null");
        }
        return new DateTimeWithZone(OffsetDateTime.parse(date));
    }
}