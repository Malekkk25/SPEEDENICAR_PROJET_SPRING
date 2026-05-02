package tn.enicarthage.speedenicar_projet.student.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskScoreResponse {

    private Integer score;
    private String level;
    private String color;
    private String emoji;
    private List<String> factors;
    private String recommendation;

    // ── Niveaux de risque ────────────────────────────────────
    public static String getLevel(int score) {
        if (score <= 30) return "FAIBLE";
        if (score <= 50) return "MOYEN";
        if (score <= 75) return "ÉLEVÉ";
        return "CRITIQUE";
    }

    public static String getColor(int score) {
        if (score <= 30) return "#22c55e";
        if (score <= 50) return "#eab308";
        if (score <= 75) return "#f97316";
        return "#ef4444";
    }

    public static String getEmoji(int score) {
        if (score <= 30) return "🟢";
        if (score <= 50) return "🟡";
        if (score <= 75) return "🟠";
        return "🔴";
    }

    public static String getRecommendation(int score) {
        if (score <= 30) return "Tout va bien, continuez comme ça !";
        if (score <= 50) return "Quelques signaux à surveiller, pensez à consulter.";
        if (score <= 75) return "Situation préoccupante, une consultation est recommandée.";
        return "Situation critique, veuillez contacter le psychologue immédiatement.";
    }
}