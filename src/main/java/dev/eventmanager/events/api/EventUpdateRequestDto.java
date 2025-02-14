package dev.eventmanager.events.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventUpdateRequestDto(
        @Size(min = 1)
        @JsonProperty("name")
        String eventName,
        @PositiveOrZero
        Integer maxPlaces,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        @Future
        @JsonProperty("date")
        OffsetDateTime startDate,
        @PositiveOrZero
        BigDecimal cost,
        @Min(30)
        Integer duration,
        @PositiveOrZero
        Long locationId
) {
}
