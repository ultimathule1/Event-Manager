package dev.eventmanager.events.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventDto(
        Long id,
        @NotNull
        String name,
        @NotNull
        Integer maxPlaces,
        @NotNull
        OffsetDateTime date,
        @Min(0)
        @NotNull
        BigDecimal cost,
        @NotNull
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