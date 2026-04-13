package tn.enicarthage.speedenicar_projet.module_psychologue.consultation;

import jakarta.persistence.*;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;

import java.time.LocalDateTime;
@Entity
@Table(name = "consultation_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsultationSession extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(name = "room_id", nullable = false, unique = true)
    private String roomId; // UUID généré à la création

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "student_joined_at")
    private LocalDateTime studentJoinedAt;

    @Column(name = "psychologist_joined_at")
    private LocalDateTime psychologistJoinedAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    public enum SessionStatus {
        WAITING,   // Créée, en attente que les deux participants rejoignent
        ACTIVE,    // Les deux participants sont connectés
        ENDED,     // Session terminée normalement
        ABANDONED  // Session abandonnée (timeout ou déconnexion)
    }
}
