package tn.enicarthage.speedenicar_projet.auth;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.auth.dto.AuthResponse;
import tn.enicarthage.speedenicar_projet.auth.dto.LoginRequest;
import tn.enicarthage.speedenicar_projet.auth.dto.RefreshTokenRequest;
import tn.enicarthage.speedenicar_projet.auth.dto.RegisterRequest;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(authService.login(request), "Connexion réussie"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Inscription réussie"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(authService.refreshToken(request), "Token renouvelé"));
    }

}
