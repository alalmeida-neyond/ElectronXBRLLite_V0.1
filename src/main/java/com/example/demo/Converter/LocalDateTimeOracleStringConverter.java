package com.example.demo.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;

@Converter(autoApply = false)
public class LocalDateTimeOracleStringConverter implements AttributeConverter<LocalDateTime, String> {

    private static DateTimeFormatter fmt(DateTimeFormatterBuilder b) {
        return b.toFormatter(Locale.ENGLISH);
    }

    private static final DateTimeFormatter YMD_DOTS_NANO = fmt(new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH")
            .appendLiteral('.').appendPattern("mm")
            .appendLiteral('.').appendPattern("ss")
            .optionalStart()
                .appendLiteral('.')
                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, false)
            .optionalEnd()
    );

    private static final DateTimeFormatter YMD_COLONS_NANO = fmt(new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
                .appendLiteral('.')
                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, false)
            .optionalEnd()
    );

    private static final DateTimeFormatter ORA_12H_WITH_FRACTION = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd-MMM-yy hh.mm.ss")
            .optionalStart().appendLiteral('.').appendPattern("SSSSSS").optionalEnd()
            .appendLiteral(' ')
            .appendPattern("a")
            .toFormatter(Locale.ENGLISH);

    private static final DateTimeFormatter ORA_12H = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd-MMM-yy hh.mm.ss a")
            .toFormatter(Locale.ENGLISH);

    private static final DateTimeFormatter WRITE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);

    private static final List<DateTimeFormatter> CANDIDATES = List.of(
            YMD_DOTS_NANO,
            YMD_COLONS_NANO,
            ORA_12H_WITH_FRACTION,
            ORA_12H
    );

    @Override
    public String convertToDatabaseColumn(LocalDateTime v) {
        return v == null ? null : WRITE_FORMAT.format(v);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String v) {
        if (v == null) return null;
        final String s = v.trim();
        for (DateTimeFormatter f : CANDIDATES) {
            try {
                return LocalDateTime.parse(s, f);
            } catch (Exception ignore) {}
        }
        throw new IllegalArgumentException("Unsupported date format in DB: " + v);
    }
}
