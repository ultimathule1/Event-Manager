package dev.eventmanager.locations;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocationDto(
        Long i,
        @NotBlank(message = "name should not be empty")
        String name,
        @NotBlank(message = "address should not be empty")
        String address,
        @Min(value = 5, message = "capacity should not be less than 5")
        @NotNull(message = "capacity should not be null")
        Integer capacity,
        String description
) {
}
