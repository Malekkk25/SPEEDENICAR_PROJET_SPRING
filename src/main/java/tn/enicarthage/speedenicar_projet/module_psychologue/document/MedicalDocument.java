package tn.enicarthage.speedenicar_projet.module_psychologue.document;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
// Si tu utilises @SuperBuilder dans BaseEntity, remplace @Builder par @SuperBuilder ici aussi
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
@Builder // (Remplace par @SuperBuilder si BaseEntity utilise @SuperBuilder)
public class MedicalDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @NotBlank
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @NotBlank
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DocStatus status = DocStatus.PENDING;

    /**
     * Agent scolarité (ou psychologue) qui a validé/rejeté le document.
     * Null tant que le document est PENDING.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by")
    private User validatedBy;

    // ❌ CORRECTION 1 : Enlever columnDefinition = "TEXT"
    // Dans MySQL (ta base de prod) et H2 (ta base de test), LocalDateTime
    // se mappe automatiquement en TIMESTAMP. Le forcer en TEXT cause des bugs de lecture.
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