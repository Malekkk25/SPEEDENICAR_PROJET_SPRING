package tn.enicarthage.speedenicar_projet.psychologist.dto;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.AlertSeverity;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecordResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentDepartment;
    private String studentLevel;
    private LocalDate sessionDate;
    private String observations;
    private AlertSeverity riskLevel;
    private String recommendations;
    private Boolean followUpRequired;
    private LocalDate nextSessionDate;
    private Integer sessionDurationMinutes;
    private String interventions;
    private String studentProgress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
