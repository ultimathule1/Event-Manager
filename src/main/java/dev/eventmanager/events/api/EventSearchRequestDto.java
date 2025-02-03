package dev.eventmanager.events.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record EventSearchRequestDto(
        @JsonProperty("name") String name, Integer placesMin, Integer placesMax,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") OffsetDateTime dateStartBefore,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") OffsetDateTime dateStartAfter,
        BigDecimal costMin, BigDecimal costMax, Integer durationMin,
        @JsonProperty("durationMax") Integer durationMax, Integer locationId,
        String eventStatus) {
    private static final Integer DEFAULT_PLACES_MIN = 0;
    private static final Integer DEFAULT_PLACES_MAX = 1000;
    private static final OffsetDateTime DEFAULT_DATE_START_BEFORE = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    private static final OffsetDateTime DEFAULT_DATE_TIME_AFTER = OffsetDateTime.now().plusYears(10);
    private static final BigDecimal DEFAULT_COST_MIN = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_COST_MAX = BigDecimal.valueOf(1_000_000).setScale(2, RoundingMode.HALF_UP);
    private static final Integer DEFAULT_DURATION_MIN = 0;
    private static final Integer DEFAULT_DURATION_MAX = 1_000_000;

    public EventSearchRequestDto(String name, Integer placesMin, Integer placesMax, OffsetDateTime dateStartBefore, OffsetDateTime dateStartAfter, BigDecimal costMin, BigDecimal costMax, Integer durationMin, Integer durationMax, Integer locationId, String eventStatus) {
        this.name = name;
        this.placesMin = placesMin == null ? DEFAULT_PLACES_MIN : placesMin;
        this.placesMax = placesMax == null ? DEFAULT_PLACES_MAX : placesMax;
        this.dateStartBefore = dateStartBefore == null ? DEFAULT_DATE_START_BEFORE : dateStartBefore;
        this.dateStartAfter = dateStartAfter == null ? DEFAULT_DATE_TIME_AFTER : dateStartAfter;
        this.costMin = costMin == null ? DEFAULT_COST_MIN : costMin;
        this.costMax = costMax == null ? DEFAULT_COST_MAX : costMax;
        this.durationMin = durationMin == null ? DEFAULT_DURATION_MIN : durationMin;
        this.durationMax = durationMax == null ? DEFAULT_DURATION_MAX : durationMax;
        this.locationId = locationId;
        this.eventStatus = eventStatus;
    }

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
