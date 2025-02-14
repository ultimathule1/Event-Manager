package dev.eventmanager.config;

import dev.eventmanager.events.db.EventRepository;
import dev.eventmanager.events.domain.EventStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.List;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);
    private final EventRepository eventRepository;

    public SchedulerConfig(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(cron = "${scheduler.interval.cron.every-minute}")
    public void schedulerForCheckEventStatus() {
        log.info("the scheduler for changing the event status started");
        Instant before = Instant.now();

        List<Long> startedEventsList = eventRepository.findStartedEventWithStatus(EventStatus.WAIT_START.name());
        startedEventsList
                .forEach(id -> eventRepository.updateEventStatusById(id, EventStatus.STARTED.name()));

        List<Long> endedEventsList = eventRepository.findEndedEventWithStatus(EventStatus.STARTED.name());
        endedEventsList
                .forEach(id -> eventRepository.updateEventStatusById(id, EventStatus.FINISHED.name()));

        Instant after = Instant.now();
        log.info("the scheduler for changing the event status has been completed. Running time: {} seconds",
                schedulerRuntimeInSeconds(before, after));
    }

    private String schedulerRuntimeInSeconds(Instant before, Instant after) {
        long differenceMillis = after.toEpochMilli() - before.toEpochMilli();
        double differenceSeconds = differenceMillis / 1000.0;

        return String.format("%.3f", differenceSeconds);
    }
}
