package com.example.demo.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.*;

@Converter(autoApply = false)
public class EpochMillisToLocalDateTimeReadOnly
        implements AttributeConverter<LocalDateTime, Long> {

    // Not used because the field is insertable=false, updatable=false
    @Override
    public Long convertToDatabaseColumn(LocalDateTime attribute) {
        return null; // or throw UnsupportedOperationException if you prefer
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long dbData) {
        if (dbData == null) return null;
        return Instant.ofEpochMilli(dbData)
                      .atZone(ZoneId.systemDefault())
                      .toLocalDateTime();
    }
}
