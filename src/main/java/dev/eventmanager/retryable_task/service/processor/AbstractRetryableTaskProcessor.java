package dev.eventmanager.retryable_task.service.processor;

import dev.eventmanager.retryable_task.db.entities.RetryableTask;
import dev.eventmanager.retryable_task.service.RetryableTaskService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRetryableTaskProcessor implements RetryableTaskProcessor {

    private final RetryableTaskService retryableTaskService;
    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    public void processRetryableTasks(List<RetryableTask> retryableTasks) {
        log.debug("Processing retryable tasks is started");

        List<CompletableFuture<Pair<RetryableTask, Boolean>>> futures = retryableTasks.stream()
                .map(task -> CompletableFuture.supplyAsync(() -> {
                    log.debug("Processing retryable task with Id : {}", task.getId());
                    boolean success = processRetryableTask(task);
                    if (success) {
                        log.debug("Task with ID: {} processed successfully", task.getId());
                    } else {
                        log.warn("Task with ID: {} failed to process", task.getId());
                    }
                    return new Pair<>(task, success);
                }, cachedThreadPool))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<RetryableTask> successTasks = futures.stream()
                .map(CompletableFuture::join)
                .filter(Pair::value)
                .map(Pair::key)
                .toList();

        if (!successTasks.isEmpty()) {
            retryableTaskService.markRetryableTasksAsCompleted(successTasks);
            log.info("Marked {} retryable tasks as completed", successTasks.size());
        } else {
            log.info("No tasks were marked as completed");
        }
    }

    protected abstract boolean processRetryableTask(RetryableTask retryableTask);

    public record Pair<K, V>(K key, V value) {
    }
}