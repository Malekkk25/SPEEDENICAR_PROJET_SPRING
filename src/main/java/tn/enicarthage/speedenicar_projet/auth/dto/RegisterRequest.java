package tn.enicarthage.speedenicar_projet.auth.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.Role;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, message = "Minimum 8 caractères")
    private String password;

    @NotBlank private String firstName;
    @NotBlank private String lastName;
    private String phone;
    @NotNull private Role role;

    // Conditional
    private String studentId;
    private String department;
    private String level;
    private String licenseNumber;
    private String specialization;
}

