package tn.enicarthage.speedenicar_projet.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.student.dto.response.RiskScoreResponse;
import tn.enicarthage.speedenicar_projet.student.service.RiskScoreService;

@RestController
@RequestMapping("/api/v1/student/risk-score")
@RequiredArgsConstructor
public class RiskScoreController {

    private final RiskScoreService riskScoreService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'PSYCHOLOGIST', 'ADMIN')")
    public ResponseEntity<ApiResponse<RiskScoreResponse>> getRiskScore(
            @AuthenticationPrincipal UserDetails user) {

        Long userId = Long.parseLong(user.getUsername());
        RiskScoreResponse response = riskScoreService.calculateRiskScore(userId);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('PSYCHOLOGIST', 'ADMIN')")
    public ResponseEntity<ApiResponse<RiskScoreResponse>> getRiskScoreByStudentId(
            @PathVariable Long studentId) {

        RiskScoreResponse response = riskScoreService
                .calculateRiskScoreByProfileId(studentId);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}