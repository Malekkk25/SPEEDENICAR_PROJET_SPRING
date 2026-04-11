package tn.enicarthage.speedenicar_projet.messaging.entity;



import jakarta.persistence.*;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_conv_last_msg", columnList = "last_message_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_one_id", nullable = false)
    private User participantOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_two_id", nullable = false)
    private User participantTwo;

   /* @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;*/
   @Column(name = "last_message_at", columnDefinition = "TEXT")
   private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    // ── Helpers ──────────────────────────────────────────────

    public boolean involvesUser(Long userId) {
        return (participantOne != null && participantOne.getId().equals(userId))
                || (participantTwo != null && participantTwo.getId().equals(userId));
    }

    public User getOtherParticipant(Long currentUserId) {
        if (participantOne != null && participantOne.getId().equals(currentUserId)) {
            return participantTwo;
        }
        return participantOne;
    }

    public void updateLastMessage(String preview) {
        this.lastMessageAt = LocalDateTime.now();
        this.lastMessagePreview = preview != null && preview.length() > 255
                ? preview.substring(0, 252) + "..."
                : preview;
    }
}
