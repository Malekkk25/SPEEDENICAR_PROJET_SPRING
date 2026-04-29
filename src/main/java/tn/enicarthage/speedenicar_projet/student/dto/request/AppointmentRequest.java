package tn.enicarthage.speedenicar_projet.student.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

// Ce que l'étudiant envoie pour réserver
@Data
public class AppointmentRequest {
    private Long psychologistId;
    private LocalDateTime dateTime; // 👈 Doit s'appeler exactement dateTime
    private String reason;
    private String type;
    private String locationType;
}


