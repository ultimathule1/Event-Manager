package dev.eventmanager.retryable_task;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "retryable.task")
public class RetryableTaskProperties {
    private Integer limit;
    private Integer timeoutInSeconds;
}
