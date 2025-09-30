package com.example.demo.Converter;

import java.time.LocalDate;
import jakarta.persistence.*;



@Converter(autoApply = false)
public class LocalDatePersistenceConverter implements AttributeConverter<LocalDate, String> {
  @Override public String convertToDatabaseColumn(LocalDate attribute) {
    return attribute == null ? null : attribute.toString();
  }
  @Override public LocalDate convertToEntityAttribute(String dbData) {
    return (dbData == null || dbData.isBlank()) ? null : LocalDate.parse(dbData);
  }
}
