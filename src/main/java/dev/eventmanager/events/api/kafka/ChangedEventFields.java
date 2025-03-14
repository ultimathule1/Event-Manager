package dev.eventmanager.events.api.kafka;

import dev.eventmanager.events.domain.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChangedEventFields {
    /**
     * First is old value, Second is new value
     * Values can be null!!!
     */
    private HashMap<String, String> eventName;
    private HashMap<Integer, Integer> maxPlaces;
    private HashMap<OffsetDateTime, OffsetDateTime> startTime;
    private HashMap<BigDecimal, BigDecimal> cost;
    private HashMap<Integer, Integer> duration;
    private HashMap<Long, Long> locationId;
    private HashMap<String, String> status;

    public ChangedEventFields(Event event) {
        eventName = new HashMap<>();
        eventName.put(event.name(), null);
        maxPlaces = new HashMap<>();
        maxPlaces.put(event.maxPlaces(), null);
        startTime = new HashMap<>();
        startTime.put(event.startDate(), null);
        cost = new HashMap<>();
        cost.put(event.cost(), null);
        duration = new HashMap<>();
        duration.put(event.duration(), null);
        locationId = new HashMap<>();
        locationId.put(event.locationId(), null);
        status = new HashMap<>();
        status.put(event.status(), null);
    }
}