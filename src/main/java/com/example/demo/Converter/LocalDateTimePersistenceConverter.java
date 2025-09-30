package com.example.demo.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.example.demo.Resources.Constants;

@Converter(autoApply = true)
public class LocalDateTimePersistenceConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String convertToDatabaseColumn(LocalDateTime entityValue) {
        if (entityValue == null) return null;
        return String.valueOf(entityValue.atZone(Constants.LISBON).toInstant().toEpochMilli());
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        try {
            long epochMillis = Long.parseLong(dbData);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), Constants.LISBON);
        } catch (NumberFormatException ignored) { }

        try {
            return LocalDateTime.parse(dbData, DB_FORMATTER);
        } catch (Exception ignored) { }

        try {
            return LocalDateTime.parse(dbData);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unsupported date format in DB: " + dbData, ex);
        }
    }
}