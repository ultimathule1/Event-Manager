package dev.eventmanager.events.registration;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.events.api.EventDto;
import dev.eventmanager.events.db.EventEntity;
import dev.eventmanager.events.db.EventRepository;
import dev.eventmanager.events.domain.Event;
import dev.eventmanager.events.domain.EventService;
import dev.eventmanager.events.domain.EventStatus;
import dev.eventmanager.users.domain.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final MapperConfig mapperConfig;

    public RegistrationService(
            RegistrationRepository registrationRepository,
            EventService eventService,
            EventRepository eventRepository, MapperConfig mapperConfig) {
        this.registrationRepository = registrationRepository;
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.mapperConfig = mapperConfig;
    }

    @Transactional
    public void registerCurrentUserForEvent(User currentUser, Long eventId) {

        Event event = eventService.getEventById(eventId);

        registrationRepository.findByEventIdAndUserId(eventId, currentUser.id()).ifPresent(registration -> {
            if (registration.getUserId().equals(currentUser.id()))
                throw new EntityNotFoundException("The user has already registered for this event");
        });

        if (currentUser.id().equals(event.ownerId())) {
            throw new IllegalArgumentException("The event creator cannot register for this event");
        }

        if (event.status().equals(EventStatus.CANCELLED.name())
                || event.status().equals(EventStatus.FINISHED.name())) {
            throw new IllegalArgumentException("Event with id=%s already finished or cancelled".formatted(eventId));
        }

        registrationRepository.save(
                new RegistrationUserEventEntity(
                        null,
                        currentUser.id(),
                        eventRepository.findById(eventId)
                                .orElseThrow(() -> new EntityNotFoundException("Could not find event with id: " + eventId))
                )
        );
    }

    @Transactional
    public void cancelUserRegistration(User currentUser, Long eventId) {
        RegistrationUserEventEntity registrationEntity =
                registrationRepository.findByEventIdAndUserId(eventId, currentUser.id())
                        .orElseThrow(() -> new EntityNotFoundException("No registrations were found for this event"));

        if (!registrationEntity.getUserId().equals(currentUser.id())) {
            throw new IllegalArgumentException("The current user is not registered for this event");
        }

        if (registrationEntity.getEvent().getStatus().equals(EventStatus.FINISHED.name())
                || registrationEntity.getEvent().getStatus().equals(EventStatus.STARTED.name())) {
            throw new IllegalArgumentException(("The current user cannot cancel registration " +
                    "because the event has either already started or has already been completed"));
        }

        registrationRepository.deleteById(registrationEntity.getId());
    }

    public List<Event> getAllEventWhereUserRegistered(Long userId) {
        List<EventEntity> eventsEntity =
                registrationRepository.findAllEventsWhereUserRegistered(userId);

        return eventsEntity
                .stream()
                .map(e -> mapperConfig.getMapper().map(e, Event.class))
                .toList();
    }
}
