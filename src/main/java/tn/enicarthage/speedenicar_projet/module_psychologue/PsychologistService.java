package tn.enicarthage.speedenicar_projet.module_psychologue;




import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.AppointmentRepository;
import tn.enicarthage.speedenicar_projet.common.enums.AlertSeverity;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentStatus;
import tn.enicarthage.speedenicar_projet.common.exception.BusinessException;
import tn.enicarthage.speedenicar_projet.common.exception.ResourceNotFoundException;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.*;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.*;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.*;
import tn.enicarthage.speedenicar_projet.module_psychologue.entity.ConfidentialRecord;
import tn.enicarthage.speedenicar_projet.module_psychologue.entity.PsychologistProfile;
import tn.enicarthage.speedenicar_projet.module_psychologue.entity.TimeSlot;
import tn.enicarthage.speedenicar_projet.module_psychologue.repository.ConfidentialRecordRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.repository.PsychologistProfileRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.AbsenceRepository;
import tn.enicarthage.speedenicar_projet.student.repository.MoodEntryRepository;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;


import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PsychologistService {

    private final PsychologistProfileRepository psychologistRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final ConfidentialRecordRepository recordRepo;
    private final AppointmentRepository appointmentRepo;
    private final StudentProfileRepository studentProfileRepo;
    private final MoodEntryRepository moodEntryRepo;
    private final AbsenceRepository absenceRepo;

    // ═══════════════════════════════════════════════════════
    //  PROFILE
    // ═══════════════════════════════════════════════════════

    public PsychologistProfile getProfileByUserId(Long userId) {
        return psychologistRepo.findByUserIdWithUser(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PsychologistProfile", "userId", userId));
    }

    // ═══════════════════════════════════════════════════════
    //  DASHBOARD
    // ═══════════════════════════════════════════════════════

    public PsychologistDashboardResponse getDashboard(Long userId) {
        PsychologistProfile profile = getProfileByUserId(userId);
        Long psyId = profile.getId();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        long todayAppts = appointmentRepo
                .countByPsychologistIdAndDateTimeBetweenAndStatusNotAndDeletedFalse(
                        profile.getUser().getId(), todayStart, todayEnd,
                        AppointmentStatus.CANCELLED);

        long pendingReqs = appointmentRepo
                .countByPsychologistIdAndStatusAndDeletedFalse(
                        profile.getUser().getId(), AppointmentStatus.PENDING);

        List<Long> patientIds = recordRepo.findDistinctStudentIds(psyId);

        List<ConfidentialRecord> criticalRecords = recordRepo
                .findByPsychologistAndRiskLevels(psyId,
                        List.of(AlertSeverity.HIGH, AlertSeverity.CRITICAL));

        List<ConfidentialRecord> followUps = recordRepo.findPendingFollowUps(psyId);

        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);

        long weekSessions = recordRepo
                .countByPsychologistIdAndSessionDateBetweenAndDeletedFalse(
                        psyId, weekStart, LocalDate.now());
        long monthSessions = recordRepo
                .countByPsychologistIdAndSessionDateBetweenAndDeletedFalse(
                        psyId, monthStart, LocalDate.now());

        return PsychologistDashboardResponse.builder()
                .psychologistName(profile.getFullName())
                .specialization(profile.getSpecialization())
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
        PsychologistProfile profile = getProfileByUserId(userId);
        return timeSlotRepo
                .findByPsychologistIdAndDeletedFalseOrderByDayOfWeekAscStartTimeAsc(
                        profile.getId())
                .stream()
                .map(this::toTimeSlotResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TimeSlotResponse> updateSchedule(Long userId, ScheduleUpdateRequest request) {
        PsychologistProfile profile = getProfileByUserId(userId);

        for (int i = 0; i < request.getSlots().size(); i++) {
            TimeSlotRequest slot = request.getSlots().get(i);
            for (int j = i + 1; j < request.getSlots().size(); j++) {
                TimeSlotRequest other = request.getSlots().get(j);
                if (slot.getDayOfWeek().equals(other.getDayOfWeek())) {
                    boolean overlaps = slot.getStartTime().isBefore(other.getEndTime())
                            && other.getStartTime().isBefore(slot.getEndTime());
                    if (overlaps) {
                        throw new BusinessException(String.format(
                                "Chevauchement détecté le %s entre %s-%s et %s-%s",
                                slot.getDayOfWeek(), slot.getStartTime(), slot.getEndTime(),
                                other.getStartTime(), other.getEndTime()));
                    }
                }
            }
        }

        List<TimeSlot> existingSlots = timeSlotRepo
                .findByPsychologistIdAndDeletedFalseOrderByDayOfWeekAscStartTimeAsc(
                        profile.getId());
        existingSlots.forEach(s -> s.setDeleted(true));
        timeSlotRepo.saveAll(existingSlots);

        List<TimeSlot> newSlots = request.getSlots().stream()
                .map(req -> TimeSlot.builder()
                        .psychologist(profile)
                        .dayOfWeek(req.getDayOfWeek())
                        .startTime(req.getStartTime())
                        .endTime(req.getEndTime())
                        .available(req.getAvailable() != null ? req.getAvailable() : true)
                        .build())
                .collect(Collectors.toList());

        List<TimeSlot> saved = timeSlotRepo.saveAll(newSlots);
        log.info("Schedule updated for psychologist {} : {} slots",
                profile.getLicenseNumber(), saved.size());

        return saved.stream().map(this::toTimeSlotResponse).collect(Collectors.toList());
    }

    public List<TimeSlotResponse> getAvailableSlots(Long psychologistUserId,
                                                    DayOfWeek dayOfWeek) {
        PsychologistProfile profile = psychologistRepo.findByUserId(psychologistUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PsychologistProfile", "userId", psychologistUserId));

        return timeSlotRepo
                .findByPsychologistIdAndDayOfWeekAndAvailableTrueAndDeletedFalse(
                        profile.getId(), dayOfWeek)
                .stream()
                .map(this::toTimeSlotResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════
    //  APPOINTMENTS
    // ═══════════════════════════════════════════════════════

    public Page<AppointmentResponse> getAppointments(Long userId, Pageable pageable) {
        return appointmentRepo
                .findByPsychologistIdAndDeletedFalseOrderByDateTimeDesc(userId, pageable)
                .map(this::toAppointmentResponse);
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
    public AppointmentResponse cancelAppointment(Long userId, Long appointmentId,
                                                 String reason) {
        Appointment appointment = findAppointmentForPsychologist(userId, appointmentId);
        appointment.cancel(userId, reason);
        return toAppointmentResponse(appointmentRepo.save(appointment));
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long userId, Long appointmentId,
                                                   String notes) {
        Appointment appointment = findAppointmentForPsychologist(userId, appointmentId);
        appointment.complete(notes);
        return toAppointmentResponse(appointmentRepo.save(appointment));
    }

    // ═══════════════════════════════════════════════════════
    //  CONFIDENTIAL RECORDS
    // ═══════════════════════════════════════════════════════

    public Page<RecordResponse> getStudentRecords(Long userId, Long studentId,
                                                  Pageable pageable) {
        PsychologistProfile profile = getProfileByUserId(userId);
        return recordRepo
                .findByStudentIdAndPsychologistIdAndDeletedFalseOrderBySessionDateDesc(
                        studentId, profile.getId(), pageable)
                .map(this::toRecordResponse);
    }

    public Page<RecordResponse> getAllMyRecords(Long userId, Pageable pageable) {
        PsychologistProfile profile = getProfileByUserId(userId);
        return recordRepo
                .findByPsychologistIdAndDeletedFalseOrderBySessionDateDesc(
                        profile.getId(), pageable)
                .map(this::toRecordResponse);
    }

    @Transactional
    public RecordResponse createRecord(Long userId, CreateRecordRequest request) {
        PsychologistProfile profile = getProfileByUserId(userId);

        StudentProfile student = studentProfileRepo.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "StudentProfile", "id", request.getStudentId()));

        ConfidentialRecord record = ConfidentialRecord.builder()
                .student(student)
                .psychologist(profile)
                .sessionDate(request.getSessionDate())
                .observations(request.getObservations())
                .riskLevel(request.getRiskLevel() != null
                        ? request.getRiskLevel() : AlertSeverity.LOW)
                .recommendations(request.getRecommendations())
                .followUpRequired(request.getFollowUpRequired() != null
                        ? request.getFollowUpRequired() : false)
                .nextSessionDate(request.getNextSessionDate())
                .sessionDurationMinutes(request.getSessionDurationMinutes() != null
                        ? request.getSessionDurationMinutes() : 30)
                .interventions(request.getInterventions())
                .studentProgress(request.getStudentProgress())
                .build();

        ConfidentialRecord saved = recordRepo.save(record);
        log.info("Confidential record {} created for student {} by psychologist {}",
                saved.getId(), student.getStudentId(), profile.getLicenseNumber());

        return toRecordResponse(saved);
    }

    @Transactional
    public RecordResponse updateRecord(Long userId, Long recordId,
                                       UpdateRecordRequest request) {
        PsychologistProfile profile = getProfileByUserId(userId);

        ConfidentialRecord record = recordRepo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ConfidentialRecord", "id", recordId));

        if (!record.getPsychologist().getId().equals(profile.getId())) {
            throw new BusinessException(
                    "Vous ne pouvez modifier que vos propres fiches de suivi");
        }

        if (request.getObservations() != null)
            record.setObservations(request.getObservations());
        if (request.getRiskLevel() != null)
            record.setRiskLevel(request.getRiskLevel());
        if (request.getRecommendations() != null)
            record.setRecommendations(request.getRecommendations());
        if (request.getFollowUpRequired() != null)
            record.setFollowUpRequired(request.getFollowUpRequired());
        if (request.getNextSessionDate() != null)
            record.setNextSessionDate(request.getNextSessionDate());
        if (request.getSessionDurationMinutes() != null)
            record.setSessionDurationMinutes(request.getSessionDurationMinutes());
        if (request.getInterventions() != null)
            record.setInterventions(request.getInterventions());
        if (request.getStudentProgress() != null)
            record.setStudentProgress(request.getStudentProgress());

        return toRecordResponse(recordRepo.save(record));
    }

    // ═══════════════════════════════════════════════════════
    //  AT-RISK STUDENTS / ALERTS
    // ═══════════════════════════════════════════════════════

    public List<StudentAlertResponse> getStudentsAtRisk(Long userId) {
        PsychologistProfile profile = getProfileByUserId(userId);
        List<Long> studentIds = recordRepo.findDistinctStudentIds(profile.getId());

        List<StudentAlertResponse> alerts = new ArrayList<>();
        LocalDate twoWeeksAgo = LocalDate.now().minusWeeks(2);

        for (Long studentId : studentIds) {
            StudentProfile student = studentProfileRepo.findById(studentId).orElse(null);
            if (student == null) continue;

            List<ConfidentialRecord> latestRecords = recordRepo.findLatestByStudent(
                    studentId, PageRequest.of(0, 1));
            ConfidentialRecord latest = latestRecords.isEmpty() ? null : latestRecords.get(0);

            Double moodAvg = moodEntryRepo.getAverageMood(
                    studentId, twoWeeksAgo, LocalDate.now());
            Long unjustifiedAbs = absenceRepo.countUnjustified(studentId);
            double academicAvg = student.getAverageGrade(null);

            List<String> reasons = new ArrayList<>();
            AlertSeverity currentRisk = latest != null ? latest.getRiskLevel() : AlertSeverity.LOW;

            if (currentRisk == AlertSeverity.HIGH || currentRisk == AlertSeverity.CRITICAL) {
                reasons.add("Niveau de risque clinique : " + currentRisk);
            }
            if (moodAvg != null && moodAvg < 2.5) {
                reasons.add("Humeur moyenne basse : " + String.format("%.1f", moodAvg) + "/5");
            }
            if (unjustifiedAbs != null && unjustifiedAbs > 3) {
                reasons.add(unjustifiedAbs + " absences non justifiées");
            }
            if (academicAvg > 0 && academicAvg < 40) {
                reasons.add("Moyenne académique critique : " + String.format("%.1f", academicAvg) + "%");
            }
            if (latest != null && Boolean.TRUE.equals(latest.getFollowUpRequired())) {
                reasons.add("Suivi en attente depuis le " + latest.getSessionDate());
            }

            if (!reasons.isEmpty()) {
                alerts.add(StudentAlertResponse.builder()
                        .studentId(studentId)
                        .studentName(student.getUser().getFullName())
                        .department(student.getDepartment())
                        .level(student.getLevel())
                        .currentRiskLevel(currentRisk)
                        .lastSessionDate(latest != null ? latest.getSessionDate() : null)
                        .recentMoodAverage(moodAvg != null
                                ? Math.round(moodAvg * 100.0) / 100.0 : null)
                        .unjustifiedAbsences(unjustifiedAbs)
                        .academicAverage(Math.round(academicAvg * 100.0) / 100.0)
                        .followUpRequired(latest != null ? latest.getFollowUpRequired() : false)
                        .alertReasons(reasons)
                        .build());
            }
        }

        alerts.sort((a, b) -> b.getCurrentRiskLevel().ordinal() - a.getCurrentRiskLevel().ordinal());
        return alerts;
    }

    public List<RecordResponse> getPendingFollowUps(Long userId) {
        PsychologistProfile profile = getProfileByUserId(userId);
        return recordRepo.findPendingFollowUps(profile.getId()).stream()
                .map(this::toRecordResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════

    private Appointment findAppointmentForPsychologist(Long userId, Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment", "id", appointmentId));
        if (!appointment.getPsychologist().getId().equals(userId)) {
            throw new BusinessException("Ce rendez-vous n'est pas assigné à votre compte");
        }
        return appointment;
    }

    // ═══════════════════════════════════════════════════════
    //  MAPPERS (FIXED)
    // ═══════════════════════════════════════════════════════

    private TimeSlotResponse toTimeSlotResponse(TimeSlot ts) {
        return TimeSlotResponse.builder()
                .id(ts.getId())
                .dayOfWeek(ts.getDayOfWeek())
                .startTime(ts.getStartTime())
                .endTime(ts.getEndTime())
                .available(ts.getAvailable())
                .durationMinutes(ts.getDurationMinutes())
                .build();
    }

    /**
     * FIXED: Le champ psychologist dans Appointment est de type User (pas PsychologistProfile).
     * Donc on accède directement à a.getPsychologist().getId() et a.getPsychologist().getFullName()
     * sans passer par un profile intermédiaire.
     *
     * Avant (INCORRECT si psychologist est un PsychologistProfile) :
     *   .psychologistId(a.getPsychologist().getId())         // retourne l'ID du profile
     *   .psychologistName(a.getPsychologist().getFullName())  // getFullName() n'existe pas sur PsychologistProfile directement
     *
     * Correction : Le champ Appointment.psychologist est un User,
     * donc getId() et getFullName() fonctionnent directement.
     * Si ton entité Appointment a un champ PsychologistProfile au lieu de User,
     * il faut passer par a.getPsychologist().getUser().getId() etc.
     */
    private AppointmentResponse toAppointmentResponse(Appointment a) {
        // Cas 1 : Appointment.psychologist est de type User
        // (c'est le cas dans notre entité Appointment actuelle)
        return AppointmentResponse.builder()
                .id(a.getId())
                .studentId(a.getStudent().getId())
                .studentName(a.getStudent().getUser().getFullName())
                .studentDepartment(a.getStudent().getDepartment())
                .psychologistId(a.getPsychologist().getId())
                .psychologistName(a.getPsychologist().getFullName())
                .dateTime(a.getDateTime())
                .duration(a.getDuration())
                .status(a.getStatus())
                .type(a.getType())
                .reason(a.getReason())
                .notes(a.getNotes())
                .cancellationReason(a.getCancellationReason())
                .createdAt(a.getCreatedAt())
                .build();
    }

    /*
     * Si tu changes Appointment.psychologist vers PsychologistProfile,
     * utilise ce mapper à la place :
     *
     * private AppointmentResponse toAppointmentResponse(Appointment a) {
     *     PsychologistProfile psyProfile = a.getPsychologist();
     *     return AppointmentResponse.builder()
     *             .id(a.getId())
     *             .studentId(a.getStudent().getId())
     *             .studentName(a.getStudent().getUser().getFullName())
     *             .studentDepartment(a.getStudent().getDepartment())
     *             .psychologistId(psyProfile.getUser().getId())
     *             .psychologistName(psyProfile.getUser().getFullName())
     *             .dateTime(a.getDateTime())
     *             .duration(a.getDuration())
     *             .status(a.getStatus())
     *             .type(a.getType())
     *             .reason(a.getReason())
     *             .notes(a.getNotes())
     *             .cancellationReason(a.getCancellationReason())
     *             .createdAt(a.getCreatedAt())
     *             .build();
     * }
     */

    private RecordResponse toRecordResponse(ConfidentialRecord r) {
        return RecordResponse.builder()
                .id(r.getId())
                .studentId(r.getStudent().getId())
                .studentName(r.getStudent().getUser().getFullName())
                .studentDepartment(r.getStudent().getDepartment())
                .studentLevel(r.getStudent().getLevel())
                .sessionDate(r.getSessionDate())
                .observations(r.getObservations())
                .riskLevel(r.getRiskLevel())
                .recommendations(r.getRecommendations())
                .followUpRequired(r.getFollowUpRequired())
                .nextSessionDate(r.getNextSessionDate())
                .sessionDurationMinutes(r.getSessionDurationMinutes())
                .interventions(r.getInterventions())
                .studentProgress(r.getStudentProgress())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        PsychologistProfile profile = getProfileByUserId(userId);

        ConfidentialRecord record = recordRepo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ConfidentialRecord", "id", recordId));

        if (!record.getPsychologist().getId().equals(profile.getId())) {
            throw new BusinessException(
                    "Vous ne pouvez supprimer que vos propres fiches de suivi");
        }

        record.setDeleted(true);
        recordRepo.save(record);

        log.info("Confidential record {} soft-deleted by psychologist {}",
                recordId, profile.getLicenseNumber());
    }
}

