package tn.enicarthage.speedenicar_projet.scolarity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.common.enums.DocStatus;
import tn.enicarthage.speedenicar_projet.module_psychologue.document.MedicalDocument;
import tn.enicarthage.speedenicar_projet.scolarity.dto.response.*;
import tn.enicarthage.speedenicar_projet.scolarity.repository.AcademicRecordRepository;
import tn.enicarthage.speedenicar_projet.student.entity.Absence;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.student.entity.AcademicRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import tn.enicarthage.speedenicar_projet.common.enums.Role;
import tn.enicarthage.speedenicar_projet.scolarity.dto.request.CreateStudentRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
// ⚠️ Ces repositories viennent du code de ton amie —
//    remplace par les vrais imports quand tu vois leurs noms exacts
import tn.enicarthage.speedenicar_projet.module_psychologue.document.MedicalDocumentRepository;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;
import tn.enicarthage.speedenicar_projet.student.repository.AbsenceRepository;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScolarityService {

    private final MedicalDocumentRepository  documentRepo;
    private final StudentProfileRepository   studentRepo;
    private final AbsenceRepository          absenceRepo;
    private final AcademicRecordRepository   academicRepo;
    private final UserRepository             userRepo;
    private final PasswordEncoder passwordEncoder;
    // ════════════════════════════════════════════════════════
    // DOSSIERS ÉTUDIANTS
    // ════════════════════════════════════════════════════════

    public Page<StudentDossierResponse> getAllStudents(Pageable pageable) {
        return studentRepo.findAll(pageable)
                .map(this::toStudentDossierLight);
    }

    public StudentDossierResponse getStudentById(Long id) {
        StudentProfile student = studentRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Étudiant introuvable avec l'id : " + id));
        return toStudentDossierFull(student);
    }

    @Transactional
    public StudentDossierResponse createStudent(CreateStudentRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé : " + req.getEmail());
        }
        if (studentRepo.existsByStudentId(req.getStudentId())) {
            throw new IllegalArgumentException("Numéro étudiant déjà utilisé : " + req.getStudentId());
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPassword(passwordEncoder.encode("Student123!"));
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        User savedUser = userRepo.save(user);

        StudentProfile profile = new StudentProfile();
        profile.setUser(savedUser);
        profile.setStudentId(req.getStudentId());
        profile.setDepartment(req.getDepartment());
        profile.setLevel(req.getLevel());
        profile.setEnrollmentYear(req.getEnrollmentYear());
        StudentProfile savedProfile = studentRepo.save(profile);

        log.info("Étudiant créé : {}", req.getEmail());
        return toStudentDossierLight(savedProfile);
    }

    // ════════════════════════════════════════════════════════
    // DOCUMENTS MÉDICAUX
    // ════════════════════════════════════════════════════════

    public List<MedicalDocumentResponse> getPendingDocuments() {
        return documentRepo.findByStatusAndDeletedFalseOrderByCreatedAtAsc(
                        DocStatus.PENDING, Pageable.unpaged())
                .stream()
                .map(this::toDocumentResponse)
                .toList();
    }

    @Transactional
    public MedicalDocumentResponse validateDocument(Long docId, Long agentId) {
        MedicalDocument doc = documentRepo.findById(docId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Document introuvable : " + docId));

        if (!doc.isPending()) {
            throw new IllegalStateException(
                    "Ce document a déjà été traité (statut : " + doc.getStatus() + ")");
        }

        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Agent introuvable : " + agentId));

        // ✅ utilise la méthode de ton amie directement
        doc.validate(agent);
        documentRepo.save(doc);

        log.info("✅ Document {} validé par l'agent {}", docId, agentId);
        return toDocumentResponse(doc);
    }

    @Transactional
    public MedicalDocumentResponse rejectDocument(Long docId,
                                                  String reason,
                                                  Long agentId) {
        MedicalDocument doc = documentRepo.findById(docId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Document introuvable : " + docId));

        if (!doc.isPending()) {
            throw new IllegalStateException(
                    "Ce document a déjà été traité (statut : " + doc.getStatus() + ")");
        }

        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Agent introuvable : " + agentId));

        // ✅ utilise la méthode de ton amie directement
        doc.reject(agent, reason);
        documentRepo.save(doc);

        log.info("❌ Document {} rejeté par l'agent {}. Motif : {}", docId, agentId, reason);
        return toDocumentResponse(doc);
    }

    // ════════════════════════════════════════════════════════
    // ABSENCES PROLONGÉES
    // ════════════════════════════════════════════════════════

    public List<AbsenceResponse> getProlongedAbsences(int thresholdDays) {
        LocalDate threshold = LocalDate.now().minusDays(thresholdDays);
        return absenceRepo.findAll()
                .stream()
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .filter(a -> Boolean.FALSE.equals(a.getJustified()))
                .filter(a -> a.getStartDate() != null &&
                        a.getStartDate().isBefore(threshold))
                .map(this::toAbsenceResponse)
                .toList();
    }
    @Transactional
    public AbsenceResponse justifyAbsence(Long absenceId, boolean justified) {
        Absence absence = absenceRepo.findById(absenceId)
                .orElseThrow(() -> new NoSuchElementException("Absence introuvable : " + absenceId));
        absence.setJustified(justified);
        absenceRepo.save(absence);
        log.info("Absence {} marquée comme {}", absenceId, justified ? "justifiée" : "non justifiée");
        return toAbsenceResponse(absence);
    }

    // ════════════════════════════════════════════════════════
    // NOTES ACADÉMIQUES
    // ════════════════════════════════════════════════════════

    public List<AcademicRecordResponse> getGradesByStudent(Long studentId,
                                                           String semester) {
        List<AcademicRecord> records = (semester != null && !semester.isBlank())
                ? academicRepo.findByStudentIdAndSemester(studentId, semester)
                : academicRepo.findActiveByStudentId(studentId);

        return records.stream()
                .map(this::toAcademicRecordResponse)
                .toList();
    }

    @Transactional
    public AcademicRecordResponse addGrade(Long studentId,
                                           AcademicRecord record) {
        StudentProfile student = studentRepo.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Étudiant introuvable : " + studentId));
        record.setStudent(student);
        AcademicRecord saved = academicRepo.save(record);
        log.info("📝 Note ajoutée pour l'étudiant {}", studentId);
        return toAcademicRecordResponse(saved);
    }

    // ════════════════════════════════════════════════════════
    // MAPPERS PRIVÉS  (Entity → DTO)
    // ════════════════════════════════════════════════════════

    private StudentDossierResponse toStudentDossierLight(StudentProfile s) {
        return StudentDossierResponse.builder()
                .id(s.getId())
                .studentId(s.getStudentId())
                .firstName(s.getUser().getFirstName())
                .lastName(s.getUser().getLastName())
                .email(s.getUser().getEmail())
                .department(s.getDepartment())
                .level(s.getLevel())
                .enrollmentYear(s.getEnrollmentYear())
                .unjustifiedAbsencesCount(s.countUnjustifiedAbsences())
                .averageGrade(s.getAverageGrade(null))
                .build();
    }

    private StudentDossierResponse toStudentDossierFull(StudentProfile s) {
        List<MedicalDocumentResponse> pendingDocs = s.getMedicalDocuments()
                .stream()
                .filter(d -> DocStatus.PENDING.equals(d.getStatus()))
                .map(this::toDocumentResponse)
                .toList();

        List<AbsenceResponse> prolonged = s.getAbsences()
                .stream()
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .filter(Absence::isProlonged)
                .map(this::toAbsenceResponse)
                .toList();

        List<AcademicRecordResponse> grades =
                academicRepo.findActiveByStudentId(s.getId())
                        .stream()
                        .map(this::toAcademicRecordResponse)
                        .toList();

        return StudentDossierResponse.builder()
                .id(s.getId())
                .studentId(s.getStudentId())
                .firstName(s.getUser().getFirstName())
                .lastName(s.getUser().getLastName())
                .email(s.getUser().getEmail())
                .department(s.getDepartment())
                .level(s.getLevel())
                .enrollmentYear(s.getEnrollmentYear())
                .pendingDocuments(pendingDocs)
                .prolongedAbsences(prolonged)
                .grades(grades)
                .unjustifiedAbsencesCount(s.countUnjustifiedAbsences())
                .averageGrade(s.getAverageGrade(null))
                .build();
    }

    private MedicalDocumentResponse toDocumentResponse(MedicalDocument d) {
        String studentName = d.getStudent().getUser().getFirstName()
                + " " + d.getStudent().getUser().getLastName();
        return MedicalDocumentResponse.builder()
                .id(d.getId())
                .studentId(d.getStudent().getId())
                .studentName(studentName)
                .fileName(d.getFileName())
                .fileType(d.getFileType())
                .fileSize(d.getFileSize())
                .status(d.getStatus())
                .rejectionReason(d.getRejectionReason())
                .createdAt(d.getCreatedAt())
                .validationDate(d.getValidationDate())
                .build();
    }

    private AbsenceResponse toAbsenceResponse(Absence a) {
        String studentName = a.getStudent().getUser().getFirstName()
                + " " + a.getStudent().getUser().getLastName();
        return AbsenceResponse.builder()
                .id(a.getId())
                .studentId(a.getStudent().getId())
                .studentName(studentName)
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .reason(a.getReason())
                .justified(a.getJustified())
                .durationDays(a.getDurationInDays())
                .prolonged(a.isProlonged())
                .build();
    }

    private AcademicRecordResponse toAcademicRecordResponse(AcademicRecord r) {
        return AcademicRecordResponse.builder()
                .id(r.getId())
                .subject(r.getSubject())
                .grade(r.getGrade())
                .maxGrade(r.getMaxGrade())
                .percentage(r.getPercentage())
                .semester(r.getSemester())
                .academicYear(r.getAcademicYear())
                .passing(r.isPassing())
                .build();
    }
}