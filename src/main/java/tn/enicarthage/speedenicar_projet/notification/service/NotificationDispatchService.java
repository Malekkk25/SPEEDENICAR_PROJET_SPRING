package tn.enicarthage.speedenicar_projet.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.enicarthage.speedenicar_projet.common.enums.NotificationType; // Assurez-vous du bon import
import tn.enicarthage.speedenicar_projet.notification.dto.NotificationPayload;
import tn.enicarthage.speedenicar_projet.notification.entity.Notification;
import tn.enicarthage.speedenicar_projet.notification.repository.NotificationRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    /**
     * Persiste la notification ET la pousse en WebSocket à l'utilisateur cible.
     *
     * @param recipient Utilisateur destinataire
     * @param payload   Contenu de la notification
     */
    @Transactional
    public void sendToUser(User recipient, NotificationPayload payload) {

        // 1. Construire l'entité Notification avec les VRAIES données du payload
        Notification notification = Notification.builder()
                .recipient(recipient) // ✅ On utilise la variable recipient, pas la classe
                .title(payload.getTitle())
                .message(payload.getMessage())
                // Si payload.getType() et l'entité n'utilisent pas exactement le même Enum,
                // on convertit via le nom : NotificationType.valueOf(payload.getType().name())
                .type(NotificationType.valueOf(payload.getType().name()))
                .link(payload.getActionUrl())
                // .senderName(...) // À remplir si vous ajoutez l'expéditeur dans le payload
                .build();

        // 2. Sauvegarder en base ET récupérer l'objet sauvegardé (pour avoir son ID généré)
        Notification saved = notificationRepository.save(notification);

        // 3. Construire le payload WebSocket avec l'ID et la date générés par la BDD
        NotificationPayload toSend = NotificationPayload.builder()
                .id(saved.getId())
                .title(payload.getTitle())
                .message(payload.getMessage())
                .type(payload.getType())
                .actionUrl(payload.getActionUrl())
                .data(payload.getData())
                .read(false)
                .createdAt(saved.getCreatedAt())
                .build();

        // 4. Push WebSocket → /topic/notifications/{userId}
        String destination = "/topic/notifications/" + recipient.getId();
        messagingTemplate.convertAndSend(destination, toSend);

        log.debug("Notification envoyée à userId={} via {}: {}",
                recipient.getId(), destination, payload.getTitle());
    }
}