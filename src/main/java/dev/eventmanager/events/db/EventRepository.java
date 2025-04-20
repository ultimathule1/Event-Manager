package dev.eventmanager.events.db;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

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
            SELECT e FROM EventEntity e
            LEFT JOIN FETCH e.registrations
            WHERE e.date < :now
            AND e.status = :status
            """
    )
    List<EventEntity> findStartedEventWithStatus(
            @Param("status") String status,
            OffsetDateTime now
    );

    @Transactional
    @Modifying
    @Query("""
            UPDATE EventEntity e
            SET e.status = :status
            WHERE e.id = :id
            """
    )
    int updateEventStatusById(
            @Param("id") Long id,
            @Param("status") String status);

    @Query(value = """
            SELECT e.id FROM events e
            LEFT JOIN registrations r ON r.event_id = e.id
            WHERE (e.date + INTERVAL '1 MINUTE' * e.duration) < :now
            AND e.status = :status
            """
            , nativeQuery = true)
    List<Long> findEndedEventWithStatus(
            @Param("status") String status,
            @Param("now") OffsetDateTime now
    );

    @Query("SELECT e.date FROM EventEntity e")
    List<OffsetDateTime> getDates();

    @EntityGraph(value = "Event.withRegistrations", type = EntityGraph.EntityGraphType.LOAD)
    @Query(value = """
                SELECT e FROM EventEntity e
                WHERE e.id IN :ids
            """)
    List<EventEntity> findAllWithRegistrations(@Param("ids") List<Long> ids);

    @Query("""
            SELECT e FROM EventEntity e
            WHERE e.locationId = :locationId AND (e.date BETWEEN :dateBeginning AND :dateEnding)
            AND (:id IS NULL OR  e.id <> :id)
            """)
    List<EventEntity> findEventsWhereDateIsBusyWithoutId(Long locationId, OffsetDateTime dateBeginning, OffsetDateTime dateEnding, Long id);

    Optional<EventEntity> findFirstByLocationIdAndDateBeforeOrderByDateDesc(Long locationId, OffsetDateTime dateBeginning);
}