package tn.enicarthage.speedenicar_projet.module_psychologue.dto;

import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentStatus;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentDepartment;
    private Long psychologistId;
    private String psychologistName;
    private LocalDateTime dateTime;
    private Integer duration;
    private AppointmentStatus status;
    private AppointmentType type;
    private String reason;
    private String notes;
    private String cancellationReason;
    private LocalDateTime createdAt;
}
