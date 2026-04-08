package tn.enicarthage.speedenicar_projet.scolarity.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data @Builder
public class AbsenceResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Boolean justified;
    private long durationDays;
    private boolean prolonged;
}