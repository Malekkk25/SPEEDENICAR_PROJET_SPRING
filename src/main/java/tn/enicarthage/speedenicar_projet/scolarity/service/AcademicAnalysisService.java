package tn.enicarthage.speedenicar_projet.scolarity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.enicarthage.speedenicar_projet.common.enums.RiskLevel;
import tn.enicarthage.speedenicar_projet.scolarity.dto.response.AcademicAnalysisResult;
import tn.enicarthage.speedenicar_projet.student.entity.Absence;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicAnalysisService {

    private final StudentProfileRepository studentRepo;

    // ═══════════════════════════════════════════
    // ANALYSER UN SEUL ÉTUDIANT
    // ═══════════════════════════════════════════

    public AcademicAnalysisResult analyzeStudent(Long studentId) {
        StudentProfile student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException(
                        "Étudiant introuvable : " + studentId));
        return buildResult(student);
    }

    // ═══════════════════════════════════════════
    // ANALYSER TOUS LES ÉTUDIANTS À RISQUE
    // ═══════════════════════════════════════════

    public List<AcademicAnalysisResult> analyzeAllAtRisk() {
        return studentRepo.findAll().stream()
                .filter(s -> Boolean.FALSE.equals(s.getDeleted()))
                .map(this::buildResult)
                .filter(r -> r.getRiskLevel() != RiskLevel.LOW)
                .sorted((a, b) ->
                        Double.compare(b.getRiskScore(), a.getRiskScore()))
                .toList();
    }

    // ═══════════════════════════════════════════
    // CONSTRUIRE LE RÉSULTAT POUR UN ÉTUDIANT
    // ═══════════════════════════════════════════

    private AcademicAnalysisResult buildResult(StudentProfile student) {

        // 1. Moyenne générale en % (utilise la méthode de ton amie)
        double averagePercent = student.getAverageGrade(null);

        // 2. Absences injustifiées (méthode de ton amie)
        long unjustifiedAbsences = student.countUnjustifiedAbsences();

        // 3. Total jours d'absence
        long totalAbsenceDays = student.getAbsences().stream()
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .mapToLong(Absence::getDurationInDays)
                .sum();

        // 4. Score IA basé sur notes + absences uniquement
        double score = calculateRiskScore(
                averagePercent, unjustifiedAbsences, totalAbsenceDays);

        RiskLevel level = determineRiskLevel(score);

        log.info("IA — {} {} : score={} niveau={}",
                student.getUser().getFirstName(),
                student.getUser().getLastName(),
                Math.round(score), level);

        return AcademicAnalysisResult.builder()
                .studentId(student.getId())
                .studentName(student.getUser().getFirstName()
                        + " " + student.getUser().getLastName())
                .studentEmail(student.getUser().getEmail())
                .riskScore(Math.round(score * 10.0) / 10.0)
                .riskLevel(level)
                .averageGradePercent(Math.round(averagePercent * 10.0) / 10.0)
                .unjustifiedAbsences(unjustifiedAbsences)
                .totalAbsenceDays(totalAbsenceDays)
                .alerts(generateAlerts(
                        averagePercent, unjustifiedAbsences, totalAbsenceDays))
                .recommendations(generateRecommendations(level))
                .build();
    }

    // ═══════════════════════════════════════════
    // ALGORITHME IA — CALCUL DU SCORE (0-100)
    // ═══════════════════════════════════════════

    private double calculateRiskScore(double averagePercent,
                                      long unjustifiedAbsences,
                                      long totalAbsenceDays) {
        double score = 0;

        // --- FACTEUR 1 : Notes académiques (60% du score) ---
        if (averagePercent == 0) {
            score += 30;  // pas de notes enregistrées
        } else if (averagePercent < 30) {
            score += 60;  // très critique
        } else if (averagePercent < 50) {
            score += 40;  // insuffisant
        } else if (averagePercent < 65) {
            score += 20;  // passable
        } else if (averagePercent < 80) {
            score += 8;   // bien
        }
        // >= 80 → excellent, +0

        // --- FACTEUR 2 : Absences injustifiées (25% du score) ---
        if (unjustifiedAbsences >= 10) {
            score += 25;
        } else if (unjustifiedAbsences >= 5) {
            score += 15;
        } else if (unjustifiedAbsences >= 3) {
            score += 8;
        } else if (unjustifiedAbsences >= 1) {
            score += 3;
        }

        // --- FACTEUR 3 : Total jours absence (15% du score) ---
        if (totalAbsenceDays >= 20) {
            score += 15;
        } else if (totalAbsenceDays >= 10) {
            score += 10;
        } else if (totalAbsenceDays >= 5) {
            score += 5;
        }

        return Math.min(score, 100);
    }

    // ═══════════════════════════════════════════
    // NIVEAU DE RISQUE
    // ═══════════════════════════════════════════

    private RiskLevel determineRiskLevel(double score) {
        if (score >= 81) return RiskLevel.CRITICAL;
        if (score >= 61) return RiskLevel.HIGH;
        if (score >= 31) return RiskLevel.MODERATE;
        return RiskLevel.LOW;
    }

    // ═══════════════════════════════════════════
    // ALERTES GÉNÉRÉES AUTOMATIQUEMENT
    // ═══════════════════════════════════════════

    private List<String> generateAlerts(double avg,
                                        long absJust,
                                        long totalDays) {
        List<String> alerts = new ArrayList<>();

        if (avg > 0 && avg < 30) {
            alerts.add("CRITIQUE : Moyenne de " + Math.round(avg)
                    + "% — risque d'échec très élevé");
        } else if (avg > 0 && avg < 50) {
            alerts.add("ATTENTION : Moyenne insuffisante ("
                    + Math.round(avg) + "%)");
        }

        if (absJust >= 10) {
            alerts.add("CRITIQUE : " + absJust
                    + " absences injustifiées — absentéisme sévère");
        } else if (absJust >= 5) {
            alerts.add("ATTENTION : " + absJust
                    + " absences injustifiées détectées");
        } else if (absJust >= 3) {
            alerts.add("INFO : " + absJust
                    + " absences injustifiées à surveiller");
        }

        if (totalDays >= 20) {
            alerts.add("ATTENTION : " + totalDays
                    + " jours d'absence au total");
        }

        if (alerts.isEmpty()) {
            alerts.add("OK : Aucune alerte — étudiant dans les normes");
        }

        return alerts;
    }

    // ═══════════════════════════════════════════
    // RECOMMANDATIONS SELON LE NIVEAU
    // ═══════════════════════════════════════════

    private List<String> generateRecommendations(RiskLevel level) {
        return switch (level) {
            case CRITICAL -> List.of(
                    "Convoquer l'étudiant immédiatement",
                    "Contacter le référent pédagogique en urgence",
                    "Orienter vers le psychologue",
                    "Mettre en place un plan de rattrapage personnalisé"
            );
            case HIGH -> List.of(
                    "Planifier un entretien avec le référent pédagogique",
                    "Proposer un suivi hebdomadaire",
                    "Signaler à la cellule SPEED",
                    "Proposer des séances de tutorat"
            );
            case MODERATE -> List.of(
                    "Surveiller l'évolution sur 2 semaines",
                    "Envoyer un message d'encouragement",
                    "Demander des justificatifs d'absence si nécessaire"
            );
            case LOW -> List.of(
                    "Continuer le suivi régulier",
                    "Aucune action urgente requise"
            );
        };
    }
}