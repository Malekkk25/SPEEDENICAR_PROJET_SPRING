package tn.enicarthage.speedenicar_projet.student.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.student.dto.request.UpdateProfileRequest;
import tn.enicarthage.speedenicar_projet.student.dto.response.StudentProfileResponse;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.service.StudentProfileService;

@RestController
@RequestMapping("/api/v1/student/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    // GET /api/v1/student/profile
    @GetMapping
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails user) {

        Long userId = Long.parseLong(user.getUsername());
        StudentProfile profile = studentProfileService.getProfile(userId);

        return ResponseEntity.ok(ApiResponse.ok(toResponse(profile)));
    }

    // PUT /api/v1/student/profile
    @PutMapping
    public ResponseEntity<ApiResponse<StudentProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody UpdateProfileRequest request) {

        Long userId = Long.parseLong(user.getUsername());

        StudentProfile updated = StudentProfile.builder()
                .department(request.getDepartment())
                .level(request.getLevel())
                .dateOfBirth(request.getDateOfBirth())
                .build();

        StudentProfile saved = studentProfileService.updateProfile(userId, updated);

        return ResponseEntity.ok(
                ApiResponse.ok(toResponse(saved), "Profil mis à jour avec succès"));
    }

    // GET /api/v1/student/profile/stats
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getProfileWithStats(
            @AuthenticationPrincipal UserDetails user) {

        Long userId = Long.parseLong(user.getUsername());
        StudentProfile profile = studentProfileService.getProfileWithStats(userId);

        return ResponseEntity.ok(ApiResponse.ok(toResponse(profile)));
    }

    // ── Mapper entité → response ─────────────────────────────

    private StudentProfileResponse toResponse(StudentProfile profile) {
        return StudentProfileResponse.builder()
                .id(profile.getId())
                .studentId(profile.getStudentId())
                .firstName(profile.getUser().getFirstName())
                .lastName(profile.getUser().getLastName())
                .email(profile.getUser().getEmail())
                .phone(profile.getUser().getPhone())
                .avatarUrl(profile.getUser().getAvatarUrl())
                .department(profile.getDepartment())
                .level(profile.getLevel())
                .enrollmentYear(profile.getEnrollmentYear())
                .dateOfBirth(profile.getDateOfBirth())
                .averageGrade(profile.getAverageGrade(null))
                .unjustifiedAbsences(profile.countUnjustifiedAbsences())
                .build();
    }
}