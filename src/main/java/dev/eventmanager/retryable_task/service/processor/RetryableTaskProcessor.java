package dev.eventmanager.retryable_task.service.processor;

import dev.eventmanager.retryable_task.db.entities.RetryableTaskEntity;

import java.util.List;

public interface RetryableTaskProcessor {
    void processRetryableTasks(List<RetryableTaskEntity> retryableTaskEntities);
}
