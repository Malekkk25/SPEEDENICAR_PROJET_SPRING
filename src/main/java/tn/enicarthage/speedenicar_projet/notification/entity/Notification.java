package tn.enicarthage.speedenicar_projet.notification.entity;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.common.enums.NotificationType;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_recipient", columnList = "recipient_id"),
    @Index(name = "idx_notif_read", columnList = "is_read"),
    @Index(name = "idx_notif_type", columnList = "type"),
    @Index(name = "idx_notif_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;

    /*@Column(name = "read_at")
    private LocalDateTime readAt;*/
    @Column(name = "read_at", columnDefinition = "TEXT")
    private LocalDateTime readAt;

    @Column(length = 500)
    private String link;

    @Column(name = "sender_name", length = 100)
    private String senderName;

    // ── Business methods ────────────────────────────────────

    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
}
