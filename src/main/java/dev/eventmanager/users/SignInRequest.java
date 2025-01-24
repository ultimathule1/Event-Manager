package dev.eventmanager.users;

public record SignInRequest (
        String login,
        String password
) {
}
