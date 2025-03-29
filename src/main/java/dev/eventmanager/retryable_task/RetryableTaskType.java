package dev.eventmanager.retryable_task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RetryableTaskType {
    SEND_CREATE_NOTIFICATION_REQUEST("SEND_CREATE_NOTIFICATION_REQUEST");

    private final String value;

    public static RetryableTaskType fromValue(String v) {
        for (RetryableTaskType type : RetryableTaskType.values()) {
            if (type.value.equals(v)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown type: " + v);
    }
}
