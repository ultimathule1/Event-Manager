package dev.eventmanager.kafka;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventProperties {
    private static String TOPIC_NOTIFICATION_NAME;

    @Value("${events.notifications.topic.name}")
    private String tempName;

    @PostConstruct
    public void init() {
        TOPIC_NOTIFICATION_NAME = tempName;
    }

    public static String getTopicNotificationName() {
        return TOPIC_NOTIFICATION_NAME;
    }
}