package dev.eventmanager.events;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventCreateRequestDto (
        @NotEmpty
        String name,
        @NotNull
        @Min(1)
        int maxPlaces,
        @NotNull
        @Future
        LocalDateTime date,
        @NotNull
        @PositiveOrZero
        BigDecimal cost,
        @NotNull
        @Min(30)
        int duration,
        @NotNull
        Long locationId
){
}
