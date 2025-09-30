package com.example.demo.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {
  private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE;
  @Override public String convertToDatabaseColumn(LocalDate v){ return v==null?null:F.format(v); }
  @Override public LocalDate convertToEntityAttribute(String v){ return v==null?null:LocalDate.parse(v, F); }
}
