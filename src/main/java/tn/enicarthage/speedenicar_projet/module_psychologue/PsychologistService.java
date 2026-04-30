package tn.enicarthage.speedenicar_projet.module_psychologue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.common.enums.AlertSeverity;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentStatus;
import tn.enicarthage.speedenicar_projet.common.enums.DocStatus;
import tn.enicarthage.speedenicar_projet.common.exception.BusinessException;
import tn.enicarthage.speedenicar_projet.common.exception.ResourceNotFoundException;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.AppointmentRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.TimeSlotRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.document.MedicalDocument;
import tn.enicarthage.speedenicar_projet.module_psychologue.document.MedicalDocumentRepository; // 👈 IMPORT AJOUTÉ
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.*;
import tn.enicarthage.speedenicar_projet.module_psychologue.entity.ConfidentialRecord;
import tn.enicarthage.speedenicar_projet.module_psychologue.entity.TimeSlot;
import tn.enicarthage.speedenicar_projet.module_psychologue.repository.ConfidentialRecordRepository;
import tn.enicarthage.speedenicar_projet.scolarity.dto.response.MedicalDocumentResponse;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.AbsenceRepository;
import tn.enicarthage.speedenicar_projet.student.repository.MoodEntryRepository;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PsychologistService {

    private final UserRepository userRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final ConfidentialRecordRepository recordRepo;
    private final AppointmentRepository appointmentRepo;
    private final StudentProfileRepository studentProfileRepo;
    private final MoodEntryRepository moodEntryRepo;
    private final AbsenceRepository absenceRepo;
    private final MedicalDocumentRepository medicalDocumentRepository; // 👈 DÉCLARATION AJOUTÉE

    // ═══════════════════════════════════════════════════════
    //  PROFILE
    // ═══════════════════════════════════════════════════════

    public User getPsychologistById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    // ═══════════════════════════════════════════════════════
    //  DASHBOARD
    // ═══════════════════════════════════════════════════════

    public PsychologistDashboardResponse getDashboard(Long userId) {
        User psy = getPsychologistById(userId);
        Long psyId = psy.getId();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        long todayAppts = appointmentRepo
                .countByPsychologistIdAndDateTimeBetweenAndStatusNotAndDeletedFalse(
                        psyId, todayStart, todayEnd, AppointmentStatus.CANCELLED);

        long pendingReqs = appointmentRepo
                .countByPsychologistIdAndStatusAndDeletedFalse(psyId, AppointmentStatus.PENDING);

        List<Long> patientIds = recordRepo.findDistinctStudentIds(psyId);

        List<ConfidentialRecord> criticalRecords = recordRepo
                .findByPsychologistAndRiskLevels(psyId, List.of(AlertSeverity.HIGH, AlertSeverity.CRITICAL));

        List<ConfidentialRecord> followUps = recordRepo.findPendingFollowUps(psyId);

        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);

        long weekSessions = recordRepo.countByPsychologistIdAndSessionDateBetweenAndDeletedFalse(
                psyId, weekStart, LocalDate.now());
        long monthSessions = recordRepo.countByPsychologistIdAndSessionDateBetweenAndDeletedFalse(
                psyId, monthStart, LocalDate.now());

        return PsychologistDashboardResponse.builder()
                .psychologistName(psy.getFullName())
                .specialization(psy.getSpecialty())
                .todayAppointments(todayAppts)
                .pendingRequests(pendingReqs)
                .totalPatients((long) patientIds.size())
                .criticalAlerts((long) criticalRecords.size())
                .pendingFollowUps((long) followUps.size())
                .weekSessions(weekSessions)
                .monthSessions(monthSessions)
                .build();
    }

    // ═══════════════════════════════════════════════════════
    //  SCHEDULE / TIME SLOTS
    // ═══════════════════════════════════════════════════════

    public List<TimeSlotResponse> getSchedule(Long userId) {
        User psy = getPsychologistById(userId);
        return timeSlotRepo.findByPsychologistIdAndDeletedFalseOrderByDayOfWeekAscStartTimeAsc(psy.getId())
                .stream()
                .map(this::toTimeSlotResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TimeSlotResponse> updateSchedule(Long userId, ScheduleUpdateRequest request) {
        User psy = getPsychologistById(userId);

        for (int i = 0; i < request.getSlots().size(); i++) {
            TimeSlotRequest slot = request.getSlots().get(i);
            for (int j = i + 1; j < request.getSlots().size(); j++) {
                TimeSlotRequest other = request.getSlots().get(j);
                if (slot.getDayOfWeek().equals(other.getDayOfWeek())) {
                    boolean overlaps = slot.getStartTime().isBefore(other.getEndTime())
                            && other.getStartTime().isBefore(slot.getEndTime());
                    if (overlaps) {
                        throw new BusinessException("Chevauchement détecté le " + slot.getDayOfWeek());
                    }
                }
            }
        }

        timeSlotRepo.deleteByPsychologistId(psy.getId());
        timeSlotRepo.flush();

        List<TimeSlot> newSlots = request.getSlots().stream()
                .map(req -> {
                    TimeSlot ts = new TimeSlot();
                    ts.setPsychologist(psy);

                    // Note : assure-toi que ts.setDayOfWeek() accepte bien ce que tu lui envoies (String ou Enum)
                    ts.setDayOfWeek(req.getDayOfWeek() != null ? DayOfWeek.valueOf(req.getDayOfWeek().name()) : null);
                    ts.setStartTime(req.getStartTime() != null ? req.getStartTime().toString() : null);
                    ts.setEndTime(req.getEndTime() != null ? req.getEndTime().toString() : null);

                    ts.setAvailable(req.getAvailable() != null ? req.getAvailable() : true);
                    ts.setDeleted(false);

                    return ts;
                })
                .collect(Collectors.toList());

        List<TimeSlot> saved = timeSlotRepo.saveAll(newSlots);

        return saved.stream().map(this::toTimeSlotResponse).collect(Collectors.toList());
    }

    public List<TimeSlotResponse> getAvailableSlots(Long psychologistUserId, DayOfWeek dayOfWeek) {
        User psy = getPsychologistById(psychologistUserId);

        return timeSlotRepo.findByPsychologistIdAndDayOfWeekAndAvailableTrueAndDeletedFalse(
                        psy.getId(),
                        dayOfWeek // ou dayOfWeek.name() si ton repo attend un String
                )
                .stream()
                .map(this::toTimeSlotResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════
    //  APPOINTMENTS & DOCUMENTS
    // ═══════════════════════════════════════════════════════

    public Page<AppointmentResponse> getAppointments(Long userId, Pageable pageable) {
        return appointmentRepo.findByPsychologistIdAndDeletedFalseOrderByDateTimeDesc(userId, pageable)
                .map(this::toAppointmentResponse);
    }

    public List<MedicalDocumentResponse> getStudentMedicalDocuments(Long psyUserId, Long studentId) {
        List<MedicalDocument> documents = medicalDocumentRepository
                .findByStudentIdOrderByCreatedAtDesc(studentId);

        return documents.stream()
                .map(this::mapToMedicalDocumentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalDocumentResponse updateDocumentStatus(Long psyUserId, Long documentId, String status, String reason) {
        MedicalDocument document = medicalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document introuvable avec l'ID: " + documentId));

        // 👈 1. On convertit le String en Enum DocStatus
        DocStatus newStatus = DocStatus.valueOf(status.toUpperCase());

        // 👈 2. On passe l'Enum à l'entité
        document.setStatus(newStatus);

        // On vérifie avec l'Enum
        if (newStatus == DocStatus.REJECTED && reason != null) {
            document.setRejectionReason(reason);
        }

        medicalDocumentRepository.save(document);

        return mapToMedicalDocumentResponse(document);
    }

    public List<AppointmentResponse> getPendingRequests(Long userId) {
        return appointmentRepo.findPendingRequests(userId).stream()
                .map(this::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getTodayAppointments(Long userId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        return appointmentRepo.findByPsychologistAndDateRange(
                        userId, todayStart, todayEnd,
                        List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED))
                .stream()
                .map(this::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse confirmAppointment(Long userId, Long appointmentId) {
        Appointment appointment = findAppointmentForPsychologist(userId, appointmentId);
        appointment.confirm();
        return toAppointmentResponse(appointmentRepo.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long userId, Long appointmentId, String reason) {
        Appointment appointment = findAppointmentForPsychologist(userId, appointmentId);
        appointment.cancel(userId, reason);
        return toAppointmentResponse(appointmentRepo.save(appointment));
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long userId, Long appointmentId, String notes) {
        Appointment appointment = findAppointmentForPsychologist(userId, appointmentId);
        appointment.complete(notes);
        return toAppointmentResponse(appointmentRepo.save(appointment));
    }

    // ═══════════════════════════════════════════════════════
    //  CONFIDENTIAL RECORDS
    // ═══════════════════════════════════════════════════════

    public Page<RecordResponse> getStudentRecords(Long userId, Long studentId, Pageable pageable) {
        User psy = getPsychologistById(userId);
        return recordRepo.findByStudentIdAndPsychologistIdAndDeletedFalseOrderBySessionDateDesc(
                        studentId, psy.getId(), pageable)
                .map(this::toRecordResponse);
    }

    public Page<RecordResponse> getAllMyRecords(Long userId, Pageable pageable) {
        User psy = getPsychologistById(userId);
        return recordRepo.findByPsychologistIdAndDeletedFalseOrderBySessionDateDesc(psy.getId(), pageable)
                .map(this::toRecordResponse);
    }

    @Transactional
    public RecordResponse createRecord(Long userId, CreateRecordRequest request) {
        User psy = getPsychologistById(userId);
        StudentProfile student = studentProfileRepo.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "id", request.getStudentId()));

        ConfidentialRecord record = ConfidentialRecord.builder()
                .student(student)
                .psychologist(psy)
                .sessionDate(request.getSessionDate())
                .observations(request.getObservations())
                .riskLevel(request.getRiskLevel() != null ? request.getRiskLevel() : AlertSeverity.LOW)
                .recommendations(request.getRecommendations())
                .followUpRequired(request.getFollowUpRequired() != null ? request.getFollowUpRequired() : false)
                .nextSessionDate(request.getNextSessionDate())
                .sessionDurationMinutes(request.getSessionDurationMinutes() != null ? request.getSessionDurationMinutes() : 30)
                .interventions(request.getInterventions())
                .studentProgress(request.getStudentProgress())
                .build();

        return toRecordResponse(recordRepo.save(record));
    }

    @Transactional
    public RecordResponse updateRecord(Long userId, Long recordId, UpdateRecordRequest request) {
        User psy = getPsychologistById(userId);
        ConfidentialRecord record = recordRepo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("ConfidentialRecord", "id", recordId));

        if (!record.getPsychologist().getId().equals(psy.getId())) {
            throw new BusinessException("Droit de modification refusé");
        }

        if (request.getObservations() != null) record.setObservations(request.getObservations());
        if (request.getRiskLevel() != null) record.setRiskLevel(request.getRiskLevel());
        if (request.getRecommendations() != null) record.setRecommendations(request.getRecommendations());
        if (request.getFollowUpRequired() != null) record.setFollowUpRequired(request.getFollowUpRequired());
        if (request.getNextSessionDate() != null) record.setNextSessionDate(request.getNextSessionDate());
        if (request.getSessionDurationMinutes() != null) record.setSessionDurationMinutes(request.getSessionDurationMinutes());
        if (request.getInterventions() != null) record.setInterventions(request.getInterventions());
        if (request.getStudentProgress() != null) record.setStudentProgress(request.getStudentProgress());

        return toRecordResponse(recordRepo.save(record));
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        User psy = getPsychologistById(userId);
        ConfidentialRecord record = recordRepo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("ConfidentialRecord", "id", recordId));

        if (!record.getPsychologist().getId().equals(psy.getId())) {
            throw new BusinessException("Droit de suppression refusé");
        }
        record.setDeleted(true);
        recordRepo.save(record);
    }

    // ═══════════════════════════════════════════════════════
    //  AT-RISK STUDENTS / ALERTS
    // ═══════════════════════════════════════════════════════

    public List<StudentAlertResponse> getStudentsAtRisk(Long userId) {
        User psy = getPsychologistById(userId);
        List<Long> studentIds = recordRepo.findDistinctStudentIds(psy.getId());
        List<StudentAlertResponse> alerts = new ArrayList<>();
        LocalDate twoWeeksAgo = LocalDate.now().minusWeeks(2);

        for (Long studentId : studentIds) {
            StudentProfile student = studentProfileRepo.findById(studentId).orElse(null);
            if (student == null) continue;

            List<ConfidentialRecord> latestRecords = recordRepo.findLatestByStudent(studentId, PageRequest.of(0, 1));
            ConfidentialRecord latest = latestRecords.isEmpty() ? null : latestRecords.get(0);
            Double moodAvg = moodEntryRepo.getAverageMood(studentId, twoWeeksAgo, LocalDate.now());
            Long unjustifiedAbs = absenceRepo.countUnjustified(studentId);

            List<String> reasons = new ArrayList<>();
            AlertSeverity currentRisk = latest != null ? latest.getRiskLevel() : AlertSeverity.LOW;

            if (currentRisk == AlertSeverity.HIGH || currentRisk == AlertSeverity.CRITICAL) reasons.add("Risque clinique élevé");
            if (moodAvg != null && moodAvg < 2.5) reasons.add("Humeur basse");
            if (unjustifiedAbs != null && unjustifiedAbs > 3) reasons.add("Absences répétées");

            if (!reasons.isEmpty()) {
                alerts.add(StudentAlertResponse.builder()
                        .studentId(studentId)
                        .studentName(student.getUser().getFullName())
                        .currentRiskLevel(currentRisk)
                        .recentMoodAverage(moodAvg)
                        .alertReasons(reasons)
                        .build());
            }
        }
        return alerts;
    }

    public List<RecordResponse> getPendingFollowUps(Long userId) {
        User psy = getPsychologistById(userId);
        return recordRepo.findPendingFollowUps(psy.getId()).stream().map(this::toRecordResponse).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════
    //  PRIVATE HELPERS & MAPPERS
    // ═══════════════════════════════════════════════════════

    private Appointment findAppointmentForPsychologist(Long userId, Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));
        if (!appointment.getPsychologist().getId().equals(userId)) {
            throw new BusinessException("Ce rendez-vous ne vous appartient pas");
        }
        return appointment;
    }

    private TimeSlotResponse toTimeSlotResponse(TimeSlot ts) {
        DayOfWeek day = ts.getDayOfWeek() != null ? DayOfWeek.valueOf(String.valueOf(ts.getDayOfWeek())) : null;
        LocalTime start = ts.getStartTime() != null ? LocalTime.parse(ts.getStartTime()) : null;
        LocalTime end = ts.getEndTime() != null ? LocalTime.parse(ts.getEndTime()) : null;

        // 👈 SÉCURITÉ AJOUTÉE : gestion propre du boolean pour éviter un NullPointerException
        boolean isAvailable = true;
        if (ts.isAvailable() != true) {
            isAvailable = ts.isAvailable();
        } else if (ts.isAvailable() != true) {
            isAvailable = ts.isAvailable();
        }

        return TimeSlotResponse.builder()
                .id(ts.getId())
                .dayOfWeek(day)
                .startTime(start)
                .endTime(end)
                .available(isAvailable)
                .build();
    }

    private AppointmentResponse toAppointmentResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .studentId(a.getStudent().getId())
                .studentName(a.getStudent().getUser().getFullName())
                .studentDepartment(a.getStudent().getDepartment())
                .psychologistId(a.getPsychologist().getId())
                // 👈 CORRECTION DU BUG ICI : On récupère bien le nom du Psy et non de l'étudiant
                .psychologistName(a.getPsychologist().getFullName())
                .dateTime(a.getDateTime())
                .status(a.getStatus())
                .type(a.getType())
                .build();
    }

    private RecordResponse toRecordResponse(ConfidentialRecord r) {
        return RecordResponse.builder()
                .id(r.getId())
                .studentId(r.getStudent().getId())
                .studentName(r.getStudent().getUser().getFullName())
                .sessionDate(r.getSessionDate())
                .riskLevel(r.getRiskLevel())
                .followUpRequired(r.getFollowUpRequired())
                .build();
    }

    // 👈 MÉTHODE AJOUTÉE : Pour mapper les documents médicaux
    private MedicalDocumentResponse mapToMedicalDocumentResponse(MedicalDocument doc) {
        return MedicalDocumentResponse.builder()
                .id(doc.getId())
                .fileName(doc.getFileName()) // Adapte "getTitle()" si le nom est "getFileName()" dans ton entité
                .status(doc.getStatus())
                .rejectionReason(doc.getRejectionReason())
                //.createdAt(doc.getCreatedAt()) // Ajoute le mapping de la date si nécessaire
                .build();
    }
}