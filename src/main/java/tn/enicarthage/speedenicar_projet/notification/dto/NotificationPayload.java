package tn.enicarthage.speedenicar_projet.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationPayload {

    public enum NotificationType {
        CONSULTATION_SESSION_READY,   // Salle vidéo ouverte
        CONSULTATION_SESSION_STARTED, // Les deux participants connectés
        CONSULTATION_SESSION_ENDED,   // Session terminée
        APPOINTMENT_REMINDER          // (futur)
    }

    private Long        id;
    private String      title;
    private String      message;
    private NotificationType type;
    private String      actionUrl;   // Ex: /consultation/room/{roomId}
    private boolean     read;
    private LocalDateTime createdAt;

    /** Données métier supplémentaires (roomId, appointmentId…) */
    private Object      data;
}
