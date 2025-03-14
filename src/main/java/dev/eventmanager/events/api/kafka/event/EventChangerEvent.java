package dev.eventmanager.events.api.kafka.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventChangerEvent {
    @NotNull
    @Positive
    private Long eventId;
    private Long changedEventByUserId;
    @NotNull
    @Positive
    private Long ownerEventId;
//    @NotNull
//    private List<> changedEventFields;
    private List<Long> eventSubscribers;

    private FieldChange<String> fieldEventName;
    private FieldChange<BigDecimal> fieldEventCost;
    private FieldChange<OffsetDateTime> fieldEventDate;
    private FieldChange<Integer> fieldMaxPlaces;
    private FieldChange<Integer> fieldDuration;
    private FieldChange<Long> fieldLocationId;
    private FieldChange<String> fieldStatus;

//    @Getter
//    @Setter
//    @AllArgsConstructor
//    @Builder
//    public static class FieldChange<T> {
//        T oldValue;
//        T newValue;
//    }
}