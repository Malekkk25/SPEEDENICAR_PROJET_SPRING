package tn.enicarthage.speedenicar_projet.student.dto.response;

import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.MoodLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodEntryResponse {

    private Long id;
    private Integer moodLevel;
    private MoodLevel moodLabel;
    private String emoji;
    private String note;
    private LocalDate date;
    private String activities;
    private LocalDateTime createdAt;
}