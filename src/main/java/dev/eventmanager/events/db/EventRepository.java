package dev.eventmanager.events.db;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    List<EventEntity> findAllByOwnerId(Long id);

    @Query("""
            SELECT e FROM EventEntity e
            WHERE (:name IS NULL OR e.name = :name)
            AND (:placesMin IS NULL OR :placesMin <= e.maxPlaces)
            AND (:placesMax IS NULL OR :placesMax > e.maxPlaces)
            AND (CAST(:dateStartBefore as date) IS NULL OR :dateStartBefore <= e.date)
            AND (CAST(:dateStartAfter as date) IS NULL OR :dateStartAfter > e.date)
            AND (:costMin IS NULL OR :costMin <= e.cost)
            AND (:costMax IS NULL OR :costMax > e.cost)
            AND (:durationMin IS NULL OR :durationMin <= e.duration)
            AND (:durationMax IS NULL OR :durationMax > e.duration)
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

    @Query("""
            SELECT e.id FROM EventEntity e
            WHERE e.date < CURRENT_TIMESTAMP
            AND e.status = :status
            """
    )
    List<Long> findStartedEventWithStatus(
            @Param("status") String status
    );

    @Transactional
    @Modifying
    @Query("""
            UPDATE EventEntity e
            SET e.status = :status
            WHERE e.id = :id
            """
    )
    void updateEventStatusById(
            @Param("id") Long id,
            @Param("status") String status);

    @Query(value = """
            SELECT e.id FROM events e
            WHERE (e.date + INTERVAL '1 MINUTE' * e.duration) < CURRENT_TIMESTAMP
            AND e.status = :status
            """
            , nativeQuery = true)
    List<Long> findEndedEventWithStatus(
            @Param("status") String status
    );

}
