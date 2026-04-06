package tn.enicarthage.speedenicar_projet.module_psychologue.dto;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimeSlotResponse {
    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean available;
    private Integer durationMinutes;
}
