package dev.eventmanager.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class EventUpdateBuilder {
    private final Long id;
    private final Long ownerId;
    private final Integer occupiedPlaces;
    private final String status;
    private Integer maxPlaces;
    private String name;
    private OffsetDateTime startTime;
    private Integer duration;
    private BigDecimal cost;
    private Long locationId;

    private EventUpdateBuilder(Event event) {
        id = event.id();
        name = event.name();
        maxPlaces = event.maxPlaces();
        occupiedPlaces = event.occupiedPlaces();
        startTime = event.startDate();
        duration = event.duration();
        cost = event.cost();
        ownerId = event.ownerId();
        locationId = event.locationId();
        status = event.status();
    }

    public static EventUpdateBuilder builder(Event event) {
        return new EventUpdateBuilder(event);
    }

    public EventUpdateBuilder changeName(String name) {
        this.name = name == null ? this.name : name;
        return this;
    }

    public EventUpdateBuilder changeMaxPlaces(Integer maxPlaces) {
        this.maxPlaces = maxPlaces == null ? this.maxPlaces : maxPlaces;
        return this;
    }

    public EventUpdateBuilder changeStartDate(OffsetDateTime startTime) {
        this.startTime = startTime == null ? this.startTime : startTime;
        return this;
    }

    public EventUpdateBuilder changeCost(BigDecimal cost) {
        this.cost = cost == null ? this.cost : cost;
        return this;
    }

    public EventUpdateBuilder changeDuration(Integer duration) {
        this.duration = duration == null ? this.duration : duration;
        return this;
    }

    public EventUpdateBuilder changeLocationId(Long locationId) {
        this.locationId = locationId == null ? this.locationId : locationId;
        return this;
    }

    public Event build() {
        return new Event(
                this.id,
                this.name,
                this.occupiedPlaces,
                this.startTime,
                this.duration,
                this.cost,
                this.ownerId,
                this.locationId,
                this.status,
                this.maxPlaces
        );
    }
}
