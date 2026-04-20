package tn.enicarthage.speedenicar_projet.scolarity.dto.response;

import lombok.Builder;
import lombok.Data;
import tn.enicarthage.speedenicar_projet.common.enums.DocStatus;
import java.time.LocalDateTime;

@Data @Builder
public class MedicalDocumentResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private DocStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime validationDate;
}