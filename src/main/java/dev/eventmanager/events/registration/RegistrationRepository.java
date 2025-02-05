package dev.eventmanager.events.registration;

import dev.eventmanager.events.db.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationUserEventEntity, Long> {

    Optional<RegistrationUserEventEntity> findByEventIdAndUserId(long eventId, long userId);

    //    @Query(value = """
//        SELECT r.event FROM RegistrationUserEventEntity AS r
//        WHERE r.userId = :userId
//    """
//    )
//    @Query("""
//            SELECT e FROM EventEntity e
//            JOIN FETCH RegistrationUserEventEntity r ON e.id = r.event.id AND r.userId = :userId
//            """)

    @Query("""
        SELECT e FROM EventEntity e
        JOIN FETCH e.registrations r
        WHERE r.userId = :userId
    """)
    List<EventEntity> findAllEventsWhereUserRegistered(
            Long userId);
}