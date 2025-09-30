package com.example.demo.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.*;

@Converter(autoApply = false) // set true only if ALL your LDTs use epoch millis
public class LocalDateTimeEpochMillisConverter
        implements AttributeConverter<LocalDateTime, Long> {

    @Override
    public Long convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) return null;
        return attribute.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long dbData) {
        if (dbData == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(dbData), ZoneOffset.UTC);
    }
}
