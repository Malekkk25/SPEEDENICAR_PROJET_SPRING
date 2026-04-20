package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.student.entity.AcademicRecord;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.AcademicRecordRepository;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AcademicRecordService {

    private final AcademicRecordRepository academicRecordRepository;
    private final StudentProfileRepository studentProfileRepository;

    // ── Récupérer les notes (avec filtre semestre optionnel) ─

    @Transactional(readOnly = true)
    public List<AcademicRecord> getGrades(Long userId, String semester) {
        StudentProfile student = getStudentByUserId(userId);

        if (semester != null && !semester.isBlank()) {
            return academicRecordRepository
                    .findByStudentIdAndSemesterAndDeletedFalse(student.getId(), semester);
        }

        return academicRecordRepository
                .findByStudentIdAndDeletedFalseOrderBySemesterAsc(student.getId());
    }

    // ── Calculer la moyenne générale ─────────────────────────

    @Transactional(readOnly = true)
    public double getAverage(Long userId, String semester) {
        StudentProfile student = getStudentByUserId(userId);
        return student.getAverageGrade(semester);
    }

    // ── Helper privé ─────────────────────────────────────────

    private StudentProfile getStudentByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));
    }
}