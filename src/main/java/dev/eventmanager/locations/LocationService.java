package dev.eventmanager.locations;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    private final Logger log = LoggerFactory.getLogger(LocationService.class);
    private final LocationRepository locationRepository;
    private final LocationEntityMapper locationEntityMapper;

    public LocationService(LocationRepository locationRepository, LocationEntityMapper locationEntityMapper) {
        this.locationRepository = locationRepository;
        this.locationEntityMapper = locationEntityMapper;
    }

    public List<Location> getAllLocations() {
        Pageable pageable = PageRequest.of(0, 3);

        return locationRepository.findAll(pageable)
                .stream()
                .map(locationEntityMapper::toDomain)
                .toList();
    }

    public Location createLocation(Location location) {
        checkIsLocationIdNull(location.id());
        var locationEntity = locationEntityMapper.toEntity(location);
        log.info("created location: {}", locationEntity);
        return locationEntityMapper.toDomain(locationRepository.save(locationEntity));
    }

    public void deleteLocation(Long locationId) {
        checkLocationExistsById(locationId);
        locationRepository.deleteById(locationId);
        log.info("location with id={} deleted", locationId);
    }

    public Location getLocationById(Long locationId) {
        checkLocationExistsById(locationId);

        return locationEntityMapper.toDomain(locationRepository.getLocationEntityById(locationId));
    }

    public Location updateLocation(Long locationId, Location updatedLocation) {
        checkIsLocationIdNull(updatedLocation.id());

        if (getLocationById(locationId).capacity() > updatedLocation.capacity()) {
            throw new IllegalArgumentException(
                    "The capacity of the location cannot be changed downwards");
        }

        LocationEntity locationEntity = locationRepository.save(new LocationEntity(
                locationId,
                updatedLocation.name(),
                updatedLocation.address(),
                updatedLocation.capacity(),
                updatedLocation.description()
        ));

        log.info("location with id={} updated", locationId);

        return locationEntityMapper.toDomain(locationEntity);
    }

    public boolean existsLocationById(Long locationId) {
        return locationRepository.existsById(locationId);
    }

    private void checkLocationExistsById(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new EntityNotFoundException("Location with id=%s not found".formatted(locationId));
        }
    }

    private void checkIsLocationIdNull(Long locationId) {
        if (locationId != null) {
            throw new IllegalArgumentException("Location id must be null");
        }
    }
}