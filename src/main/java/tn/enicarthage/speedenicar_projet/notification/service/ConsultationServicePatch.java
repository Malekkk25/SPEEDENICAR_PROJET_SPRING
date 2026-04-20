package tn.enicarthage.speedenicar_projet.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.enicarthage.speedenicar_projet.common.enums.AppointmentStatus;
import tn.enicarthage.speedenicar_projet.common.exception.ResourceNotFoundException;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.AppointmentRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationDto;
import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSession;
import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSessionCreatedEvent;
import tn.enicarthage.speedenicar_projet.module_psychologue.consultation.ConsultationSessionRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationServicePatch {

    private final AppointmentRepository appointmentRepository;
    private final ConsultationSessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ConsultationDto.SessionResponse createSession(Long appointmentId, User currentUser) {

        // 1. Récupération du rendez-vous
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment non trouvé"));

        // 2. Vérification des autorisations
        // 👇 AJOUT DES LOGS POUR LE DÉBOGAGE
        log.info("=== DEBUG AUTORISATION SESSION ===");
        log.info("Rendez-vous ID: {}", appointmentId);

        // ⚠️ Si .getUser() est souligné en rouge, remplace par .getId() et regarde les logs
        Long assignedPsyUserId = appointment.getPsychologist().getId();
        Long currentUserId = currentUser.getId();

        log.info("ID du User assigné au psy : {}", assignedPsyUserId);
        log.info("ID du User connecté : {}", currentUserId);
        log.info("==================================");

        if (!assignedPsyUserId.equals(currentUserId)) {
            throw new AccessDeniedException("Seul le psychologue assigné peut créer la session");
        }

        // 3. Vérification du statut
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalArgumentException("Le rendez-vous doit être confirmé pour démarrer une consultation");
        }

        // 4. Vérification d'une session existante
        // 4. Vérification d'une session existante
        java.util.Optional<ConsultationSession> existingSession = sessionRepository.findByAppointmentId(appointmentId);
        if (existingSession.isPresent()) {
            log.info("Session déjà existante pour appointmentId={}, renvoi direct", appointmentId);
            return toResponse(existingSession.get());
        }

        // 5. Création de la session (si elle n'existe pas)
        ConsultationSession session = ConsultationSession.builder()
                .appointment(appointment)
                .roomId(UUID.randomUUID().toString())
                .status(ConsultationSession.SessionStatus.WAITING)
                .build();

        session = sessionRepository.save(session);
        log.info("Session créée avec succès: roomId={}, appointmentId={}", session.getRoomId(), appointmentId);

        // ✅ Publication de l'événement pour les notifications email
        eventPublisher.publishEvent(new ConsultationSessionCreatedEvent(this, session));

        return toResponse(session);
    }

    // ------------------------------------------------------------------------
    // Reste de tes méthodes de patch éventuelles...
    // ------------------------------------------------------------------------

    private ConsultationDto.SessionResponse toResponse(ConsultationSession session) {
        return ConsultationDto.SessionResponse.builder()
                .roomId(session.getRoomId())
                .status(session.getStatus())
                .build();
    }
}