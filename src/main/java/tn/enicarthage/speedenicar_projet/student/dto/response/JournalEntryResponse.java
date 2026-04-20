package tn.enicarthage.speedenicar_projet.student.dto.response;

import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.MoodLevel;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntryResponse {

    private Long id;
    private String title;
    private String content;
    private MoodLevel mood;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}