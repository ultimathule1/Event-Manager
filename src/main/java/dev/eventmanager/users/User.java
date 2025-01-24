package dev.eventmanager.users;

public record User(
        Long id,
        String login,
        int age,
        UserRole role
) {
}
