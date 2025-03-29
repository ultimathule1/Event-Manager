package dev.eventmanager.retryable_task.service;

import dev.eventmanager.retryable_task.db.entities.RetryableTask;

import java.util.List;

public interface RetryableTaskProcessor {
    void processRetryableTasks(List<RetryableTask> retryableTasks);
}
