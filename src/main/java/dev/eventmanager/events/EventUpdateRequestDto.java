package dev.eventmanager.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventUpdateRequestDto(
        String eventName,
        Long maxPlaces,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        OffsetDateTime startDate,
        @PositiveOrZero
        BigDecimal cost,
        @Min(30)
        Long duration,
        Long locationId
) {
}
