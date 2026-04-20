package tn.enicarthage.speedenicar_projet.module_psychologue.consultation.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSession;
import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSessionCreatedEvent;
import tn.enicarthage.speedenicar_projet.notification.dto.NotificationPayload;
import tn.enicarthage.speedenicar_projet.notification.service.EmailService;
import tn.enicarthage.speedenicar_projet.notification.service.NotificationDispatchService;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.common.enums.NotificationType;

import java.time.format.DateTimeFormatter;
import java.util.Map;

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

        User student = session.getAppointment().getStudent().getUser();
        User psychologist = session.getAppointment().getPsychologist();
        String roomUrl = "/consultation/room/" + session.getRoomId();
        String dateTime = session.getAppointment().getDateTime().format(DATE_FMT);

        log.info("Envoi notifications pour session roomId={}", session.getRoomId());

        // ── 1. WebSocket → Étudiant ──────────────────────────────────────────
        notificationDispatchService.sendToUser(student,
                NotificationPayload.builder()
                        .title("Consultation vidéo disponible")
                        .message(String.format(
                                "Votre consultation avec %s %s est prête. Cliquez pour rejoindre.",
                                psychologist.getFirstName(), psychologist.getLastName()))
                        .type(NotificationType.APPOINTMENT)
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
                        .type(NotificationType.APPOINTMENT)
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
                "emails/consultation-ready-student", // CORRECTION ICI
                Map.of(
                        "studentFirstName", student.getFirstName(),
                        "psychologistFullName", psychologist.getFirstName() + " " + psychologist.getLastName(),
                        "appointmentDateTime", dateTime,
                        "roomUrl", "http://localhost:4200" + roomUrl,
                        "roomId", session.getRoomId()
                )
        );

        // ── 4. Email → Psychologue ───────────────────────────────────────────
        emailService.sendHtml(
                psychologist.getEmail(),
                "✅ Salle de consultation créée — SPEED",
                "emails/consultation-ready-psychologist", // CORRECTION ICI
                Map.of(
                        "psychologistFirstName", psychologist.getFirstName(),
                        "studentFullName", student.getFirstName() + " " + student.getLastName(),
                        "appointmentDateTime", dateTime,
                        "roomUrl", "http://localhost:4200" + roomUrl,
                        "roomId", session.getRoomId()
                )
        );
    }
}