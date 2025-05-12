package com.example.demo.Converter;

import java.time.LocalDate;
import jakarta.persistence.*;

/**
 * Converter to provide Java 8 Date/Time API Support to JPA
 */
@Converter
//public class LocalDatePersistenceConverter implements AttributeConverter<LocalDate, Timestamp> {
public class LocalDatePersistenceConverter implements AttributeConverter<LocalDate, String> {

    /*@Override
    public Timestamp convertToDatabaseColumn(LocalDate localDate) {
            return Optional.ofNullable(localDate.atStartOfDay()).map(Timestamp::valueOf).orElse(null);
        
        //return localDate == null ? null : Timestamp.valueOf(localDate.atStartOfDay());
    }*/

    @Override
    public String convertToDatabaseColumn(LocalDate localDate) {
            return localDate.toString();
        
        //return localDate == null ? null : Timestamp.valueOf(localDate.atStartOfDay());
    }

    /*@Override
    public LocalDate convertToEntityAttribute(Timestamp date) {
        return date == null ? null : date.toLocalDateTime().toLocalDate();
    }*/

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        return dbData != null ? LocalDate.parse(dbData) : null;
    }
}
