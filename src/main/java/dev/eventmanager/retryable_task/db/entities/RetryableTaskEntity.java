package dev.eventmanager.retryable_task.db.entities;

import dev.eventmanager.retryable_task.RetryableTaskStatus;
import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.converter.RetryableTaskStatusConverter;
import dev.eventmanager.retryable_task.converter.RetryableTaskTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class RetryableTaskEntity implements Serializable {
    @Id
    private UUID id;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String payload;

    @Convert(converter = RetryableTaskTypeConverter.class)
    private RetryableTaskType type;

    @Convert(converter = RetryableTaskStatusConverter.class)
    private RetryableTaskStatus status;

    private Instant retryTime;
}