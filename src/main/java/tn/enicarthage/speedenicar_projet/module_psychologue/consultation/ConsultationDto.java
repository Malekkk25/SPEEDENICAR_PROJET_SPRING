package tn.enicarthage.speedenicar_projet.module_psychologue.consultation;

import lombok.*;

import java.time.LocalDateTime;

public class ConsultationDto {

    // ─── Request ───────────────────────────────────────────────────────────────

    /** Créer une session à partir d'un appointment confirmé */
    @Data
    public static class CreateSessionRequest {
        private Long appointmentId;
    }

    // ─── Response ──────────────────────────────────────────────────────────────

    @Data @Builder
    public static class SessionResponse {
        private Long id;
        private String roomId;
        private ConsultationSession.SessionStatus status;
        private Long appointmentId;
        private ParticipantInfo student;
        private ParticipantInfo psychologist;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private Integer durationMinutes;
    }

    @Data @Builder
    public static class ParticipantInfo {
        private Long userId;
        private String fullName;
        private String avatarUrl;
        private boolean joined;
    }

    // ─── WebRTC Signaling Messages (via WebSocket) ─────────────────────────────

    @Getter
    @Setter @NoArgsConstructor
    @AllArgsConstructor
    public static class SignalMessage {
        private String type;
        private String roomId;
        private String senderId;
        private Object payload; // RTCSessionDescription, RTCIceCandidate, String, ou Map
    }

    @Data
    @Builder
    public static class JoinResponse {
        private String roomId;
        private boolean peerAlreadyPresent; // true si l'autre participant est déjà là
        private String peerId;
        private String peerName;
    }
}
