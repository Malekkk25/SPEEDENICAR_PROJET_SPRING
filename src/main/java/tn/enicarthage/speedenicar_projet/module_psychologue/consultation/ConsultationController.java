package tn.enicarthage.speedenicar_projet.module_psychologue.consultation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;

import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

@Slf4j
@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;
    private final UserRepository      userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createSession(
            @RequestBody ConsultationDto.CreateSessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(
                ApiResponse.ok(consultationService.createSession(request.getAppointmentId(), user)));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<?>> getSessionByAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(
                ApiResponse.ok(consultationService.getSessionByAppointment(appointmentId, user)));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<?>> getSession(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(
                ApiResponse.ok(consultationService.getSession(roomId, user)));
    }

    @PostMapping("/room/{roomId}/join")
    public ResponseEntity<ApiResponse<?>> joinSession(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(
                ApiResponse.ok(consultationService.markJoined(roomId, user)));
    }

    @PostMapping("/room/{roomId}/end")
    public ResponseEntity<ApiResponse<?>> endSession(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(
                ApiResponse.ok(consultationService.endSession(roomId, user)));
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private User resolveUser(UserDetails userDetails) {
        String email = userDetails.getUsername();
        log.info("Résolution utilisateur: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
    }
}