package dev.eventmanager.kafka.service;

import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import dev.eventmanager.events.domain.Event;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaEventMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEventMessageService.class);
    private final KafkaTemplate<Long, EventChangerEvent> kafkaTemplate;
    private final KafkaMessageCreator kafkaMessageCreator;

    public KafkaEventMessageService(KafkaTemplate<Long, EventChangerEvent> kafkaTemplate, KafkaMessageCreator kafkaMessageCreator) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaMessageCreator = kafkaMessageCreator;
    }

    public void sendKafkaEventMessage(String eventsTopicName, EventChangerEvent event) {
        sendMessage(eventsTopicName, event);
    }

    public void sendKafkaEventMessage(String eventsTopicName, Event eventBefore, Event eventAfter) {
        sendKafkaEventMessage(eventsTopicName, eventBefore, eventAfter, false);
    }

    public void sendKafkaEventMessage(String eventsTopicName, Event eventBefore, Event eventAfter, Boolean isUser) {
        EventChangerEvent messageEvent;
        messageEvent = createEventMessageEvent(eventBefore, eventAfter, isUser);

        sendMessage(eventsTopicName, messageEvent);
    }

    private void sendMessage(String eventsTopicName, EventChangerEvent event) {
        ProducerRecord<Long, EventChangerEvent> record = new ProducerRecord<>(
                eventsTopicName,
                null,
                event
        );

        record.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        CompletableFuture<SendResult<Long, EventChangerEvent>> future = kafkaTemplate.send(record);
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                LOGGER.error("Failed to send message: {}", exception.getMessage());
            } else {
                LOGGER.info("Message sent successfully: {}", result.getRecordMetadata());
            }
        });
    }

    public EventChangerEvent createEventMessageEvent(Event eventBefore, Event eventAfter, Boolean isUser) {
        if (eventAfter == null) {
            return isUser ?
                    kafkaMessageCreator.createEventMessageForUser(eventBefore) :
                    kafkaMessageCreator.createEventMessageForSystem(eventBefore);
        } else {
            return isUser ?
                    kafkaMessageCreator.createEventMessageForUser(eventBefore, eventAfter) :
                    kafkaMessageCreator.createEventMessageForSystem(eventBefore, eventAfter);
        }
    }
}
