package tn.enicarthage.speedenicar_projet.student.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileResponse {

    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String department;
    private String level;
    private Integer enrollmentYear;
    private LocalDate dateOfBirth;
    private Double averageGrade;
    private Long unjustifiedAbsences;
}