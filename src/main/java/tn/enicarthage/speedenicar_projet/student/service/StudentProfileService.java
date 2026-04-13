package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;

    // ── Récupérer le profil de l'étudiant connecté ──────────

    @Transactional(readOnly = true)
    public StudentProfile getProfile(Long userId) {
        return studentProfileRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));
    }

    // ── Récupérer le profil par studentId ───────────────────

    @Transactional(readOnly = true)
    public StudentProfile getProfileByStudentId(String studentId) {
        return studentProfileRepository.findByStudentIdWithUser(studentId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));
    }

    // ── Mettre à jour le profil ──────────────────────────────

    public StudentProfile updateProfile(Long userId, StudentProfile updated) {
        StudentProfile existing = getProfile(userId);

        if (updated.getDepartment() != null) {
            existing.setDepartment(updated.getDepartment());
        }
        if (updated.getLevel() != null) {
            existing.setLevel(updated.getLevel());
        }
        if (updated.getDateOfBirth() != null) {
            existing.setDateOfBirth(updated.getDateOfBirth());
        }

        return studentProfileRepository.save(existing);
    }

    // ── Vérifier si un studentId existe déjà ────────────────

    @Transactional(readOnly = true)
    public boolean studentIdExists(String studentId) {
        return studentProfileRepository.existsByStudentId(studentId);
    }

    // ── Récupérer le résumé académique ───────────────────────

    @Transactional(readOnly = true)
    public StudentProfile getProfileWithStats(Long userId) {
        StudentProfile student = getProfile(userId);

        // Ces méthodes sont déjà définies dans l'entité StudentProfile
        double average = student.getAverageGrade(null);
        long unjustifiedAbsences = student.countUnjustifiedAbsences();

        return student;
    }
}