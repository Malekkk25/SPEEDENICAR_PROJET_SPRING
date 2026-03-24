package tn.enicarthage.speedenicar_projet.psychologist.dto;

import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.AlertSeverity;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateRecordRequest {
    private String observations;
    private AlertSeverity riskLevel;
    private String recommendations;
    private Boolean followUpRequired;
    private LocalDate nextSessionDate;
    private Integer sessionDurationMinutes;
    private String interventions;
    private String studentProgress;
}
