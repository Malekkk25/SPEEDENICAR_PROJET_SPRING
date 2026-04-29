package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.student.dto.response.StudentProfileResponse;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfileByUserId(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil non trouvé"));

        // Convertir l'entité en DTO (Response)
        return StudentProfileResponse.builder()
                .id(profile.getId())
                .studentId(profile.getStudentId())
                .firstName(profile.getUser().getFirstName())
                .lastName(profile.getUser().getLastName())
                .email(profile.getUser().getEmail())
                .department(profile.getDepartment())
                .level(profile.getLevel())
                .build();
    }

    private StudentProfileResponse toResponse(StudentProfile profile) {
        return StudentProfileResponse.builder()
                .id(profile.getId())
                .studentId(profile.getStudentId())
                // On récupère les infos depuis l'objet User lié au profil
                .firstName(profile.getUser().getFirstName())
                .lastName(profile.getUser().getLastName())
                .email(profile.getUser().getEmail())
                .department(profile.getDepartment())
                .level(profile.getLevel())
                .enrollmentYear(profile.getEnrollmentYear())
                .dateOfBirth(profile.getDateOfBirth())
                .build();
    }
}