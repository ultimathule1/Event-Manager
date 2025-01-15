package dev.eventmanager.locations;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    Logger log = LoggerFactory.getLogger(LocationController.class);
    LocationService locationService;
    LocationDtoMapper locationDtoMapper;

    public LocationController(LocationService locationService, LocationDtoMapper locationDtoMapper) {
        this.locationService = locationService;
        this.locationDtoMapper = locationDtoMapper;
    }

    @GetMapping
    public List<LocationDto> getAllLocations() {
        log.info("Received request to get all locations");
        return locationService.getAllLocations()
                .stream()
                .map(l -> locationDtoMapper.toDto(l))
                .toList();
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
}
