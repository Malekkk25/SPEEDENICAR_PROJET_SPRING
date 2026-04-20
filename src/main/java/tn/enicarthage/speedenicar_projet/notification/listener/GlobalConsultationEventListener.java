package tn.enicarthage.speedenicar_projet.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSession;
import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSessionCreatedEvent;
import tn.enicarthage.speedenicar_projet.notification.service.NotificationDispatchService;
// Ajoute tes imports pour EmailService quand tu l'auras

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalConsultationEventListener {

    // On injecte ton service de WebSocket
    private final NotificationDispatchService notificationDispatchService;

    // On injectera le service d'email plus tard
    // private final EmailService emailService;

    @EventListener
    public void onConsultationSessionCreated(ConsultationSessionCreatedEvent event) {
        ConsultationSession session = event.getSession();
        log.info("📢 Événement capturé ! Préparation des notifications pour la roomId: {}", session.getRoomId());

        /* * 1. TODO : Envoyer la notification WebSocket via ton NotificationDispatchService
         * Tu pourras créer un NotificationPayload et faire :
         * notificationDispatchService.sendToUser(etudiantUser, payload);
         */

        /* * 2. TODO : Envoyer l'Email via JavaMailSender
         * emailService.sendConsultationLink(session);
         */
    }
}
