package dev.eventmanager.locations;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private static final Logger log = LoggerFactory.getLogger(LocationController.class);
    private final LocationService locationService;
    LocationDtoMapper locationDtoMapper;

    public LocationController(LocationService locationService, LocationDtoMapper locationDtoMapper) {
        this.locationService = locationService;
        this.locationDtoMapper = locationDtoMapper;
    }

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        log.info("Received request to get all locations");
        return ResponseEntity
                .ok()
                .body(
                        locationService.getAllLocations()
                                .stream()
                                .map(location -> locationDtoMapper.toDto(location))
                                .toList()
                );
    }

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(
            @RequestBody @Valid LocationDto locationDto
    ) {
        log.info("Received request to create a new location: locationDto={}", locationDto);
        var createdLocation = locationService.createLocation(locationDtoMapper.toDomain(locationDto));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(locationDtoMapper.toDto(createdLocation));
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable long locationId
    ) {
        log.info("Received request to delete a location: locationId={}", locationId);
        locationService.deleteLocation(locationId);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<LocationDto> getLocationById(
            @PathVariable long locationId
    ) {
        log.info("Received request to retrieve a location: locationId={}", locationId);
        Location location = locationService.getLocationById(locationId);

        return ResponseEntity
                .ok()
                .body(locationDtoMapper.toDto(location));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocation(
            @PathVariable long id,
            @RequestBody @Valid LocationDto locationDto
    ) {
        log.info("Received request to update a location: id={}; locationDto={}", id, locationDto);
        return ResponseEntity
                .ok()
                .body(locationDtoMapper.toDto(
                                locationService.updateLocation(id, locationDtoMapper.toDomain(locationDto))
                        )
                );
    }
}
