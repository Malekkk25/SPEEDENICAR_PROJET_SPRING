package tn.enicarthage.speedenicar_projet.scolarity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectDocumentRequest {
    @NotBlank(message = "Le motif de refus est obligatoire")
    @Size(max = 500, message = "Le motif ne dépasse pas 500 caractères")
    private String reason;
}