package dev.eventmanager.retryable_task.scheduler;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.kafka.service.KafkaEventMessageService;
import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.service.RetryableTaskProcessor;
import dev.eventmanager.retryable_task.service.RetryableTaskService;
import dev.eventmanager.retryable_task.service.SendCreateNotificationRequestRetryableTaskProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class RetryableTaskScheduler {
    private final RetryableTaskService retryableTaskService;
    private final MapperConfig mapperConfig;
    private final KafkaEventMessageService kafkaEventMessageService;
    private final Map<RetryableTaskType, RetryableTaskProcessor> taskProcessors;

    public RetryableTaskScheduler(
            RetryableTaskService retryableTaskService,
            MapperConfig mapperConfig,
            KafkaEventMessageService kafkaEventMessageService) {
        this.retryableTaskService = retryableTaskService;
        this.mapperConfig = mapperConfig;
        this.kafkaEventMessageService = kafkaEventMessageService;
        taskProcessors = Map.of(
                RetryableTaskType.SEND_CREATE_NOTIFICATION_REQUEST,
                new SendCreateNotificationRequestRetryableTaskProcessor(
                        mapperConfig,
                        kafkaEventMessageService
                )
        );
    }


    @Scheduled(fixedRate = 5000)
    public void executeRetryableTasks() {
        log.info("Starting retryable task processors");
        for (Map.Entry<RetryableTaskType, RetryableTaskProcessor> entry : taskProcessors.entrySet()) {
            var taskType = entry.getKey();
            var taskProcessor = entry.getValue();

            var retryableTasks = retryableTaskService.getRetryableTasksForProcessing(taskType);

            if (retryableTasks.isEmpty()) {
                log.info("No retryable tasks found for type {}", taskType);
                return;
            }

            taskProcessor.processRetryableTasks(retryableTasks);
        }

        log.info("Completed all retryable task processors");
    }

}
