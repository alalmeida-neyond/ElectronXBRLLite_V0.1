package com.example.demo.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Converter(autoApply = false)
public class LocalDateOracleStringConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter DD_MMM_YY =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd-MMM-")
            .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
            .toFormatter(Locale.ENGLISH);

    private static final DateTimeFormatter DD_MMM_YYYY =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd-MMM-uuuu")
            .toFormatter(Locale.ENGLISH);

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final List<DateTimeFormatter> PARSE_FORMATS = Arrays.asList(
        DD_MMM_YY,
        DD_MMM_YYYY,
        ISO
    );

    private static final DateTimeFormatter WRITE_FORMAT = DD_MMM_YY;

    @Override
    public String convertToDatabaseColumn(LocalDate v) {
        return v == null ? null : WRITE_FORMAT.format(v);
    }

    @Override
    public LocalDate convertToEntityAttribute(String v) {
        if (v == null) return null;
        String s = v.trim();
        for (DateTimeFormatter f : PARSE_FORMATS) {
            try { return LocalDate.parse(s, f); } catch (Exception ignore) {}
        }
        throw new IllegalArgumentException("Unparseable LocalDate: " + v);
    }
}
