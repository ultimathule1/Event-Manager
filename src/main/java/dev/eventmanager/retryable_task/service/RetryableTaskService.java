package dev.eventmanager.retryable_task.service;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import dev.eventmanager.retryable_task.RetryableTaskProperties;
import dev.eventmanager.retryable_task.RetryableTaskStatus;
import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.db.entities.RetryableTask;
import dev.eventmanager.retryable_task.db.repository.RetryableTaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetryableTaskService {
    private final RetryableTaskRepository retryableTaskRepository;
    private final MapperConfig mapper;
    private final RetryableTaskProperties properties;

    @Transactional
    public RetryableTask createRetryableTask(EventChangerEvent event, RetryableTaskType type) {
        RetryableTask retryableTask = mapper.getMapper().map(event, RetryableTask.class);
        retryableTask.setType(type);
        return retryableTaskRepository.save(retryableTask);
    }

    @Transactional
    public List<RetryableTask> getRetryableTasksForProcessing(RetryableTaskType type) {
        var currentTime = Instant.now();
        Pageable pageable = PageRequest.of(0, properties.getLimit());
        List<RetryableTask> retryableTasks = retryableTaskRepository.findRetryableTaskForProcessing(
                type, currentTime, RetryableTaskStatus.IN_PROGRESS, pageable
        );

        //Write retry time to the future so that another scheduler cannot work with the same data
        for (RetryableTask retryableTask : retryableTasks) {
            retryableTask.setRetryTime(currentTime.plus(Duration.ofSeconds(properties.getTimeoutInSeconds())));
        }

        return retryableTasks;
    }

    @Transactional
    public void markRetryableTasksAsCompleted(List<RetryableTask> retryableTasks) {
        retryableTaskRepository.updateRetryableTasks(retryableTasks, RetryableTaskStatus.SUCCESS);
    }

    @Transactional
    public void deleteRetryableTasksThanMoreWeek() {
        retryableTaskRepository.deleteAllRetryableTasksThatExpiredMoreWeek();
    }
}
