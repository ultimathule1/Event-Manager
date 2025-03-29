package dev.eventmanager.retryable_task.service;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import dev.eventmanager.kafka.service.KafkaEventMessageService;
import dev.eventmanager.retryable_task.db.entities.RetryableTask;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SendCreateNotificationRequestRetryableTaskProcessor implements RetryableTaskProcessor {
    private final ModelMapper map;
    private final KafkaEventMessageService kafkaEventMessageService;
    @Value("${events.notifications.topic.name}")
    private String toSendTopicName;

    public SendCreateNotificationRequestRetryableTaskProcessor(
            MapperConfig map,
            KafkaEventMessageService kafkaEventMessageService
            ) {
        this.map = map.getMapper();
        this.kafkaEventMessageService = kafkaEventMessageService;
    }

    @Override
    public void processRetryableTasks(List<RetryableTask> retryableTasks) {
        for (RetryableTask retryableTask : retryableTasks) {
            var event = map.map(retryableTask, EventChangerEvent.class);
            kafkaEventMessageService.sendKafkaEventMessage(toSendTopicName, event);
        }
    }
}
