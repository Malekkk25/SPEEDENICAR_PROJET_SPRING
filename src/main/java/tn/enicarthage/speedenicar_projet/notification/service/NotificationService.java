package tn.enicarthage.speedenicar_projet.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.enicarthage.speedenicar_projet.common.enums.NotificationType;
import tn.enicarthage.speedenicar_projet.common.exception.ResourceNotFoundException;
import tn.enicarthage.speedenicar_projet.notification.dto.NotificationResponse;
import tn.enicarthage.speedenicar_projet.notification.entity.Notification;
import tn.enicarthage.speedenicar_projet.notification.repository.NotificationRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepo;

    // ── Query ───────────────────────────────────────────────

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepo
                .findByRecipientIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public Page<NotificationResponse> getUnread(Long userId, Pageable pageable) {
        return notificationRepo
                .findByRecipientIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(
                        userId, pageable)
                .map(this::toResponse);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepo.countByRecipientIdAndReadFalseAndDeletedFalse(userId);
    }

    // ── Commands ────────────────────────────────────────────

    @Transactional
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notif = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification", "id", notificationId));
        if (!notif.getRecipient().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }
        notif.markAsRead();
        return toResponse(notificationRepo.save(notif));
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepo.markAllAsRead(userId);
    }

    @Transactional
    public void send(User recipient, String title, String message,
                     NotificationType type, String link, String senderName) {
        Notification notif = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .link(link)
                .senderName(senderName)
                .build();

        notificationRepo.save(notif);
        log.debug("Notification saved for user {}: {}", recipient.getId(), title);
    }

    // ── Mapper ──────────────────────────────────────────────

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .read(n.getRead())
                .readAt(n.getReadAt())
                .link(n.getLink())
                .senderName(n.getSenderName())
                .createdAt(n.getCreatedAt())
                .build();
    }
}