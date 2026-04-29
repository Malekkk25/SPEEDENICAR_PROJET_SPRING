package tn.enicarthage.speedenicar_projet.student.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Ce que le backend renvoie pour la liste déroulante
@Data
@Builder
public class AvailableSlotResponse {
    private LocalDateTime dateTime; // 👈 Doit s'appeler exactement dateTime
    private Long psychologistId;
    private String psychologistName;
}
