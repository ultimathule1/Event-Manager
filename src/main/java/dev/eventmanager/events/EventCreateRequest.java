package dev.eventmanager.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventCreateRequest(
        @NotEmpty
        String name,
        @NotNull
        @Positive
        int maxPlaces,
        @NotNull
        @Future
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        OffsetDateTime date,
        @NotNull
        @PositiveOrZero
        BigDecimal cost,
        @NotNull
        @Min(30)
        int duration,
        @NotNull
        Long locationId
) {
}