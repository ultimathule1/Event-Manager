package dev.eventmanager.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    List<EventEntity> findAllByOwnerId(Long id);

    @Query("""
    SELECT e FROM EventEntity e
    WHERE (:name IS NULL OR e.name = :name)
    AND (:placesMin <=  e.maxPlaces AND :placesMax > e.maxPlaces)
    AND (:dateStartBefore <= e.startDate AND :dateStartAfter > e.startDate)
    AND (:costMin <= e.cost AND :costMax > e.cost)
    AND (:durationMin <= e.duration AND :durationMax > e.duration)
    AND (:locationId IS NULL OR e.locationId = :locationId)
    AND (:eventStatus IS NULL OR e.status = :eventStatus)
    """)
    List<EventEntity> searchEvents(
        String name,
        Integer placesMin,
        Integer placesMax,
        OffsetDateTime dateStartBefore,
        OffsetDateTime dateStartAfter,
        BigDecimal costMin,
        BigDecimal costMax,
        Integer durationMin,
        Integer durationMax,
        Integer locationId,
        String eventStatus
    );
}
