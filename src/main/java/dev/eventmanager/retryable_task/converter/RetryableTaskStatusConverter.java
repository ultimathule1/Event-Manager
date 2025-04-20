package dev.eventmanager.retryable_task.converter;

import dev.eventmanager.retryable_task.RetryableTaskStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RetryableTaskStatusConverter implements AttributeConverter<RetryableTaskStatus, String> {


    @Override
    public String convertToDatabaseColumn(RetryableTaskStatus retryableTaskStatus) {
        if (retryableTaskStatus == null) {
            return null;
        }

        return retryableTaskStatus.toString();
    }

    @Override
    public RetryableTaskStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return RetryableTaskStatus.fromValue(dbData);
    }
}
