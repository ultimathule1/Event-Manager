package dev.eventmanager.users.domain;

public record User(
        Long id,
        String login,
        int age,
        UserRole role
) {
}
