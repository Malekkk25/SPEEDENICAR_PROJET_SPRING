package tn.enicarthage.speedenicar_projet.messaging.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversationResponse {
    private Long id;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatarUrl;
    private String otherUserRole;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
}
