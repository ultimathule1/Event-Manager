package dev.eventmanager.locations;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationEntityMapper locationEntityMapper;

    public LocationService(LocationRepository locationRepository, LocationEntityMapper locationEntityMapper) {
        this.locationRepository = locationRepository;
        this.locationEntityMapper = locationEntityMapper;
    }

    List<Location> getAllLocations() {
        return locationRepository.findAll()
                .stream()
                .map(locationEntityMapper::toDomain)
                .toList();
    }

    Location createLocation(Location location) {
        checkIsLocationIdNotNull(location.id());
        var locationEntity = locationEntityMapper.toEntity(location);
        return locationEntityMapper.toDomain(locationRepository.save(locationEntity));
    }

    void deleteLocation(Long locationId) {
        checkLocationExists(locationId);
        locationRepository.deleteById(locationId);
    }

    Location getLocationById(Long locationId) {
        checkLocationExists(locationId);

        return locationEntityMapper.toDomain(locationRepository.getLocationEntityById(locationId));
    }

    Location fullUpdateLocation(Long locationId, Location updatedLocation) {
        checkIsLocationIdNotNull(updatedLocation.id());
        checkLocationExists(locationId);

        if (getLocationById(locationId).capacity() > updatedLocation.capacity()) {
            throw new IllegalArgumentException(
                    "The capacity of the location cannot be changed downwards");
        }

        return locationEntityMapper.toDomain(locationRepository.save(new LocationEntity(
                locationId,
                updatedLocation.name(),
                updatedLocation.address(),
                updatedLocation.capacity(),
                updatedLocation.description()
        )));
    }

    private void checkLocationExists(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new EntityNotFoundException("Location not found");
        }
    }

    private void checkIsLocationIdNotNull(Long locationId) {
        if (locationId != null) {
            throw new IllegalArgumentException("Location id must be null");
        }
    }
}