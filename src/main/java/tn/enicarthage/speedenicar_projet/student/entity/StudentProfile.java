package tn.enicarthage.speedenicar_projet.student.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.module_psychologue.document.MedicalDocument;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_profiles", indexes = {
        @Index(name = "idx_student_id", columnList = "student_id", unique = true),
        @Index(name = "idx_student_department", columnList = "department"),
        @Index(name = "idx_student_level", columnList = "level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Column(name = "student_id", nullable = false, unique = true, length = 30)
    private String studentId;

    @Column(name = "date_of_birth", columnDefinition = "TEXT")
    private LocalDate dateOfBirth;

    @Column(length = 100)
    private String department;

    @Column(length = 10)
    private String level;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;
    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date DESC")
    private List<MoodEntry> moodEntries = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<JournalEntry> journalEntries = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcademicRecord> academicRecords = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateTime DESC")
    private List<Appointment> appointments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate DESC")
    private List<Absence> absences = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<MedicalDocument> medicalDocuments = new ArrayList<>();

   /* @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<DifficultyReport> difficultyReports = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Alert> alerts = new ArrayList<>();*/

    public void addMoodEntry(MoodEntry entry) {
        moodEntries.add(entry);
        entry.setStudent(this);
    }

    public void addJournalEntry(JournalEntry entry) {
        journalEntries.add(entry);
        entry.setStudent(this);
    }

    public void addAcademicRecord(AcademicRecord record) {
        academicRecords.add(record);
        record.setStudent(this);
    }

    public void addAbsence(Absence absence) {
        absences.add(absence);
        absence.setStudent(this);
    }

   /* public void addDifficultyReport(DifficultyReport report) {
        difficultyReports.add(report);
        report.setStudent(this);
    }*/
    public double getAverageGrade(String semester) {
        return academicRecords.stream()
                .filter(r -> Boolean.FALSE.equals(r.getDeleted()))
                .filter(r -> semester == null || semester.equals(r.getSemester()))
                .mapToDouble(r -> (r.getGrade() / r.getMaxGrade()) * 100)
                .average()
                .orElse(0.0);
    }

    public long countUnjustifiedAbsences() {
        return absences.stream()
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .filter(a -> Boolean.FALSE.equals(a.getJustified()))
                .count();
    }

   /* public long countActiveAlerts() {
        return alerts.stream()
                .filter(Alert::isActive)
                .count();
    }*/
}
