package tn.enicarthage.speedenicar_projet.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.student.dto.response.AcademicRecordResponse;
import tn.enicarthage.speedenicar_projet.student.entity.AcademicRecord;
import tn.enicarthage.speedenicar_projet.student.service.AcademicRecordService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class AcademicRecordController {

    private final AcademicRecordService academicRecordService;

    // GET /api/v1/student/grades?semester=S1
    @GetMapping("/grades")
    public ResponseEntity<ApiResponse<List<AcademicRecordResponse>>> getGrades(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String semester) {

        Long userId = Long.parseLong(user.getUsername());
        List<AcademicRecordResponse> grades = academicRecordService
                .getGrades(userId, semester)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(grades));
    }

    // GET /api/v1/student/grades/average?semester=S1
    @GetMapping("/grades/average")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getAverage(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String semester) {

        Long userId = Long.parseLong(user.getUsername());
        double average = academicRecordService.getAverage(userId, semester);

        return ResponseEntity.ok(
                ApiResponse.ok(Map.of("average", average)));
    }

    // ── Mapper entité → response ─────────────────────────────

    private AcademicRecordResponse toResponse(AcademicRecord record) {
        return AcademicRecordResponse.builder()
                .id(record.getId())
                .subject(record.getSubject())
                .grade(record.getGrade())
                .maxGrade(record.getMaxGrade())
                .percentage(record.getPercentage())
                .isPassing(record.isPassing())
                .semester(record.getSemester())
                .academicYear(record.getAcademicYear())
                .coefficient(record.getCoefficient())
                .build();
    }
}