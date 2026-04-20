package tn.enicarthage.speedenicar_projet.student.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodStatsResponse {

    private Double average;
    private Long negativeCount;
    private Integer totalEntries;
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<MoodDistribution> distribution;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MoodDistribution {
        private Integer moodLevel;
        private Long count;
    }
}