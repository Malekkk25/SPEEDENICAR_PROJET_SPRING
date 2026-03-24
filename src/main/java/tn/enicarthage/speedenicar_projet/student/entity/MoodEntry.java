package tn.enicarthage.speedenicar_projet.student.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.common.enums.MoodLevel;

import java.time.LocalDate;

/**
 * Entrée du mood tracker — suivi quotidien de l'humeur.
 *
 * Chaque étudiant peut enregistrer une entrée par jour.
 * Le moodLevel (1-5) est complété par un emoji et une note optionnelle.
 * Le champ activities stocke les activités du jour en JSON.
 *
 * Contrainte unique : (student_id, date) — une humeur par jour.
 */
@Entity
@Table(name = "mood_entries", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mood_student_date", columnNames = {"student_id", "date"})
}, indexes = {
        @Index(name = "idx_mood_student", columnList = "student_id"),
        @Index(name = "idx_mood_date", columnList = "date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "mood_level", nullable = false)
    private Integer moodLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood_label", length = 20)
    private MoodLevel moodLabel;

    @Column(length = 10)
    private String emoji;

    @Column(columnDefinition = "TEXT")
    private String note;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Activités du jour — stockées en JSON.
     * Exemple : ["sport", "lecture", "amis"]
     */
    @Column(columnDefinition = "JSON")
    private String activities;
}

