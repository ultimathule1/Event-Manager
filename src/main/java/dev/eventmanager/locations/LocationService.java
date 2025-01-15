package dev.eventmanager.locations;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    LocationRepository locationRepository;
    LocationEntityMapper locationEntityMapper;

    public LocationService(LocationRepository locationRepository, LocationEntityMapper locationEntityMapper) {
        this.locationRepository = locationRepository;
        this.locationEntityMapper = locationEntityMapper;
    }

    List<Location> getAllLocations() {
        return locationRepository.findAll()
                .stream()
                .map(l -> locationEntityMapper.toDomain(l))
                .toList();
    }

    Location createLocation(Location location) {
        var locationEntity = locationEntityMapper.toEntity(location);
        return locationEntityMapper.toDomain(locationRepository.save(locationEntity));
    }
}
