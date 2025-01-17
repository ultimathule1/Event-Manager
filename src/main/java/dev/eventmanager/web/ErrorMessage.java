package dev.eventmanager.web;

import java.time.LocalDateTime;

public record ErrorMessage(
        String message,
        String detailMessage,
        LocalDateTime dateTime
) {
}
