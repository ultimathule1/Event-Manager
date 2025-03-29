package dev.eventmanager.retryable_task.db.repository;

import dev.eventmanager.retryable_task.RetryableTaskStatus;
import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.db.entities.RetryableTask;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RetryableTaskRepository extends CrudRepository<RetryableTask, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r FROM RetryableTask r where r.type = :type
        AND r.retryTime <= :retryTime
        AND r.status = :status
        ORDER BY r.retryTime ASC
        """)
    List<RetryableTask> findRetryableTaskForProcessing(RetryableTaskType type, Instant retryTime, RetryableTaskStatus status, Pageable pageable);

    @Query("""
        UPDATE RetryableTask r SET r.status = :status WHERE r IN :retryableTasks
    """)
    void updateRetryableTasks(List<RetryableTask> retryableTasks, RetryableTaskStatus status);
}