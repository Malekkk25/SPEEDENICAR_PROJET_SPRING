package tn.enicarthage.speedenicar_projet.module_psychologue.consultation.listener;

import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSession;
import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSessionCreatedEvent;

import java.time.format.DateTimeFormatter;
import java.util.Map;

// ── 2. Imports Spring Framework ─────────────────────────────────────────────
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// ── 3. Imports Lombok ───────────────────────────────────────────────────────
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ── 4. Imports liés à votre module Consultation (déjà présents) ─────────────
import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSession;
import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSessionCreatedEvent;

// ── 5. Imports spécifiques à votre projet (À ADAPTER SELON VOS PACKAGES) ────
// Import de l'entité User
import tn.enicarthage.speedenicar_projet.notification.dto.NotificationPayload;
import tn.enicarthage.speedenicar_projet.notification.service.EmailService;
import tn.enicarthage.speedenicar_projet.user.entity.User;

// Imports des services de notification et d'email
import tn.enicarthage.speedenicar_projet.notification.service.NotificationDispatchService; // Exemple

// Exemple

/**
 * Écoute ConsultationSessionCreatedEvent et déclenche :
 *  1. Notification WebSocket temps réel → étudiant
 *  2. Email HTML → étudiant (avec lien de la salle)
 *  3. Email HTML → psychologue (confirmation de création)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsultationNotificationListener {

    private final NotificationDispatchService notificationDispatchService;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    @Async
    @EventListener
    public void onSessionCreated(ConsultationSessionCreatedEvent event) {
        ConsultationSession session = event.getSession();

        User student     = session.getAppointment().getStudent().getUser();
        User psychologist = session.getAppointment().getPsychologist();
        String roomUrl   = "/consultation/room/" + session.getRoomId();
        String dateTime  = session.getAppointment().getDateTime().format(DATE_FMT);

        log.info("Envoi notifications pour session roomId={}", session.getRoomId());

        // ── 1. WebSocket → Étudiant ──────────────────────────────────────────
        notificationDispatchService.sendToUser(student,
                NotificationPayload.builder()
                        .title("Consultation vidéo disponible")
                        .message(String.format(
                                "Votre consultation avec %s %s est prête. Cliquez pour rejoindre.",
                                psychologist.getFirstName(), psychologist.getLastName()))
                        .type(NotificationPayload.NotificationType.CONSULTATION_SESSION_READY)
                        .actionUrl(roomUrl)
                        .data(Map.of(
                                "roomId", session.getRoomId(),
                                "appointmentId", session.getAppointment().getId(),
                                "psychologistName", psychologist.getFirstName() + " " + psychologist.getLastName()
                        ))
                        .build()
        );

        // ── 2. WebSocket → Psychologue (confirmation) ────────────────────────
        notificationDispatchService.sendToUser(psychologist,
                NotificationPayload.builder()
                        .title("Salle de consultation créée")
                        .message(String.format(
                                "La salle pour votre consultation avec %s %s est prête.",
                                student.getFirstName(), student.getLastName()))
                        .type(NotificationPayload.NotificationType.CONSULTATION_SESSION_READY)
                        .actionUrl(roomUrl)
                        .data(Map.of(
                                "roomId", session.getRoomId(),
                                "appointmentId", session.getAppointment().getId()
                        ))
                        .build()
        );

        // ── 3. Email → Étudiant ──────────────────────────────────────────────
        emailService.sendHtml(
                student.getEmail(),
                "📹 Votre consultation vidéo est prête — SPEED",
                "consultation-ready-student",
                Map.of(
                        "studentFirstName",     student.getFirstName(),
                        "psychologistFullName",  psychologist.getFirstName() + " " + psychologist.getLastName(),
                        "appointmentDateTime",   dateTime,
                        "roomUrl",               "https://speed-platform.dz" + roomUrl,
                        "roomId",                session.getRoomId()
                )
        );

        // ── 4. Email → Psychologue ───────────────────────────────────────────
        emailService.sendHtml(
                psychologist.getEmail(),
                "✅ Salle de consultation créée — SPEED",
                "consultation-ready-psychologist",
                Map.of(
                        "psychologistFirstName", psychologist.getFirstName(),
                        "studentFullName",        student.getFirstName() + " " + student.getLastName(),
                        "appointmentDateTime",    dateTime,
                        "roomUrl",                "https://speed-platform.dz" + roomUrl,
                        "roomId",                 session.getRoomId()
                )
        );
    }
}
