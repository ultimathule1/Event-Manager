package dev.eventmanager.events.db;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.ZoneOffset;

@Converter(autoApply = true)
public class ZoneOffsetConverter implements AttributeConverter<ZoneOffset, String> {
    @Override
    public String convertToDatabaseColumn(ZoneOffset offset) {
        if (offset == null) {
            return null;
        }
        return offset.toString();
    }

    @Override
    public ZoneOffset convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }

        return ZoneOffset.of(s);
    }
}
