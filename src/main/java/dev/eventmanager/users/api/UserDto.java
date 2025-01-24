package dev.eventmanager.users.api;

public record UserDto(
        Long id,
        String login,
        int age,
        String role
) {
}
