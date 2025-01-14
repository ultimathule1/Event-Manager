package dev.eventmanager.locations;

public record Location(
        Long id,
        String name,
        String address,
        Integer capacity,
        String description
) {
}
