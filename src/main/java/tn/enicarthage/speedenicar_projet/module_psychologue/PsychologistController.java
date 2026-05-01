package tn.enicarthage.speedenicar_projet.module_psychologue;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.module_psychologue.document.MedicalDocumentRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.*;
import tn.enicarthage.speedenicar_projet.scolarity.dto.request.RejectDocumentRequest;
import tn.enicarthage.speedenicar_projet.scolarity.dto.response.MedicalDocumentResponse;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/api/psychologist")
@RequiredArgsConstructor
public class PsychologistController {

    private final PsychologistService psychologistService;
    private final UserRepository userRepository;
    private final MedicalDocumentRepository medicalDocumentRepository;

    // ─── Dashboard & Planning ─────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<PsychologistDashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(psychologistService.getDashboard(getUserId(userDetails))));
    }

    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getSchedule(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(psychologistService.getSchedule(getUserId(userDetails))));
    }

    @PutMapping("/schedule")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> updateSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.updateSchedule(getUserId(userDetails), request),
                "Planning mis à jour avec succès"));
    }

    @GetMapping("/schedule/available")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAvailableSlots(
            @RequestParam Long psychologistId,
            @RequestParam DayOfWeek day) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getAvailableSlots(psychologistId, day)));
    }

    // ─── Rendez-vous ──────────────────────────────────────────────────────

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAppointments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getAppointments(getUserId(userDetails), pageable)));
    }

    @GetMapping("/appointments/today")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getTodayAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getTodayAppointments(getUserId(userDetails))));
    }

    @GetMapping("/appointments/pending")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getPendingRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getPendingRequests(getUserId(userDetails))));
    }

    @PutMapping("/appointments/{id}/confirm")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.confirmAppointment(getUserId(userDetails), id),
                "Rendez-vous confirmé"));
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.cancelAppointment(getUserId(userDetails), id, reason),
                "Rendez-vous annulé"));
    }

    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.completeAppointment(getUserId(userDetails), id, notes),
                "Rendez-vous terminé"));
    }

    // ─── Dossiers & Suivis ────────────────────────────────────────────────

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<Page<RecordResponse>>> getAllRecords(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getAllMyRecords(getUserId(userDetails), pageable)));
    }

    @GetMapping("/records/student/{studentId}")
    public ResponseEntity<ApiResponse<Page<RecordResponse>>> getStudentRecords(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getStudentRecords(getUserId(userDetails), studentId, pageable)));
    }

    @PostMapping("/records")
    public ResponseEntity<ApiResponse<RecordResponse>> createRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateRecordRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        psychologistService.createRecord(getUserId(userDetails), request),
                        "Fiche de suivi créée"));
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<ApiResponse<RecordResponse>> updateRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.updateRecord(getUserId(userDetails), id, request),
                "Fiche de suivi mise à jour"));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<StudentAlertResponse>>> getStudentsAtRisk(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getStudentsAtRisk(getUserId(userDetails))));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        psychologistService.deleteRecord(getUserId(userDetails), id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Fiche de suivi supprimée"));
    }

    // ─── Documents Médicaux ──────────────────────────────────────────────

    @GetMapping("/documents/pending")
    public ResponseEntity<ApiResponse<List<MedicalDocumentResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.ok(psychologistService.getPendingDocuments()));
    }

    @GetMapping("/documents/student/{studentId}")
    public ResponseEntity<ApiResponse<List<MedicalDocumentResponse>>> getStudentMedicalDocuments(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.getStudentMedicalDocuments(getUserId(userDetails), studentId)));
    }

    @PutMapping("/documents/{id}/validate")
    public ResponseEntity<ApiResponse<MedicalDocumentResponse>> validate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.validateDocument(id, getUserId(userDetails)),
                "Document validé avec succès"));
    }

    @PutMapping("/documents/{id}/reject")
    public ResponseEntity<ApiResponse<MedicalDocumentResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectDocumentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                psychologistService.rejectDocument(id, request.getReason(), getUserId(userDetails)),
                "Document refusé"));
    }

    // ─── Téléchargement Document ──────────────────────────────────────────

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> downloadMedicalDocument(@PathVariable Long id) {
        return medicalDocumentRepository.findById(id)
                .map(doc -> {
                    String pathString = doc.getFilePath();
                    System.out.println("DEBUG - Tentative d'accès au fichier : " + pathString); // Log du chemin

                    Path path = Paths.get(pathString);
                    Resource resource = new FileSystemResource(path);

                    if (!resource.exists()) {
                        System.out.println("DEBUG - Fichier non trouvé sur le disque : " + pathString);
                        return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
                    }

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                            .contentType(MediaType.parseMediaType(doc.getMimeType()))
                            .body(resource);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // ─── Helper Unifié ────────────────────────────────────────────────────────

    private Long getUserId(Object principal) {
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof Authentication) {
            email = ((Authentication) principal).getName();
        } else {
            throw new RuntimeException("Principal non supporté");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email))
                .getId();
    }
}