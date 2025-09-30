package com.example.demo.Converter;

import java.sql.Timestamp;
import java.time.LocalDate;
import jakarta.persistence.*;


@Converter
public class LocalDatePersistenceConverter implements AttributeConverter<LocalDate, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(LocalDate localDate) {
//        return Optional.ofNullable(localDate.atStartOfDay())
//          .map(Timestamp::valueOf)
//          .orElse(null);
        
        return localDate == null ? null : Timestamp.valueOf(localDate.atStartOfDay());
    }

    @Override
    public LocalDate convertToEntityAttribute(Timestamp date) {
        return date == null ? null : date.toLocalDateTime().toLocalDate();
    }
}
