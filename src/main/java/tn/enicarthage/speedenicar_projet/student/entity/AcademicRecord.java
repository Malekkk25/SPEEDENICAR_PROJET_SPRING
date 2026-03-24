package tn.enicarthage.speedenicar_projet.student.entity;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;

@Entity
@Table(name = "academic_records", indexes = {
        @Index(name = "idx_academic_student", columnList = "student_id"),
        @Index(name = "idx_academic_semester", columnList = "semester, academic_year")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String subject;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Double grade;

    @NotNull
    @Column(name = "max_grade", nullable = false)
    private Double maxGrade;

    @Column(length = 10)
    private String semester;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(nullable = false)
    @Builder.Default
    private Double coefficient = 1.0;

    // ─── Helpers ────────────────────────────────────────────────

    /**
     * Retourne le pourcentage de réussite (grade / maxGrade * 100).
     */
    public double getPercentage() {
        if (maxGrade == null || maxGrade == 0) return 0;
        return (grade / maxGrade) * 100;
    }

    /**
     * Vérifie si la note est au-dessus de la moyenne (>= 50%).
     */
    public boolean isPassing() {
        return getPercentage() >= 50.0;
    }
}

