package dev.eventmanager.events.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventCreateRequestDto(
        @NotEmpty
        String name,
        @NotNull
        @Positive
        Integer maxPlaces,
        @NotNull
        String date,
        @NotNull
        @PositiveOrZero
        BigDecimal cost,
        @NotNull
        @Min(30)
        Integer duration,
        @NotNull
        Long locationId
) {
}