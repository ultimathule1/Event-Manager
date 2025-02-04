package dev.eventmanager.events.domain;

import dev.eventmanager.events.registration.RegistrationUserEvent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record Event(
        Long id,
        String name,
        List<RegistrationUserEvent> registrations,
        OffsetDateTime startDate,
        int duration,
        BigDecimal cost,
        Long ownerId,
        Long locationId,
        String status,
        int maxPlaces
) {
}
