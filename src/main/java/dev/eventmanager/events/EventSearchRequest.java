package dev.eventmanager.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Getter
public class EventSearchRequest {
    @JsonProperty("name")
    private final String name;
    private final Integer placesMin;
    private final Integer placesMax;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private final OffsetDateTime dateStartBefore;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private final OffsetDateTime dateStartAfter;
    private final BigDecimal costMin;
    private final BigDecimal costMax;
    private final Integer durationMin;
    @JsonProperty("durationMax")
    private final Integer durationMax;
    private final Integer locationId;
    private final String eventStatus;

    private static final Integer DEFAULT_PLACES_MIN = 0;
    private static final Integer DEFAULT_PLACES_MAX = 1000;
    private static final OffsetDateTime DEFAULT_DATE_START_BEFORE = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    private static final OffsetDateTime DEFAULT_DATE_TIME_AFTER = OffsetDateTime.now().plusYears(10);
    private static final BigDecimal DEFAULT_COST_MIN = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_COST_MAX = BigDecimal.valueOf(1_000_000).setScale(2, RoundingMode.HALF_UP);
    private static final Integer DEFAULT_DURATION_MIN = 0;
    private static final Integer DEFAULT_DURATION_MAX = 1_000_000;

    public EventSearchRequest(String name, Integer placesMin, Integer placesMax, OffsetDateTime dateStartBefore, OffsetDateTime dateStartAfter, BigDecimal costMin, BigDecimal costMax, Integer durationMin, Integer durationMax, Integer locationId, String eventStatus) {
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
        return "EventSearchRequest{" +
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
