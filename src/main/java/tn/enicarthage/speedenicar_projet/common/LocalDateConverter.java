package tn.enicarthage.speedenicar_projet.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;

@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate date) {
        return date == null ? null : date.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return LocalDate.parse(dbData.length() > 10 ? dbData.substring(0, 10) : dbData);
        } catch (Exception e) {
            return null;
        }
    }
}