package dev.eventmanager.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record Event(
        Long id,
        String name,
        int occupiedPlaces,
        OffsetDateTime startDate,
        int duration,
        BigDecimal cost,
        Long ownerId,
        Long locationId,
        String status,
        int maxPlaces
) {
}
