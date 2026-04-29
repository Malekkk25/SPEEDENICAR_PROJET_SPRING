package tn.enicarthage.speedenicar_projet.student.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.student.dto.request.MoodEntryRequest;
import tn.enicarthage.speedenicar_projet.student.dto.response.MoodEntryResponse;
import tn.enicarthage.speedenicar_projet.student.entity.MoodEntry;
import tn.enicarthage.speedenicar_projet.student.service.MoodEntryService;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository; // <-- AJOUT

import java.util.Map;

@RestController
@RequestMapping("/api/student/moods")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class MoodEntryController {

    private final MoodEntryService moodEntryService;
    private final UserRepository userRepository; // <-- AJOUT

    // POST /api/student/moods
    @PostMapping
    public ResponseEntity<ApiResponse<MoodEntryResponse>> createMood(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody MoodEntryRequest request) {

        Long userId = extractUserId(user); // <-- CORRIGÉ

        MoodEntry moodEntry = MoodEntry.builder()
                .moodLevel(request.getMoodLevel())
                .moodLabel(request.getMoodLabel())
                .emoji(request.getEmoji())
                .note(request.getNote())
                .date(request.getDate())
                .activities(request.getActivities())
                .build();

        MoodEntry saved = moodEntryService.createMood(userId, moodEntry);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toResponse(saved), "Humeur enregistrée avec succès"));
    }

    // GET /api/student/moods
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MoodEntryResponse>>> getMoods(
            @AuthenticationPrincipal UserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {

        Long userId = extractUserId(user); // <-- CORRIGÉ
        Page<MoodEntryResponse> page = moodEntryService
                .getMoods(userId, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    // GET /api/student/moods/stats?period=week
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMoodStats(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "week") String period) {

        Long userId = extractUserId(user); // <-- CORRIGÉ
        Map<String, Object> stats = moodEntryService.getMoodStats(userId, period);

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    // DELETE /api/student/moods/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMood(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {

        Long userId = extractUserId(user); // <-- CORRIGÉ
        moodEntryService.deleteMood(userId, id);

        return ResponseEntity.ok(ApiResponse.ok(null, "Entrée supprimée avec succès"));
    }

    // ── Helper méthode pour éviter la répétition ─────────────────────────────

    private Long extractUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email))
                .getId();
    }

    // ── Mapper entité → response ─────────────────────────────

    private MoodEntryResponse toResponse(MoodEntry mood) {
        return MoodEntryResponse.builder()
                .id(mood.getId())
                .moodLevel(mood.getMoodLevel())
                .moodLabel(mood.getMoodLabel())
                .emoji(mood.getEmoji())
                .note(mood.getNote())
                .date(mood.getDate())
                .activities(mood.getActivities())
                .createdAt(mood.getCreatedAt())
                .build();
    }
}