package dev.eventmanager.locations;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    @NonNull
    List<LocationEntity> findAll();
}