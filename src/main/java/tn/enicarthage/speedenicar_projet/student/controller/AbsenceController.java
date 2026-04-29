package tn.enicarthage.speedenicar_projet.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.student.dto.response.AbsenceResponse;
import tn.enicarthage.speedenicar_projet.student.entity.Absence;
import tn.enicarthage.speedenicar_projet.student.service.AbsenceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class AbsenceController {

    private final AbsenceService absenceService;

    // GET /api/v1/student/absences
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AbsenceResponse>>> getAbsences(
            @AuthenticationPrincipal UserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {

        Long userId = Long.parseLong(user.getUsername());
        Page<AbsenceResponse> page = absenceService
                .getAbsences(userId, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    // GET /api/v1/student/absences/unjustified
    @GetMapping("/unjustified")
    public ResponseEntity<ApiResponse<List<AbsenceResponse>>> getUnjustified(
            @AuthenticationPrincipal UserDetails user) {

        Long userId = Long.parseLong(user.getUsername());
        List<AbsenceResponse> absences = absenceService
                .getUnjustifiedAbsences(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(absences));
    }

    // GET /api/v1/student/absences/period?startDate=2025-01-01&endDate=2025-06-30
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<AbsenceResponse>>> getByPeriod(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = Long.parseLong(user.getUsername());
        List<AbsenceResponse> absences = absenceService
                .getAbsencesByPeriod(userId, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(absences));
    }

    // GET /api/v1/student/absences/count
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnjustifiedCount(
            @AuthenticationPrincipal UserDetails user) {

        Long userId = Long.parseLong(user.getUsername());
        Long count = absenceService.countUnjustifiedAbsences(userId);

        return ResponseEntity.ok(
                ApiResponse.ok(Map.of("unjustifiedCount", count)));
    }

    // GET /api/v1/student/absences/prolonged
    @GetMapping("/prolonged")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> hasProlonged(
            @AuthenticationPrincipal UserDetails user) {

        Long userId = Long.parseLong(user.getUsername());
        boolean hasProlonged = absenceService.hasProlongedAbsences(userId);

        return ResponseEntity.ok(
                ApiResponse.ok(Map.of("hasProlongedAbsences", hasProlonged)));
    }

    // ── Mapper entité → response ─────────────────────────────

    private AbsenceResponse toResponse(Absence absence) {
        return AbsenceResponse.builder()
                .id(absence.getId())
                .startDate(absence.getStartDate())
                .endDate(absence.getEndDate())
                .reason(absence.getReason())
                .justified(absence.getJustified())
                .subject(absence.getSubject())
                .reportedBy(absence.getReportedBy())
                .durationInDays(absence.getDurationInDays())
                .isProlonged(absence.isProlonged())
                .createdAt(absence.getCreatedAt())
                .build();
    }
}