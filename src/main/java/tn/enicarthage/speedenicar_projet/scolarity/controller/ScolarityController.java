package tn.enicarthage.speedenicar_projet.scolarity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.scolarity.dto.request.RejectDocumentRequest;
import tn.enicarthage.speedenicar_projet.scolarity.dto.response.*;
import tn.enicarthage.speedenicar_projet.scolarity.service.ScolarityService;
import tn.enicarthage.speedenicar_projet.security.service.CustomUserDetails;
import tn.enicarthage.speedenicar_projet.student.entity.AcademicRecord;
import java.util.List;

@RestController
@RequestMapping("/api/scolarity")
@PreAuthorize("hasRole('SCOLARITY')")
@RequiredArgsConstructor
public class ScolarityController {

    private final ScolarityService service;

    // ── DOSSIERS ÉTUDIANTS ────────────────────────────────────────

    @GetMapping("/students")
    public ResponseEntity<Page<StudentDossierResponse>> getAllStudents(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.getAllStudents(pageable));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<StudentDossierResponse> getStudent(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getStudentById(id));
    }

    // ── DOCUMENTS MÉDICAUX ───────────────────────────────────────

    @GetMapping("/documents/pending")
    public ResponseEntity<List<MedicalDocumentResponse>> getPending() {
        return ResponseEntity.ok(service.getPendingDocuments());
    }

    @PutMapping("/documents/{id}/validate")
    public ResponseEntity<MedicalDocumentResponse> validate(
            @PathVariable Long id,
            Authentication auth) {
        Long agentId = getAgentId(auth);
        return ResponseEntity.ok(service.validateDocument(id, agentId));
    }

    @PutMapping("/documents/{id}/reject")
    public ResponseEntity<MedicalDocumentResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectDocumentRequest request,
            Authentication auth) {
        Long agentId = getAgentId(auth);
        return ResponseEntity.ok(
                service.rejectDocument(id, request.getReason(), agentId));
    }

    // ── ABSENCES PROLONGÉES ──────────────────────────────────────

    @GetMapping("/absences/prolonged")
    public ResponseEntity<List<AbsenceResponse>> getProlongedAbsences(
            @RequestParam(defaultValue = "3") int days) {
        return ResponseEntity.ok(service.getProlongedAbsences(days));
    }

    // ── NOTES ACADÉMIQUES ────────────────────────────────────────

    @GetMapping("/students/{id}/grades")
    public ResponseEntity<List<AcademicRecordResponse>> getGrades(
            @PathVariable Long id,
            @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(service.getGradesByStudent(id, semester));
    }

    @PostMapping("/students/{id}/grades")
    public ResponseEntity<AcademicRecordResponse> addGrade(
            @PathVariable Long id,
            @RequestBody AcademicRecord record) {
        return ResponseEntity.ok(service.addGrade(id, record));
    }

    // ── HELPER PRIVÉ ─────────────────────────────────────────────

    private Long getAgentId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getUserId();
    }
}