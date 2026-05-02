package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.student.dto.response.RiskScoreResponse;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskScoreService {

    private final StudentProfileRepository studentProfileRepository;
    private final MoodEntryRepository moodEntryRepository;
    private final AbsenceRepository absenceRepository;
    private final DifficultyReportRepository difficultyReportRepository;

    public RiskScoreResponse calculateRiskScore(Long userId) {
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        int score = 0;
        List<String> factors = new ArrayList<>();

        // ── Indicateur 1 : Humeur moyenne < 2/5 sur 7 jours (25 pts) ──
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        Double avgMood = moodEntryRepository.getAverageMood(
                student.getId(), weekAgo, today);

        if (avgMood != null && avgMood < 2.0) {
            score += 25;
            factors.add("Humeur moyenne très basse cette semaine (" +
                    String.format("%.1f", avgMood) + "/5)");
        }

        // ── Indicateur 2 : Pas d'entrée mood > 5 jours (20 pts) ──
        LocalDate fiveDaysAgo = today.minusDays(5);
        boolean hasMoodEntry = moodEntryRepository
                .existsByStudentIdAndDateAndDeletedFalse(
                        student.getId(), fiveDaysAgo);

        if (!hasMoodEntry) {
            Long negativeMoods = moodEntryRepository.countNegativeMoods(
                    student.getId(), fiveDaysAgo, today);
            if (negativeMoods == 0) {
                score += 20;
                factors.add("Aucune entrée mood tracker depuis plus de 5 jours");
            }
        }

        // ── Indicateur 3 : Absences non justifiées > 3 (20 pts) ──
        Long unjustifiedAbsences = absenceRepository
                .countUnjustified(student.getId());
        if (unjustifiedAbsences > 3) {
            score += 20;
            factors.add(unjustifiedAbsences + " absences non justifiées détectées");
        }

        // ── Indicateur 4 : Moyenne académique < 10/20 (15 pts) ──
        double average = student.getAverageGrade(null);
        if (average > 0 && average < 50.0) {
            score += 15;
            factors.add("Moyenne académique insuffisante (" +
                    String.format("%.1f", average * 20 / 100) + "/20)");
        }

        // ── Indicateur 5 : Signalement ouvert (10 pts) ──
        Long openReports = difficultyReportRepository
                .countByStudentIdAndStatusAndDeletedFalse(
                        student.getId(),
                        tn.enicarthage.speedenicar_projet.common.enums.ReportStatus.OPEN);
        if (openReports > 0) {
            score += 10;
            factors.add(openReports + " signalement(s) de difficulté en cours");
        }

        // ── Indicateur 6 : Pas de connexion > 7 jours (10 pts) ──
        LocalDateTime lastLogin = student.getUser().getLastLogin();
        if (lastLogin != null &&
                lastLogin.isBefore(LocalDateTime.now().minusDays(7))) {
            score += 10;
            factors.add("Aucune connexion à la plateforme depuis plus de 7 jours");
        }

        if (factors.isEmpty()) {
            factors.add("Aucun facteur de risque détecté");
        }

        return RiskScoreResponse.builder()
                .score(score)
                .level(RiskScoreResponse.getLevel(score))
                .color(RiskScoreResponse.getColor(score))
                .emoji(RiskScoreResponse.getEmoji(score))
                .factors(factors)
                .recommendation(RiskScoreResponse.getRecommendation(score))
                .build();
    }
    public RiskScoreResponse calculateRiskScoreByProfileId(Long profileId) {
        StudentProfile student = studentProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        // Réutilise la même logique
        Long userId = student.getUser().getId();
        return calculateRiskScore(userId);
    }
}