package tn.enicarthage.speedenicar_projet.student.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.common.enums.DifficultyType;
import tn.enicarthage.speedenicar_projet.common.enums.UrgencyLevel;
import tn.enicarthage.speedenicar_projet.common.enums.ReportStatus;
import tn.enicarthage.speedenicar_projet.user.entity.User;

@Entity
@Table(name = "difficulty_reports", indexes = {
        @Index(name = "idx_report_student", columnList = "student_id"),
        @Index(name = "idx_report_status", columnList = "status"),
        @Index(name = "idx_report_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DifficultyReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DifficultyType type;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UrgencyLevel urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    // ── Business methods ─────────────────────────────────────

    public boolean isOpen() {
        return ReportStatus.OPEN.equals(this.status);
    }

    public boolean isResolved() {
        return ReportStatus.RESOLVED.equals(this.status);
    }

    public boolean isCritical() {
        return UrgencyLevel.CRITICAL.equals(this.urgency);
    }
}