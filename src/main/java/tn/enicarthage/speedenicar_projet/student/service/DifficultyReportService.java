package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.common.enums.DifficultyType;
import tn.enicarthage.speedenicar_projet.common.enums.UrgencyLevel;
import tn.enicarthage.speedenicar_projet.student.dto.request.DifficultyReportRequest;
import tn.enicarthage.speedenicar_projet.student.dto.response.DifficultyReportResponse;
import tn.enicarthage.speedenicar_projet.student.entity.DifficultyReport;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.DifficultyReportRepository;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DifficultyReportService {

    private final DifficultyReportRepository reportRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public DifficultyReportResponse createReport(String email, DifficultyReportRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        StudentProfile student = studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil étudiant non trouvé"));

        DifficultyReport report = DifficultyReport.builder()
                .student(student)
                .type(DifficultyType.valueOf(request.getType()))
                .description(request.getDescription())
                .urgency(UrgencyLevel.valueOf(request.getUrgency()))
                // status et createdAt sont gérés par le @PrePersist dans l'entité
                .build();

        DifficultyReport savedReport = reportRepository.save(report);
        return mapToDto(savedReport);
    }

    public List<DifficultyReportResponse> getStudentReports(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        StudentProfile student = studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil étudiant non trouvé"));

        List<DifficultyReport> reports = reportRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());

        return reports.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private DifficultyReportResponse mapToDto(DifficultyReport report) {
        return DifficultyReportResponse.builder()
                .id(report.getId())
                .type(report.getType().name())
                .description(report.getDescription())
                .urgency(report.getUrgency().name())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
