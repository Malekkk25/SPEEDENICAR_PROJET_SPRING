package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.student.entity.Absence;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.AbsenceRepository;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final StudentProfileRepository studentProfileRepository;

    // ── Récupérer les absences paginées ──────────────────────

    @Transactional(readOnly = true)
    public Page<Absence> getAbsences(Long userId, Pageable pageable) {
        StudentProfile student = getStudentByUserId(userId);
        return absenceRepository
                .findByStudentIdAndDeletedFalseOrderByStartDateDesc(student.getId(), pageable);
    }

    // ── Récupérer les absences non justifiées ────────────────

    @Transactional(readOnly = true)
    public List<Absence> getUnjustifiedAbsences(Long userId) {
        StudentProfile student = getStudentByUserId(userId);
        return absenceRepository
                .findByStudentIdAndJustifiedFalseAndDeletedFalse(student.getId());
    }

    // ── Récupérer les absences par période ───────────────────

    @Transactional(readOnly = true)
    public List<Absence> getAbsencesByPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        StudentProfile student = getStudentByUserId(userId);
        return absenceRepository
                .findByStudentIdAndPeriod(student.getId(), startDate, endDate);
    }

    // ── Compter les absences non justifiées ──────────────────

    @Transactional(readOnly = true)
    public Long countUnjustifiedAbsences(Long userId) {
        StudentProfile student = getStudentByUserId(userId);
        return absenceRepository.countUnjustified(student.getId());
    }

    // ── Vérifier si l'étudiant a des absences prolongées ────

    @Transactional(readOnly = true)
    public boolean hasProlongedAbsences(Long userId) {
        StudentProfile student = getStudentByUserId(userId);
        List<Absence> unjustified = absenceRepository
                .findByStudentIdAndJustifiedFalseAndDeletedFalse(student.getId());
        return unjustified.stream().anyMatch(Absence::isProlonged);
    }

    // ── Helper privé ─────────────────────────────────────────

    private StudentProfile getStudentByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));
    }
}