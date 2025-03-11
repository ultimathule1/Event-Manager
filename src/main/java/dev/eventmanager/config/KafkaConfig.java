package dev.eventmanager.config;

import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaConfig {

    private final Environment env;

    public KafkaConfig(Environment env) {
        this.env = env;
    }

    @Bean
    KafkaTemplate<Long, EventChangerEvent> kafkaTemplate(
            KafkaProperties kafkaProperties
    ) {
        var props = kafkaProperties.buildProducerProperties(new DefaultSslBundleRegistry());
        ProducerFactory<Long, EventChangerEvent> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic createEventTopic() {
        return TopicBuilder
                .name(env.getProperty("events.notifications.topic.name", "events-notifications"))
                .partitions(Integer.parseInt(env.getProperty("events.notifications.topic.partitions", "3")))
                .replicas(Integer.parseInt(env.getProperty("events.notifications.topic.replicas", "3")))
                .configs(
                        Map.of("min.insync.replicas",
                                env.getProperty("events.notifications.topic.min.insync.replicas", "2")))
                .build();
    }
}