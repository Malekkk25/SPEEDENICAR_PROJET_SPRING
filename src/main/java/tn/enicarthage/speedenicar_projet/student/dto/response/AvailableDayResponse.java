package tn.enicarthage.speedenicar_projet.student.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AvailableDayResponse {
    private LocalDate date;
    private String dayName; // Lundi, Mardi...
    private List<AvailableSlotResponse> slots;
}
