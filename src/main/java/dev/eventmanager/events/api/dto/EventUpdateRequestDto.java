package dev.eventmanager.events.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EventUpdateRequestDto(
        @Size(min = 1)
        @JsonProperty("name")
        String eventName,
        @PositiveOrZero
        Integer maxPlaces,
        @JsonProperty("date")
        String startDate,
        @PositiveOrZero
        BigDecimal cost,
        @Min(30)
        Integer duration,
        @PositiveOrZero
        Long locationId
) {
}
