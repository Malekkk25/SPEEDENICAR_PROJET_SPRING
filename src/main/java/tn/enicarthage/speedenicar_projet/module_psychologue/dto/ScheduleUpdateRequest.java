package tn.enicarthage.speedenicar_projet.module_psychologue.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleUpdateRequest {

    @NotEmpty(message = "La liste des créneaux ne peut pas être vide")
    @Valid
    private List<TimeSlotRequest> slots;
}
