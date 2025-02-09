package dev.eventmanager.events.registration;

public record RegistrationUserEvent(
        Long id,
        Long userId,
        Long eventId
) {
}
