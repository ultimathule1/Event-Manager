package dev.eventmanager.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Event(
        Long id,
        String name,
        int occupiedPlaces,
        LocalDateTime startDate,
        int duration,
        BigDecimal cost,
        Long ownerId,
        Long locationId,
        String status,
        int maxPlaces
) {
}
