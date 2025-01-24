package dev.eventmanager.users.api;

public record SignInRequest(
        String login,
        String password
) {
}
