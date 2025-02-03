package dev.eventmanager.events.domain;

public record RegistrationEventUser (
        Long id,
        Long userId,
        Long eventId
){
}
