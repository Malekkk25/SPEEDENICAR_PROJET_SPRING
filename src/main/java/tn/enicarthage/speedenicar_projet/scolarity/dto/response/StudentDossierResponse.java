package tn.enicarthage.speedenicar_projet.scolarity.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class StudentDossierResponse {
    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String level;
    private Integer enrollmentYear;
    private List<MedicalDocumentResponse> pendingDocuments;
    private List<AbsenceResponse> prolongedAbsences;
    private List<AcademicRecordResponse> grades;
    private long unjustifiedAbsencesCount;
    private double averageGrade;
}