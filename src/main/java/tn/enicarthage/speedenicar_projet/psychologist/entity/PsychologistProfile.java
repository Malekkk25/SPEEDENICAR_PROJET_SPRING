package tn.enicarthage.speedenicar_projet.psychologist.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "psychologist_profiles" ,indexes= {
        @Index(name = "idx_psy_license" , columnList = "license_nulber", unique = true),
        @Index(name = "idx_psy_user" ,columnList = "user_id" ,unique = true)

})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PsychologistProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id " ,nullable = false , unique = true)
    private User user;

@NotBlank
    @Column(name = "license_number" , nullable = false , unique = true ,length = 50)
    private String licenseNumber;

@Column(length = 100)
    private String specialization;



@Column(name = "max_daily_appointments"  , nullable = false )
    @Builder.Default
    private Integer maxDailyAppointments=8;


@Column(columnDefinition = "TEXT")
    private String bio;


    @Builder.Default
    @OneToMany(mappedBy = "psychologist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayOfWeek ASC, startTime ASC")
    private List<TimeSlot> timeSlots = new ArrayList<>();

@Builder.Default
    @OneToMany(mappedBy = "psychologist" ,cascade = CascadeType.ALL , orphanRemoval = true)
@OrderBy("sessionDate DESC")
    private List<ConfidentialRecord> confidentialRecords =new ArrayList<>();

public void addTimeSlot(TimeSlot slot){
    timeSlots.add(slot);
    slot.setPsychologist(this);
}

public void addRecord(ConfidentialRecord record){
    confidentialRecords.add(record);
    record.setPsychologist(this);
}
public  String getFullName(){
    return user!= null ? user.getFullName() : null;
}
}
