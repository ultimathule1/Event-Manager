package dev.eventmanager.config;

import dev.eventmanager.events.db.EventEntity;
import dev.eventmanager.events.db.EventRepository;
import dev.eventmanager.events.domain.Event;
import dev.eventmanager.events.domain.EventStatus;
import dev.eventmanager.kafka.service.KafkaEventMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Configuration
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);
    private final EventRepository eventRepository;
    private final KafkaEventMessageService kafkaEventMessageService;
    private final MapperConfig mapperConfig;
    private final String topicName;

    public SchedulerConfig(
            EventRepository eventRepository,
            KafkaEventMessageService kafkaEventMessageService,
            MapperConfig mapperConfig,
            @Value("${events.notifications.topic.name}") String topicName) {
        this.eventRepository = eventRepository;
        this.kafkaEventMessageService = kafkaEventMessageService;
        this.mapperConfig = mapperConfig;
        this.topicName = topicName;
    }

    @Scheduled(cron = "${scheduler.interval.cron.every-minute}")
    public void schedulerForCheckEventStatus() {
        log.info("the scheduler for changing the event status started");
        Instant before = Instant.now();

        CompletableFuture<Void> startedEventsFuture = processStartedEvents();
        CompletableFuture<Void> endedEventsFuture = processEndedEvents();

        CompletableFuture.allOf(startedEventsFuture, endedEventsFuture).join();

        Instant after = Instant.now();
        log.info("the scheduler for changing the event status has been completed. Running time: {} seconds",
                schedulerRuntimeInSeconds(before, after));
    }

    @Async
    CompletableFuture<Void> processStartedEvents() {
        List<Event> startedEventsList = eventRepository.findStartedEventWithStatus(EventStatus.WAIT_START.name())
                .stream()
                .map(ee -> mapperConfig.getMapper().map(ee, Event.class))
                .toList();

        startedEventsList
                .forEach(e -> {
                    EventEntity ee = eventRepository.updateEventStatusById(e.id(), EventStatus.STARTED.name());
                    Event eventAfter = mapperConfig.getMapper().map(ee, Event.class);
                    kafkaEventMessageService.sendKafkaEventMessage(topicName, e, eventAfter, false);
                });

        return CompletableFuture.completedFuture(null);
    }

    @Async
    CompletableFuture<Void> processEndedEvents() {
        List<EventEntity> eventsList = eventRepository.findEndedEventWithStatus(EventStatus.STARTED.name());
        List<Event> endedEventsList = eventsList
                .stream()
                .map(ee -> mapperConfig.getMapper().map(ee, Event.class))
                .toList();

        endedEventsList
                .forEach(e -> {
                    EventEntity ee = eventRepository.updateEventStatusById(e.id(), EventStatus.FINISHED.name());
                    Event eventAfter = mapperConfig.getMapper().map(ee, Event.class);
                    kafkaEventMessageService.sendKafkaEventMessage(topicName, e, eventAfter, false);
                });

        return CompletableFuture.completedFuture(null);
    }

    private String schedulerRuntimeInSeconds(Instant before, Instant after) {
        long differenceMillis = after.toEpochMilli() - before.toEpochMilli();
        double differenceSeconds = differenceMillis / 1000.0;

        return String.format("%.3f", differenceSeconds);
    }
}
