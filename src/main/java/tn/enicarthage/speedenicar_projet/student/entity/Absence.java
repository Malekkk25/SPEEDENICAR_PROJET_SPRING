package tn.enicarthage.speedenicar_projet.student.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.document.MedicalDocument;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "absences", indexes = {
        @Index(name = "idx_absence_student", columnList = "student_id"),
        @Index(name = "idx_absence_dates", columnList = "start_date, end_date"),
        @Index(name = "idx_absence_justified", columnList = "justified")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absence extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 300)
    private String reason;

    @Column(nullable = false)
    @Builder.Default
    private Boolean justified = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private MedicalDocument document;

    @Column(length = 100)
    private String subject;

    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    // ── Business methods ────────────────────────────────────

    public long getDurationInDays() {
        if (startDate == null || endDate == null) return 0;
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public boolean isProlonged() {
        return getDurationInDays() > 3;
    }

    public boolean isOngoing() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public void justify(MedicalDocument doc) {
        this.document = doc;
        this.justified = true;
    }

    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "La date de fin ne peut pas être antérieure à la date de début"
            );
        }
    }
}

