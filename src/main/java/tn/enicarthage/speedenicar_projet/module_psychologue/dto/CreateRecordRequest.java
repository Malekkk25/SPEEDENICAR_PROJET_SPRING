package tn.enicarthage.speedenicar_projet.module_psychologue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.AlertSeverity;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateRecordRequest {

    @NotNull(message = "L'identifiant de l'étudiant est obligatoire")
    private Long studentId;

    @NotNull(message = "La date de session est obligatoire")
    private LocalDate sessionDate;

    @NotBlank(message = "Les observations sont obligatoires")
    private String observations;

    private AlertSeverity riskLevel;
    private String recommendations;
    private Boolean followUpRequired;
    private LocalDate nextSessionDate;
    private Integer sessionDurationMinutes;
    private String interventions;
    private String studentProgress;
}

