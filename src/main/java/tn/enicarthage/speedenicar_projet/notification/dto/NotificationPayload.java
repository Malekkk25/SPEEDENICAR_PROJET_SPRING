package tn.enicarthage.speedenicar_projet.notification.dto;

import lombok.Builder;
import lombok.Data;
import tn.enicarthage.speedenicar_projet.common.enums.NotificationType;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationPayload {



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
