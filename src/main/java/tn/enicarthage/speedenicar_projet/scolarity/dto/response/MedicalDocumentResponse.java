package tn.enicarthage.speedenicar_projet.scolarity.dto.response;

import lombok.Builder;
import lombok.Data;
import tn.enicarthage.speedenicar_projet.common.enums.DocStatus;
import java.time.LocalDateTime;


import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalDocumentResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String fileName;
    private String fileType;
    private Long fileSize;         // 👈 Indispensable pour ton formatFileSize Angular !
    private String status;         // PENDING, VALIDATED, etc.
    private String type;           // MEDICAL_CERTIFICATE, etc.
    private String rejectionReason;
    private LocalDateTime createdAt;      // Ou String si tu préfères formater côté backend
    private LocalDateTime validationDate; // Idem
}