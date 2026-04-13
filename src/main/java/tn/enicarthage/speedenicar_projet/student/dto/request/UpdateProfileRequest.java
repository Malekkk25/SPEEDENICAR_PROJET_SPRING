package tn.enicarthage.speedenicar_projet.student.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(max = 100, message = "Le département ne peut pas dépasser 100 caractères")
    private String department;

    @Size(max = 10, message = "Le niveau ne peut pas dépasser 10 caractères")
    private String level;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateOfBirth;
}