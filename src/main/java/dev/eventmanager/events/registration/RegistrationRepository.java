package dev.eventmanager.events.registration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationUserEventEntity, Long> {

    Optional<RegistrationUserEventEntity> findByEventIdAndUserId(long eventId, long userId);

    void deleteById(long id);
}