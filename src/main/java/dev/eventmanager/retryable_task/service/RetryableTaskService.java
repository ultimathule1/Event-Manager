package dev.eventmanager.retryable_task.service;

import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import dev.eventmanager.retryable_task.RetryableTaskProperties;
import dev.eventmanager.retryable_task.RetryableTaskStatus;
import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.db.entities.RetryableTaskEntity;
import dev.eventmanager.retryable_task.db.repository.RetryableTaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public RetryableTaskEntity createRetryableTask(EventChangerEvent event, RetryableTaskType type) {
        RetryableTaskEntity retryableTaskEntity = mapper.getMapper().map(event, RetryableTaskEntity.class);
        retryableTaskEntity.setType(type);
        return retryableTaskRepository.save(retryableTaskEntity);
    }

    @Transactional
    public List<RetryableTaskEntity> getRetryableTasksForProcessing(RetryableTaskType type) {
        var currentTime = Instant.now();
        Pageable pageable = PageRequest.of(0, properties.getLimit());
        List<RetryableTaskEntity> retryableTaskEntities = retryableTaskRepository.findRetryableTaskForProcessing(
                type, currentTime, RetryableTaskStatus.IN_PROGRESS, pageable
        );

        //Write retry time to the future so that another scheduler cannot work with the same data
        for (RetryableTaskEntity retryableTaskEntity : retryableTaskEntities) {
            retryableTaskEntity.setRetryTime(currentTime.plus(Duration.ofSeconds(properties.getTimeoutInSeconds())));
        }

        return retryableTaskEntities;
    }

    @Transactional
    public void markRetryableTasksAsCompleted(List<RetryableTaskEntity> retryableTaskEntities) {
        retryableTaskRepository.updateRetryableTasks(retryableTaskEntities, RetryableTaskStatus.SUCCESS);
    }

    @Transactional
    public void deleteRetryableTasksThanMoreWeek() {
        retryableTaskRepository.deleteAllRetryableTasksThatExpiredMoreWeek();
    }
}
