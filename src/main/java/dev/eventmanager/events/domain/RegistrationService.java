package dev.eventmanager.events.domain;

import dev.eventmanager.events.db.EventRepository;
import dev.eventmanager.events.db.RegistrationEventUserEntity;
import dev.eventmanager.events.db.RegistrationRepository;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final UserService userService;
    private final EventRepository eventRepository;

    public RegistrationService(RegistrationRepository registrationRepository, EventService eventService, UserService userService, EventRepository eventRepository1) {
        this.registrationRepository = registrationRepository;
        this.eventService = eventService;
        this.userService = userService;
        this.eventRepository = eventRepository1;
    }

    @Transactional
    public void registerUserForEvent(Long eventId) {
        User currentUser = getAuthenticatedUser();

        Event event = eventService.getEventById(eventId);

        registrationRepository.findByEventId(eventId).ifPresent(registration -> {
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
                new RegistrationEventUserEntity(
                        null,
                        currentUser.id(),
                        eventRepository.findById(eventId)
                                .orElseThrow(() -> new EntityNotFoundException("Could not find event with id: " + eventId))
                )
        );
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("You are not logged in");
        }
        return userService.getUserByLogin(auth.getName());
    }
}
