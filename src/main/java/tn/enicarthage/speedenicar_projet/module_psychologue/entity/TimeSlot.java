package tn.enicarthage.speedenicar_projet.module_psychologue.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;

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

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn (name = "psychologist_id" , nullable = false)
private PsychologistProfile psychologist;


@NotNull
@Enumerated (EnumType.STRING)
@Column(name = "day_of_week" , nullable = false ,length = 10)
private DayOfWeek dayOfWeek;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

@NotNull
@Column(name = "end_time" ,nullable = false)
private LocalTime endTime;

@Column(nullable = false)
    @Builder.Default
    private Boolean available =true;

public int getDurationMinutes(){
    if(startTime == null || endTime ==null) return 0;
    return (int)java.time.Duration.between(startTime,endTime).toMinutes();
}

public boolean overlapsWith(LocalTime otherStart ,LocalTime otherEnd){
    return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
}


@PrePersist
    @PreUpdate
    private void validateTimes(){
    if(startTime != null && endTime!= null && !endTime.isAfter(startTime)){
throw  new IllegalArgumentException(" L_heure de din doit etre postérieure à l'heure de début ");
    }
}







}
