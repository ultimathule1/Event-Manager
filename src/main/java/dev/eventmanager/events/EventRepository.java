package dev.eventmanager.events;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    @Transactional
    @Modifying
    @Query("""
            UPDATE EventEntity e
            SET e.name = :name,
                e.maxPlaces = :maxPlaces,
                e.startDate = :date,
                e.cost = :cost,
                e.duration = :duration,
                e.locationId = :locationId
            WHERE e.id = :id
    """)
    EventEntity updateEventByRequest(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("maxPlaces") int maxPlaces,
            @Param("date") OffsetDateTime date,
            @Param("cost") BigDecimal cost,
            @Param("duration") int duration,
            @Param("locationId") Long locationId
    );
}
