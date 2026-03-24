package tn.enicarthage.speedenicar_projet.notification.dto;


import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.NotificationType;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean read;
    private LocalDateTime readAt;
    private String link;
    private String senderName;
    private LocalDateTime createdAt;
}
