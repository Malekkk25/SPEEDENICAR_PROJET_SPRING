package tn.enicarthage.speedenicar_projet.student.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.common.enums.MoodLevel;

@Entity
@Table(name = "journal_entries", indexes = {
        @Index(name = "idx_journal_student", columnList = "student_id"),
        @Index(name = "idx_journal_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Contenu chiffré — utiliser @Convert(converter = EncryptionConverter.class)
     * en production pour le chiffrement transparent.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MoodLevel mood;

    @Builder.Default
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = true;
}
