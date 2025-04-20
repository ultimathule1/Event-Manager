package dev.eventmanager.events.domain;

import dev.eventmanager.events.registration.RegistrationUserEvent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public record Event(
        Long id,
        String name,
        List<RegistrationUserEvent> registrations,
        OffsetDateTime startDate,
        ZoneOffset offsetDate,
        Integer duration,
        BigDecimal cost,
        Long ownerId,
        Long locationId,
        String status,
        Integer maxPlaces
) {
    public Event(Event event) {
        this(
                event.id,
                event.name(),
                event.registrations(),
                event.startDate(),
                event.offsetDate(),
                event.duration(),
                event.cost(),
                event.ownerId(),
                event.locationId(),
                event.status(),
                event.maxPlaces()
        );
    }
}
