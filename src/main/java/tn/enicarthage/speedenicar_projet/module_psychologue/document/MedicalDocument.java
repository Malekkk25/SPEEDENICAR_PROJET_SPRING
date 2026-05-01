package tn.enicarthage.speedenicar_projet.module_psychologue.document;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
// Si tu utilises @SuperBuilder dans BaseEntity, remplace @Builder par @SuperBuilder ici aussi
import lombok.experimental.SuperBuilder;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.common.enums.DocStatus;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_documents", indexes = {
        @Index(name = "idx_doc_student", columnList = "student_id"),
        @Index(name = "idx_doc_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = true)  // ← CHANGÉ !
    private User uploadedBy;

    @NotBlank
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @NotBlank
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;  // chemin relatif uniquement

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)// ← avec @SuperBuilder
    private DocStatus status = DocStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by")
    private User validatedBy;

    @Column(name = "validation_date")
    private LocalDateTime validationDate;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    public void validate(User agent) {
        this.status = DocStatus.VALIDATED;
        this.validatedBy = agent;
        this.validationDate = LocalDateTime.now();
    }

    public void reject(User agent, String reason) {
        this.status = DocStatus.REJECTED;
        this.validatedBy = agent;
        this.validationDate = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    public boolean isPending() {
        return DocStatus.PENDING.equals(this.status);
    }
}