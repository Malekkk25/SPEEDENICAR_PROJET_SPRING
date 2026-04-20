package tn.enicarthage.speedenicar_projet.scolarity.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AcademicRecordResponse {
    private Long id;
    private String subject;
    private Double grade;
    private Double maxGrade;
    private double percentage;
    private String semester;
    private String academicYear;
    private boolean passing;

}