package tn.enicarthage.speedenicar_projet.module_psychologue.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.common.enums.AlertSeverity;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;

import java.time.LocalDate;

@Entity
@Table(name = "confidential_records" , indexes = {
        @Index(name = "idx_confr_student", columnList = "student_id"),
        @Index(name = "idx_confr_psy", columnList = "psychologist_id"),
        @Index(name = "idx_confr_risk", columnList = "risk_level"),
        @Index(name = "idx_confr_session", columnList = "session_date")

})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ConfidentialRecord  extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id" , nullable = false)
    private StudentProfile student ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psychologist_id" ,nullable = false)
    private PsychologistProfile psychologist;

    @NotNull
    @Column(name = "session_date" ,nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false ,columnDefinition = "TEXT")
    private String observations;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level" ,nullable = false , length = 20)
    @Builder.Default
    private AlertSeverity riskLevel =AlertSeverity.LOW;

    @Column(columnDefinition =  "TEXT")
    private String recommendations;

    @Column(name = "follow_up_required" , nullable = false)
    @Builder.Default
    private Boolean followUpRequired =false;

    @Column(name = "next_session_date")
    private LocalDate nextSessionDate;

    @Column(name = "session_duration_minutes")
    @Builder.Default
    private Integer sessionDurationMinutes=30;

    @Column(columnDefinition = "TEXT")
    private String interventions;

    @Column(name = "student_progress" , length = 50)
    private String studentProgress;

public Boolean isCritical(){
    return AlertSeverity.CRITICAL.equals(this.riskLevel)
            || AlertSeverity.HIGH.equals(this.riskLevel);
}

public void escalteRisk(AlertSeverity newLevel){
    if(newLevel.ordinal() > this.riskLevel.ordinal()){
        this.riskLevel=newLevel;
        this.followUpRequired=true;
    }
}
}
