package tn.enicarthage.speedenicar_projet.auth.dto;


import lombok.*;
import tn.enicarthage.speedenicar_projet.common.enums.Role;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    public static AuthResponse of(String accessToken, String refreshToken,
                                  Long userId, String email,
                                  String firstName, String lastName, Role role) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .build();
    }
}
