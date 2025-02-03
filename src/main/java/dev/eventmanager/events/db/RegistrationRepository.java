package dev.eventmanager.events.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationEventUserEntity, Long> {

    Optional<RegistrationEventUserEntity> findByEventId(long eventId);
}