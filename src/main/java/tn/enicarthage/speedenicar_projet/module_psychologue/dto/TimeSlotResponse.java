package tn.enicarthage.speedenicar_projet.module_psychologue.dto;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimeSlotResponse {
    private Long id;
    private Long psychologistId;      // REQUIS PAR LE FRONT
    private String psychologistName;  // REQUIS PAR LE FRONT
    private LocalDateTime dateTime;   // REQUIS PAR LE FRONT
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean available;
    private Integer durationMinutes;
}
