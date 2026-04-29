package tn.enicarthage.speedenicar_projet.user.entity;




import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.BaseEntity;
import tn.enicarthage.speedenicar_projet.common.enums.Role;
import tn.enicarthage.speedenicar_projet.messaging.entity.Message;
import tn.enicarthage.speedenicar_projet.notification.entity.Notification;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_role", columnList = "role")
})
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    @Enumerated(EnumType.STRING)
    private Role role; // STUDENT, PSYCHOLOGIST, etc.

    // --- CHAMPS AJOUTÉS (Anciennement dans PsychologistProfile) ---
    private String bio;
    private String specialty;
    private String licenseNumber;
    private Boolean enabled;

    private LocalDateTime lastLogin;
    // Ajoute ici tous les autres champs que tu avais dans PsychologistProfile
    public String getFullName() {
        if (firstName == null && lastName == null) return "";
        return this.firstName + " " + this.lastName;
    }
}
