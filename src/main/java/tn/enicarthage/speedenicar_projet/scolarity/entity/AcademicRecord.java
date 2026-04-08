package tn.enicarthage.speedenicar_projet.scolarity.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;

@Entity
@Table(name = "academic_records", indexes = {
        @Index(name = "idx_academic_student", columnList = "student_id"),
        @Index(name = "idx_academic_semester", columnList = "semester")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AcademicRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String subject;

    @NotNull
    @Column(nullable = false)
    private Double grade;

    @NotNull
    @Column(name = "max_grade", nullable = false)
    private Double maxGrade;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String semester;        // S1, S2

    @NotBlank
    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;    // 2025-2026

    @Column(length = 200)
    private String comments;

    public double getPercentage() {
        if (maxGrade == null || maxGrade == 0) return 0;
        return (grade / maxGrade) * 100;
    }

    public boolean isPassing() {
        return getPercentage() >= 50;
    }
}