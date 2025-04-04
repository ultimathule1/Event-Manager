package dev.eventmanager.retryable_task.service.processor;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import dev.eventmanager.kafka.KafkaEventProperties;
import dev.eventmanager.kafka.service.KafkaEventMessageService;
import dev.eventmanager.retryable_task.db.entities.RetryableTask;
import dev.eventmanager.retryable_task.service.RetryableTaskService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.generic.RET;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class SendCreateNotificationRequestRetryableTaskProcessor extends AbstractRetryableTaskProcessor {
    private final ModelMapper map;
    private final KafkaEventMessageService kafkaEventMessageService;

    public SendCreateNotificationRequestRetryableTaskProcessor(
            MapperConfig map,
            KafkaEventMessageService kafkaEventMessageService, RetryableTaskService retryableTaskService
    ) {
        super(retryableTaskService);
        this.map = map.getMapper();
        this.kafkaEventMessageService = kafkaEventMessageService;
    }

    @Override
    protected boolean processRetryableTask(RetryableTask retryableTask) {
        var event = map.map(retryableTask, EventChangerEvent.class);
        var future = kafkaEventMessageService.sendKafkaEventMessage(KafkaEventProperties.getTopicNotificationName(), event);

        try {
            var sendResult = future.get();
            return sendResult != null;
        } catch (InterruptedException | ExecutionException e) {
            log.warn("RetryableTask with id {} is failed to send kafka message", retryableTask.getId(), e);
        }

        return false;
    }
}
