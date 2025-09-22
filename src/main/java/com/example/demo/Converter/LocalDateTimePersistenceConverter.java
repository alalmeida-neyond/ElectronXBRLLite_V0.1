package com.example.demo.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import com.example.demo.Resources.Constants;

@Converter
public class LocalDateTimePersistenceConverter implements AttributeConverter<LocalDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime entityValue) {
        if (entityValue == null) {
            return null;
        }
        return Timestamp.valueOf(entityValue);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        return LocalDateTime.ofInstant(databaseValue.toInstant(), Constants.LISBON);
    }
}
