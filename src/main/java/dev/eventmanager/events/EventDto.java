package dev.eventmanager.events;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventDto(
        Long id,
        @NotNull
        String name,
        @NotNull
        Integer maxPlaces,
        @NotNull
        LocalDateTime date,
        @Min(0)
        @NotNull
        BigDecimal cost,
        int occupiedPlaces,
        @Min(30)
        @NotNull
        Integer duration,
        @NotNull
        Long locationId,
        @NotNull
        Long ownerId,
        @NotNull
        String status
) {
}