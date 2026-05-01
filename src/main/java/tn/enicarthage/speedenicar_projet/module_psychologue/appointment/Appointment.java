package tn.enicarthage.speedenicar_projet.module_psychologue.appointment;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentStatus;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentType;
import tn.enicarthage.speedenicar_projet.common.enums.LocationType;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appt_student", columnList = "student_id"),
        @Index(name = "idx_appt_psychologist", columnList = "psychologist_id"),
        @Index(name = "idx_appt_datetime", columnList = "date_time"),
        @Index(name = "idx_appt_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psychologist_id", nullable = false)
    private User psychologist;

    @NotNull
    /*@Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;*/
    @Column(name = "date_time", nullable = false, columnDefinition = "TEXT")
    private LocalDateTime dateTime;

    @Column(nullable = false)
    @Builder.Default
    private Integer duration = 30;
    // Dans tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment
    @Column(name = "location_type")
    @Enumerated(EnumType.STRING)
    private LocationType locationType;
    // (Ou private String locationType; si tu n'utilises pas d'Enum)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentType type = AppointmentType.INITIAL;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    // ── Business methods ────────────────────────────────────

    public void confirm() {
        if (this.status != AppointmentStatus.PENDING) {
            throw new IllegalStateException(
                    "Impossible de confirmer un RDV avec le statut : " + this.status
            );
        }
        this.status = AppointmentStatus.CONFIRMED;
    }

    /**
     * FIXED: On bloque SEULEMENT si le RDV est déjà COMPLETED.
     * Un RDV PENDING ou CONFIRMED peut être annulé.
     */
    public void cancel(Long userId, String reason) {
        if (this.status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Un RDV COMPLETED ne peut pas être annulé"
            );
        }
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Ce RDV est déjà annulé"
            );
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledBy = userId;
        this.cancellationReason = reason;
    }



    public void complete(String sessionNotes) {
        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Seul un RDV CONFIRMED peut être marqué comme terminé"
            );
        }
        this.status = AppointmentStatus.COMPLETED;
        this.notes = sessionNotes;
    }

    public boolean isPast() {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }

    public boolean isUpcoming() {
        return dateTime != null
                && dateTime.isAfter(LocalDateTime.now())
                && status == AppointmentStatus.CONFIRMED;
    }
}
