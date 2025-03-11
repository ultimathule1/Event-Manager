package dev.eventmanager.events.api.kafka.event;

import dev.eventmanager.events.api.kafka.ChangedEventFields;
import dev.eventmanager.users.domain.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventChangerEvent {
    @NotNull
    @Positive
    private Long eventId;
    private Long changedEventByUserId;
    @NotNull
    @Positive
    private Long ownerEventId;
    @NotNull
    private ChangedEventFields changedEventFields;
    @NotNull
    private List<User> eventSubscribers;
}