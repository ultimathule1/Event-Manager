package dev.eventmanager.users;

public record UserDto (
        Long id,
        String login,
        int age,
        String role
){
}
