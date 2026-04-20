package tn.enicarthage.speedenicar_projet.module_psychologue.consultation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentStatus;
import tn.enicarthage.speedenicar_projet.common.exception.ResourceNotFoundException;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.AppointmentRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationSessionRepository sessionRepository;
    private final AppointmentRepository appointmentRepository;

    // 1. AJOUT : L'injecteur d'événements pour notifier le reste de l'application
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Le psychologue cree la salle de consultation avant le RDV.
     * Seul le psychologue assigne peut creer la session.
     */
    @Transactional
    public ConsultationDto.SessionResponse createSession(Long appointmentId, User currentUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment non trouve"));

        // Verifier que c'est le psychologue du RDV
        if (!appointment.getPsychologist().getId().equals(currentUser.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Seul le psychologue assigne peut creer la session");
        }

        // Verifier que le RDV est confirme
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Le rendez-vous doit etre confirme pour demarrer une consultation");
        }

        // 2. CORRECTION : Au lieu de lever une erreur si la session existe, on la renvoie
        Optional<ConsultationSession> existingSession = sessionRepository.findByAppointmentId(appointmentId);
        if (existingSession.isPresent()) {
            log.info("Session déjà existante pour appointmentId={}, renvoi direct", appointmentId);
            return toResponse(existingSession.get());
        }

        ConsultationSession session = ConsultationSession.builder()
                .appointment(appointment)
                .roomId(UUID.randomUUID().toString())
                .status(ConsultationSession.SessionStatus.WAITING)
                .build();

        session = sessionRepository.save(session);

        ConsultationSession fullyLoadedSession = sessionRepository.findByRoomId(session.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Erreur de rechargement session"));

        log.info("📢 Événement déclenché pour l'étudiant: {}",
                fullyLoadedSession.getAppointment().getStudent().getUser().getEmail());

// On envoie la version "chargée" à l'événement
        eventPublisher.publishEvent(new ConsultationSessionCreatedEvent(this, fullyLoadedSession));

        return toResponse(fullyLoadedSession);
    }

    /**
     * Recuperer les infos d'une session par son roomId.
     * Accessible uniquement aux 2 participants du RDV.
     */
    @Transactional(readOnly = true)
    public ConsultationDto.SessionResponse getSession(String roomId, User currentUser) {
        ConsultationSession session = sessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvee"));

        checkParticipant(session, currentUser);
        return toResponse(session);
    }

    /**
     * Recuperer la session liee a un appointment.
     */
    @Transactional(readOnly = true)
    public ConsultationDto.SessionResponse getSessionByAppointment(Long appointmentId, User currentUser) {
        ConsultationSession session = sessionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune session pour ce rendez-vous"));

        checkParticipant(session, currentUser);
        return toResponse(session);
    }

    /**
     * Enregistrer qu'un participant a rejoint la salle.
     */
    @Transactional
    public ConsultationDto.SessionResponse markJoined(String roomId, User currentUser) {
        ConsultationSession session = sessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvee"));

        checkParticipant(session, currentUser);

        Long studentUserId = session.getAppointment().getStudent().getUser().getId();
        boolean isStudent = currentUser.getId().equals(studentUserId);

        if (isStudent && session.getStudentJoinedAt() == null) {
            session.setStudentJoinedAt(LocalDateTime.now());
        } else if (!isStudent && session.getPsychologistJoinedAt() == null) {
            session.setPsychologistJoinedAt(LocalDateTime.now());
        }

        if (session.getStudentJoinedAt() != null && session.getPsychologistJoinedAt() != null
                && session.getStatus() == ConsultationSession.SessionStatus.WAITING) {
            session.setStatus(ConsultationSession.SessionStatus.ACTIVE);
            session.setStartedAt(LocalDateTime.now());
            log.info("Session ACTIVE: roomId={}", roomId);
        }

        return toResponse(sessionRepository.save(session));
    }

    /**
     * Terminer la session (l'un ou l'autre participant peut terminer).
     */
    @Transactional
    public ConsultationDto.SessionResponse endSession(String roomId, User currentUser) {
        ConsultationSession session = sessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvee"));

        checkParticipant(session, currentUser);

        if (session.getStatus() == ConsultationSession.SessionStatus.ENDED) {
            return toResponse(session);
        }

        session.setStatus(ConsultationSession.SessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());

        if (session.getStartedAt() != null) {
            session.setDurationMinutes((int) ChronoUnit.MINUTES.between(session.getStartedAt(), session.getEndedAt()));
        }

        Appointment appointment = session.getAppointment();
        appointment.setStatus(AppointmentStatus.COMPLETED);

        log.info("Session terminee: roomId={}, duree={}min", roomId, session.getDurationMinutes());
        return toResponse(sessionRepository.save(session));
    }

    // --- Helpers ---

    private void checkParticipant(ConsultationSession session, User user) {
        Long studentUserId = session.getAppointment().getStudent().getUser().getId();
        Long psychUserId = session.getAppointment().getPsychologist().getId();
        if (!user.getId().equals(studentUserId) && !user.getId().equals(psychUserId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Acces refuse a cette session");
        }
    }

    private ConsultationDto.SessionResponse toResponse(ConsultationSession s) {
        var studentUser = s.getAppointment().getStudent().getUser();
        var psych = s.getAppointment().getPsychologist();

        return ConsultationDto.SessionResponse.builder()
                .id(s.getId())
                .roomId(s.getRoomId())
                .status(s.getStatus())
                .appointmentId(s.getAppointment().getId())
                .student(ConsultationDto.ParticipantInfo.builder()
                        .userId(studentUser.getId())
                        .fullName(studentUser.getFirstName() + " " + studentUser.getLastName())
                        .avatarUrl(studentUser.getAvatarUrl())
                        .joined(s.getStudentJoinedAt() != null)
                        .build())
                .psychologist(ConsultationDto.ParticipantInfo.builder()
                        .userId(psych.getId())
                        .fullName(psych.getFirstName() + " " + psych.getLastName())
                        .avatarUrl(psych.getAvatarUrl())
                        .joined(s.getPsychologistJoinedAt() != null)
                        .build())
                .startedAt(s.getStartedAt())
                .endedAt(s.getEndedAt())
                .durationMinutes(s.getDurationMinutes())
                .build();
    }
}