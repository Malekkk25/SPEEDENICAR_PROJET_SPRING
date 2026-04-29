package tn.enicarthage.speedenicar_projet.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.student.service.StudentProfileService;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository; // Import à ajouter

@RestController
@RequestMapping("/api/student/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;
    private final UserRepository userRepository; // Injecter le repository

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProfile(@AuthenticationPrincipal UserDetails user) {

        // CORRECTION ICI : Ne pas faire Long.parseLong(user.getUsername())
        String email = user.getUsername();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"))
                .getId();

        return ResponseEntity.ok(ApiResponse.ok(studentProfileService.getProfileByUserId(userId)));
    }
}