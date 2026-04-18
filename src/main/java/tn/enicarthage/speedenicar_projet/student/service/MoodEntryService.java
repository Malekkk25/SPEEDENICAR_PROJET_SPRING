package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.student.entity.MoodEntry;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.MoodEntryRepository;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MoodEntryService {

    private final MoodEntryRepository moodEntryRepository;
    private final StudentProfileRepository studentProfileRepository;

    // ── Créer une entrée d'humeur ────────────────────────────

    public MoodEntry createMood(Long userId, MoodEntry moodEntry) {
        StudentProfile student = getStudentByUserId(userId);

        // Vérifier qu'il n'y a pas déjà une entrée pour aujourd'hui
        if (moodEntryRepository.existsByStudentIdAndDateAndDeletedFalse(student.getId(), moodEntry.getDate())) {
            throw new IllegalStateException("Une entrée d'humeur existe déjà pour aujourd'hui");
        }

        moodEntry.setStudent(student);
        return moodEntryRepository.save(moodEntry);
    }

    // ── Historique paginé ────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<MoodEntry> getMoods(Long userId, Pageable pageable) {
        StudentProfile student = getStudentByUserId(userId);
        return moodEntryRepository
                .findByStudentIdAndDeletedFalseOrderByDateDesc(student.getId(), pageable);
    }

    // ── Statistiques (semaine / mois) ────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> getMoodStats(Long userId, String period) {
        StudentProfile student = getStudentByUserId(userId);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = period.equals("week")
                ? endDate.minusWeeks(1)
                : endDate.minusMonths(1);

        Double average = moodEntryRepository
                .getAverageMood(student.getId(), startDate, endDate);

        Long negativeCount = moodEntryRepository
                .countNegativeMoods(student.getId(), startDate, endDate);

        List<MoodEntry> entries = moodEntryRepository
                .findByStudentIdAndDateBetween(student.getId(), startDate, endDate);

        List<Object[]> distribution = moodEntryRepository
                .getMoodDistribution(student.getId(), startDate, endDate);

        return Map.of(
                "average", average != null ? average : 0.0,
                "negativeCount", negativeCount,
                "totalEntries", entries.size(),
                "distribution", distribution,
                "period", period,
                "startDate", startDate,
                "endDate", endDate
        );
    }

    // ── Supprimer une entrée (soft delete) ───────────────────

    public void deleteMood(Long userId, Long moodId) {
        StudentProfile student = getStudentByUserId(userId);

        MoodEntry mood = moodEntryRepository.findById(moodId)
                .orElseThrow(() -> new RuntimeException("Entrée d'humeur introuvable"));

        // Vérifier que l'entrée appartient bien à cet étudiant
        if (!mood.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Accès refusé à cette entrée");
        }

        mood.setDeleted(true);
        moodEntryRepository.save(mood);
    }

    // ── Helper privé ─────────────────────────────────────────

    private StudentProfile getStudentByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));
    }
}