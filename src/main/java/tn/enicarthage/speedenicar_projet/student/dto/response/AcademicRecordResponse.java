package tn.enicarthage.speedenicar_projet.student.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicRecordResponse {

    private Long id;
    private String subject;
    private Double grade;
    private Double maxGrade;
    private Double percentage;
    private Boolean isPassing;
    private String semester;
    private String academicYear;
    private Double coefficient;
}