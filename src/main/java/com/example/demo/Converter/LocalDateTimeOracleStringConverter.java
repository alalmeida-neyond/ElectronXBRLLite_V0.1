package com.example.demo.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Converter(autoApply = false)
public class LocalDateTimeOracleStringConverter implements AttributeConverter<LocalDateTime, String> {

    private static DateTimeFormatter ci(String pattern) {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()                  
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH);
    }

    private static final List<DateTimeFormatter> PARSE_FORMATS = Arrays.asList(
            ci("dd-MMM-yy hh.mm.ss.SSSSSS a"),          
            ci("dd-MMM-yy hh.mm.ss.SSS a"),             
            ci("dd-MMM-yy hh.mm.ss a")                  
    );

    private static final DateTimeFormatter WRITE_FORMAT =
            ci("dd-MMM-yy hh.mm.ss.SSSSSS a");

    @Override
    public String convertToDatabaseColumn(LocalDateTime v) {
        return v == null ? null : WRITE_FORMAT.format(v);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String v) {
        if (v == null) return null;
        for (DateTimeFormatter f : PARSE_FORMATS) {
            try { return LocalDateTime.parse(v.trim(), f); } catch (Exception ignore) {}
        }
        throw new IllegalArgumentException("Unparseable CREATIONDATE: " + v);
    }
}
