package tn.enicarthage.speedenicar_projet.student.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.MoodLevel;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodEntryRequest {

    @NotNull(message = "Le niveau d'humeur est obligatoire")
    @Min(value = 1, message = "Le niveau minimum est 1")
    @Max(value = 5, message = "Le niveau maximum est 5")
    private Integer moodLevel;

    private MoodLevel moodLabel;

    @Size(max = 10, message = "L'emoji ne peut pas dépasser 10 caractères")
    private String emoji;

    private String note;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    private String activities; // JSON string ["sport", "lecture"]
}