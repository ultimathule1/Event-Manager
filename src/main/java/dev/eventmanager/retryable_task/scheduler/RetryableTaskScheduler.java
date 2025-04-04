package dev.eventmanager.retryable_task.scheduler;

import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.service.RetryableTaskService;
import dev.eventmanager.retryable_task.service.processor.RetryableTaskProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class RetryableTaskScheduler {
    private final RetryableTaskService retryableTaskService;
    private final Map<RetryableTaskType, RetryableTaskProcessor> taskProcessors;

    public RetryableTaskScheduler(
            RetryableTaskService retryableTaskService,
            Map<RetryableTaskType, RetryableTaskProcessor> taskProcessors
    ) {
        this.retryableTaskService = retryableTaskService;
        this.taskProcessors = taskProcessors;
    }


    @Scheduled(cron = "${scheduler.interval.cron.every-five-seconds}")
    public void executeRetryableTasks() {
        log.info("Starting retryable task processors");
        for (Map.Entry<RetryableTaskType, RetryableTaskProcessor> entry : taskProcessors.entrySet()) {
            var taskType = entry.getKey();
            var taskProcessor = entry.getValue();

            var retryableTasks = retryableTaskService.getRetryableTasksForProcessing(taskType);

            if (retryableTasks.isEmpty()) {
                log.info("No retryable tasks found for type {}", taskType);
                continue;
            }

            taskProcessor.processRetryableTasks(retryableTasks);
        }

        log.info("Completed all retryable task processors");
    }

    @Scheduled(cron = "${scheduler.interval.cron.every-ten-minutes}")
    public void clearRetryableTasksThatCreatedMoreThanWeek() {
        log.info("Starting clear retryable task processors that were created more than a week");
        retryableTaskService.deleteRetryableTasksThanMoreWeek();
        log.info("The scheduler to clear retryable task processors that were created more than a week is finished");
    }
}
