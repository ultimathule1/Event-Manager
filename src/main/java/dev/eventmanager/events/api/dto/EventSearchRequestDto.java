package dev.eventmanager.events.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventSearchRequestDto(
        @JsonProperty("name") String name,
        Integer placesMin,
        Integer placesMax,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        OffsetDateTime dateStartBefore,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        OffsetDateTime dateStartAfter,
        BigDecimal costMin,
        BigDecimal costMax,
        Integer durationMin,
        @JsonProperty("durationMax")
        Integer durationMax,
        Integer locationId,
        String eventStatus) {

    @Override
    public String toString() {
        return "EventSearchRequestDto{" +
                "name='" + name + '\'' +
                ", placesMin=" + placesMin +
                ", placesMax=" + placesMax +
                ", dateStartBefore=" + dateStartBefore +
                ", dateStartAfter=" + dateStartAfter +
                ", costMin=" + costMin +
                ", costMax=" + costMax +
                ", durationMin=" + durationMin +
                ", durationMax=" + durationMax +
                ", locationId=" + locationId +
                ", eventStatus='" + eventStatus + '\'' +
                '}';
    }
}
