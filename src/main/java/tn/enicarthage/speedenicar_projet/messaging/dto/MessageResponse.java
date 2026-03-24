package tn.enicarthage.speedenicar_projet.messaging.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatarUrl;
    private Long receiverId;
    private String receiverName;
    private String content;
    private Boolean read;
    private LocalDateTime readAt;
    private String attachmentUrl;
    private String attachmentName;
    private LocalDateTime createdAt;
    private Boolean isMine;
}
