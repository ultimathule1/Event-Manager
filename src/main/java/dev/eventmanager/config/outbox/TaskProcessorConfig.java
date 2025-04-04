package dev.eventmanager.config.outbox;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.kafka.service.KafkaEventMessageService;
import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.service.RetryableTaskService;
import dev.eventmanager.retryable_task.service.processor.RetryableTaskProcessor;
import dev.eventmanager.retryable_task.service.processor.SendCreateNotificationRequestRetryableTaskProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TaskProcessorConfig {
    @Bean
    public Map<RetryableTaskType, RetryableTaskProcessor> taskProcessors(
            MapperConfig mapperConfig,
            KafkaEventMessageService kafkaEventMessageService,
            RetryableTaskService retryableTaskService
    ) {
        Map<RetryableTaskType, RetryableTaskProcessor> processors = new HashMap<>();
        processors.put(RetryableTaskType.SEND_CREATE_NOTIFICATION_REQUEST,
                new SendCreateNotificationRequestRetryableTaskProcessor(
                        mapperConfig, kafkaEventMessageService, retryableTaskService));

        return processors;
    }
}
