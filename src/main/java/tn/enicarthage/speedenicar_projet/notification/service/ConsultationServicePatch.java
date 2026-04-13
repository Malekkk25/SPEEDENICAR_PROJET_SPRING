package tn.enicarthage.speedenicar_projet.notification.service;
// ─── MODIFICATION de ConsultationService.java ────────────────────────────────
// Ajouter les imports et la publication d'événement dans createSession()
// Seules les parties modifiées sont montrées ici.



// 1. Ajouter dans les champs injectés :
//
//    private final ApplicationEventPublisher eventPublisher;
//
// (Lombok @RequiredArgsConstructor l'injecte automatiquement)


// 2. Dans createSession(), APRÈS sessionRepository.save(session) :
//
//    session = sessionRepository.save(session);
//    log.info("Session de consultation créée: roomId={}", session.getRoomId());
//
//    // ── Publier l'événement pour les notifications ──
//    eventPublisher.publishEvent(new ConsultationSessionCreatedEvent(this, session));
//
//    return toResponse(session);


// ─── Version complète de la méthode createSession() modifiée ─────────────────

/*
@Transactional
public SessionResponse createSession(Long appointmentId, User currentUser) {
    Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment non trouvé"));

    if (!appointment.getPsychologist().getUser().getId().equals(currentUser.getId())) {
        throw new ForbiddenException("Seul le psychologue assigné peut créer la session");
    }
    if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
        throw new BadRequestException("Le rendez-vous doit être confirmé pour démarrer une consultation");
    }
    sessionRepository.findByAppointmentId(appointmentId).ifPresent(s -> {
        throw new BadRequestException("Une session existe déjà pour ce rendez-vous (roomId: " + s.getRoomId() + ")");
    });

    ConsultationSession session = ConsultationSession.builder()
            .appointment(appointment)
            .roomId(UUID.randomUUID().toString())
            .status(SessionStatus.WAITING)
            .build();

    session = sessionRepository.save(session);
    log.info("Session créée: roomId={}, appointmentId={}", session.getRoomId(), appointmentId);

    // ✅ NOUVEAU — publier l'événement
    eventPublisher.publishEvent(new ConsultationSessionCreatedEvent(this, session));

    return toResponse(session);
}
*/
public class ConsultationServicePatch {
    // Ce fichier documente uniquement le patch — voir le commentaire ci-dessus.
}
