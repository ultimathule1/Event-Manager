package dev.eventmanager.retryable_task.converter;

import dev.eventmanager.retryable_task.RetryableTaskType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RetryableTaskTypeConverter implements AttributeConverter<RetryableTaskType, String> {

    @Override
    public String convertToDatabaseColumn(RetryableTaskType retryableTaskType) {
        if (retryableTaskType == null) {
            return  null;
        }
        return retryableTaskType.getValue();
    }

    @Override
    public RetryableTaskType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        return RetryableTaskType.fromValue(dbData);
    }
}
