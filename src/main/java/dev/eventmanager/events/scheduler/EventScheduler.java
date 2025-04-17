package dev.eventmanager.events.scheduler;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.events.db.EventEntity;
import dev.eventmanager.events.db.EventRepository;
import dev.eventmanager.events.domain.Event;
import dev.eventmanager.events.domain.EventStatus;
import dev.eventmanager.kafka.service.KafkaEventMessageService;
import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.service.RetryableTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class EventScheduler {
    private static final Logger log = LoggerFactory.getLogger(EventScheduler.class);
    private final EventRepository eventRepository;
    private final KafkaEventMessageService kafkaEventMessageService;
    private final RetryableTaskService retryableTaskService;
    private final MapperConfig mapperConfig;

    public EventScheduler(
            EventRepository eventRepository,
            KafkaEventMessageService kafkaEventMessageService,
            RetryableTaskService retryableTaskService,
            MapperConfig mapperConfig) {

        this.eventRepository = eventRepository;
        this.kafkaEventMessageService = kafkaEventMessageService;
        this.retryableTaskService = retryableTaskService;
        this.mapperConfig = mapperConfig;
    }

    @Scheduled(cron = "${scheduler.interval.cron.every-ten-seconds}")
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

    CompletableFuture<Void> processStartedEvents() {
        System.out.println(eventRepository.getDates() + " OffsetDateTime" + OffsetDateTime.now(ZoneOffset.UTC));
        List<EventEntity> startedEventsList =
                eventRepository.findStartedEventWithStatus(EventStatus.WAIT_START.name(), OffsetDateTime.now(ZoneOffset.UTC));

        for (EventEntity startedEvent : startedEventsList) {
            int amountUpdates = eventRepository.updateEventStatusById(startedEvent.getId(), EventStatus.STARTED.name());

            logAndSendToKafka(startedEvent, EventStatus.STARTED, amountUpdates);
        }

        return CompletableFuture.completedFuture(null);
    }

    CompletableFuture<Void> processEndedEvents() {
        List<EventEntity> eventsList = findEndedEventsWithRegistrations(EventStatus.STARTED);

        for (EventEntity endedEvent : eventsList) {
            int amountUpdates = eventRepository.updateEventStatusById(endedEvent.getId(), EventStatus.FINISHED.name());

            logAndSendToKafka(endedEvent, EventStatus.FINISHED, amountUpdates);
        }

        return CompletableFuture.completedFuture(null);
    }

    private void logAndSendToKafka(EventEntity eventEntity, EventStatus eventStatus, int amountUpdates) {
        if (amountUpdates == 0) {
            log.warn("Found event with id={} were not found during the update", eventEntity.getId());
            return;
        }

        EventEntity endedEventAfter = new EventEntity();
        BeanUtils.copyProperties(eventEntity, endedEventAfter);
        endedEventAfter.setStatus(eventStatus.name());

        Event eventBefore = mapperConfig.getMapper().map(eventEntity, Event.class);
        Event eventAfter = mapperConfig.getMapper().map(endedEventAfter, Event.class);
        log.debug("event with id={} changed status to {}", endedEventAfter.getId(), endedEventAfter.getStatus());

        if (!eventAfter.registrations().isEmpty()) {
            var messageEvent = kafkaEventMessageService.createEventMessageEvent(eventBefore, eventAfter, false);
            retryableTaskService.createRetryableTask(messageEvent, RetryableTaskType.SEND_CREATE_NOTIFICATION_REQUEST);

            log.debug("event with id={} send to kafka", eventAfter.id());
        }
    }

    private List<EventEntity> findEndedEventsWithRegistrations(EventStatus status) {
        List<Long> eventIdsWithStatus = eventRepository.findEndedEventWithStatus(status.name(), OffsetDateTime.now(ZoneOffset.UTC));
        if (eventIdsWithStatus.isEmpty()) {
            return List.of();
        }

        return eventRepository.findAllWithRegistrations(eventIdsWithStatus);
    }

    private String schedulerRuntimeInSeconds(Instant before, Instant after) {
        long differenceMillis = after.toEpochMilli() - before.toEpochMilli();
        double differenceSeconds = differenceMillis / 1000.0;

        return String.format("%.3f", differenceSeconds);
    }
}
