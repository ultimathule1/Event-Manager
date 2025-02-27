package dev.eventmanager.users.api;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistration(
        @NotBlank(message = "login must not be empty")
        @Size(min = 4, message = "login size must be at least 4 characters")
        String login,
        @NotBlank(message = "password must not be empty")
        @Size(min = 4, message = "login size must be at least 4 characters")
        String password,
        @Min(value = 18, message = "must be at least 18 years of age")
        int age
) {
}
