package tn.enicarthage.speedenicar_projet.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SendMessageRequest {
    @NotNull(message = "Le destinataire est obligatoire")
    private Long receiverId;

    @NotBlank(message = "Le message ne peut pas être vide")
    private String content;

    private String attachmentUrl;
    private String attachmentName;
}
