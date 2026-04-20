package tn.enicarthage.speedenicar_projet.student.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsenceResponse {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Boolean justified;
    private String subject;
    private String reportedBy;
    private Long durationInDays;
    private Boolean isProlonged;
    private LocalDateTime createdAt;
}