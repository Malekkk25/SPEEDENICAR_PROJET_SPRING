package tn.enicarthage.speedenicar_projet.module_psychologue.dto;

import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.AlertSeverity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentAlertResponse {
    private Long studentId;
    private String studentName;
    private String department;
    private String level;
    private AlertSeverity currentRiskLevel;
    private LocalDate lastSessionDate;
    private Double recentMoodAverage;
    private Long unjustifiedAbsences;
    private Double academicAverage;
    private Boolean followUpRequired;
    private List<String> alertReasons;
    private LocalDateTime lastAlertDate;
}

