package dev.eventmanager.retryable_task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RetryableTaskStatus {
    IN_PROGRESS("IN_PROGRESS"),
    SUCCESS("SUCCESS");

    private final String value;

    public static RetryableTaskStatus fromValue(String v) {
        for (RetryableTaskStatus status : RetryableTaskStatus.values()) {
            if (status.value.equals(v)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + v);
    }
}
