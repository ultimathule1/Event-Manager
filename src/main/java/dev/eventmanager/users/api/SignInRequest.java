package dev.eventmanager.users.api;

import jakarta.validation.constraints.NotNull;

public record SignInRequest(
        @NotNull
        String login,
        @NotNull
        String password
) {
}
