package tn.enicarthage.speedenicar_projet.student.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.MoodLevel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntryRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    private String title;

    @NotBlank(message = "Le contenu est obligatoire")
    private String content;

    private MoodLevel mood;
}