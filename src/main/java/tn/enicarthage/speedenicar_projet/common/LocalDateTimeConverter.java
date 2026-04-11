package tn.enicarthage.speedenicar_projet.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
    private static final DateTimeFormatter FORMATTER_SHORT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String convertToDatabaseColumn(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(FORMATTER);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return LocalDateTime.parse(dbData, FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dbData, FORMATTER_SHORT);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}