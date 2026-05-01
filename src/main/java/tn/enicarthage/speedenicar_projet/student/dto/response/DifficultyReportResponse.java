package tn.enicarthage.speedenicar_projet.student.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DifficultyReportResponse {
    private Long id;
    private String type;
    private String description;
    private String urgency;
    private String status;
    private LocalDateTime createdAt;
}
