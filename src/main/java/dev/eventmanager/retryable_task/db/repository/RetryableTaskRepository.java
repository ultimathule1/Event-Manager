package dev.eventmanager.retryable_task.db.repository;

import dev.eventmanager.retryable_task.RetryableTaskStatus;
import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.db.entities.RetryableTaskEntity;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RetryableTaskRepository extends CrudRepository<RetryableTaskEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r FROM RetryableTaskEntity r where r.type = :type
            AND r.retryTime <= :retryTime
            AND r.status = :status
            ORDER BY r.retryTime ASC
            """)
    List<RetryableTaskEntity> findRetryableTaskForProcessing(RetryableTaskType type, Instant retryTime, RetryableTaskStatus status, Pageable pageable);

    @Query("""
            UPDATE RetryableTaskEntity r SET r.status = :status WHERE r IN :retryableTaskEntities
            """)
    @Modifying
    @Transactional
    void updateRetryableTasks(List<RetryableTaskEntity> retryableTaskEntities, RetryableTaskStatus status);

    @Query(value = """
            DELETE FROM retryable_task r
            WHERE (r.created_at + INTERVAL '1 WEEK') < CURRENT_TIMESTAMP
            """,
            nativeQuery = true
    )
    @Modifying
    @Transactional
    void deleteAllRetryableTasksThatExpiredMoreWeek();
}