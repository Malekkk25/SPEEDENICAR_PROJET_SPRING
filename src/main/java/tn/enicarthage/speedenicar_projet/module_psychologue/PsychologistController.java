package tn.enicarthage.speedenicar_projet.module_psychologue;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.*;
import tn.enicarthage.speedenicar_projet.scolarity.dto.response.MedicalDocumentResponse;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/api/psychologist")
@RequiredArgsConstructor
public class PsychologistController {

    private final PsychologistService psychologistService;
    private final UserRepository      userRepository;   // ← ajout

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<PsychologistDashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(psychologistService.getDashboard(userId)));
    }

    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getSchedule(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(psychologistService.getSchedule(userId)));
    }

    @PutMapping("/schedule")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> updateSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.updateSchedule(userId, request),
                "Planning mis à jour avec succès"));
    }

    @GetMapping("/schedule/available")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAvailableSlots(
            @RequestParam Long psychologistId,
            @RequestParam DayOfWeek day) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getAvailableSlots(psychologistId, day)));
    }

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAppointments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getAppointments(userId, pageable)));
    }

    @GetMapping("/appointments/today")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getTodayAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getTodayAppointments(userId)));
    }

    @GetMapping("/appointments/pending")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getPendingRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getPendingRequests(userId)));
    }

    @PutMapping("/appointments/{id}/confirm")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.confirmAppointment(userId, id),
                "Rendez-vous confirmé"));
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.cancelAppointment(userId, id, reason),
                "Rendez-vous annulé"));
    }

    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.completeAppointment(userId, id, notes),
                "Rendez-vous terminé"));
    }

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<Page<RecordResponse>>> getAllRecords(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 15) Pageable pageable) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getAllMyRecords(userId, pageable)));
    }

    @GetMapping("/records/student/{studentId}")
    public ResponseEntity<ApiResponse<Page<RecordResponse>>> getStudentRecords(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getStudentRecords(userId, studentId, pageable)));
    }

    @PostMapping("/records")
    public ResponseEntity<ApiResponse<RecordResponse>> createRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateRecordRequest request) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        psychologistService.createRecord(userId, request),
                        "Fiche de suivi créée"));
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<ApiResponse<RecordResponse>> updateRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecordRequest request) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.updateRecord(userId, id, request),
                "Fiche de suivi mise à jour"));
    }

    @GetMapping("/records/follow-ups")
    public ResponseEntity<ApiResponse<List<RecordResponse>>> getPendingFollowUps(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getPendingFollowUps(userId)));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<StudentAlertResponse>>> getStudentsAtRisk(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getStudentsAtRisk(userId)));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = extractUserId(userDetails);
        psychologistService.deleteRecord(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Fiche de suivi supprimée"));
    }

// ─── Documents Médicaux (Gestion par le Psychologue) ──────────────

    @GetMapping("/documents/student/{studentId}")
    public ResponseEntity<ApiResponse<List<MedicalDocumentResponse>>> getStudentMedicalDocuments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId) {
        Long userId = extractUserId(userDetails);

        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getStudentMedicalDocuments(userId, studentId)));
    }

    @PutMapping("/documents/{documentId}/status")
    public ResponseEntity<ApiResponse<MedicalDocumentResponse>> updateDocumentStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long documentId,
            @RequestParam String status,  // "VALIDATED" ou "REJECTED"
            @RequestParam(required = false) String reason) {
        Long userId = extractUserId(userDetails);

        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.updateDocumentStatus(userId, documentId, status, reason),
                "Statut du document mis à jour"));
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    /**
     * ✅ CORRIGÉ : username = email → chercher l'ID en base
     * Avant : Long.parseLong(email) → NumberFormatException
     */
    private Long extractUserId(UserDetails userDetails) {
        String email = userDetails.getUsername(); // = email grâce au fix JWT
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur introuvable : " + email))
                .getId();
    }
}