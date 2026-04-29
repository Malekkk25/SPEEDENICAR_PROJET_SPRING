package tn.enicarthage.speedenicar_projet.module_psychologue.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots" , indexes = {
        @Index(name = "idx_slot_psy", columnList = "psychologist_id"),
        @Index(name = "idx_slot_day", columnList = "day_of_week")
},uniqueConstraints =  {
        @UniqueConstraint(name = "uk_slot_day_start" ,
        columnNames = {"psychologist_id" , "day_of_week" ,"start_time"})

})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // On pointe vers User au lieu de PsychologistProfile
    @ManyToOne
    @JoinColumn(name = "psychologist_id")
    private User psychologist;

    @Enumerated(EnumType.STRING) // ABSOLUMENT NECESSAIRE pour lire "WEDNESDAY"
    private DayOfWeek dayOfWeek;
    private String startTime;
    private String endTime;
    private boolean available = true;
    @Column(name = "is_deleted") // Force Hibernate à regarder la bonne colonne
    private boolean deleted;

}








