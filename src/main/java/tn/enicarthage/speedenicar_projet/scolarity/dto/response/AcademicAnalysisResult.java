package tn.enicarthage.speedenicar_projet.scolarity.dto.response;

import lombok.Builder;
import lombok.Data;
import tn.enicarthage.speedenicar_projet.common.enums.RiskLevel;
import java.util.List;

@Data @Builder
public class AcademicAnalysisResult {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private double riskScore;              // 0 à 100
    private RiskLevel riskLevel;           // LOW/MODERATE/HIGH/CRITICAL
    private double averageGradePercent;    // moyenne en %
    private long unjustifiedAbsences;      // nb absences injustifiées
    private long totalAbsenceDays;         // total jours absence
    private List<String> alerts;           // alertes détectées
    private List<String> recommendations;  // conseils
}