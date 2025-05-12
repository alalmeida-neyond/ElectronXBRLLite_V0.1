package com.example.demo.Converter;

import java.sql.Timestamp;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import com.example.demo.controller.Objects.Constants;


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
