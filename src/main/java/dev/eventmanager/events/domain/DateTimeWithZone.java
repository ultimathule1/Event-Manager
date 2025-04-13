package dev.eventmanager.events.domain;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DateTimeWithZone {
    private final OffsetDateTime offsetDateTime;

    public DateTimeWithZone(OffsetDateTime dateTime) {
        this.offsetDateTime = dateTime;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public ZoneOffset getZoneOffset() {
        return offsetDateTime.getOffset();
    }

    public LocalDateTime getLocalDateTime() {
        return offsetDateTime.toLocalDateTime();
    }
}
