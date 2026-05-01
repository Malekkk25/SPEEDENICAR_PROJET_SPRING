package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentStatus;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentType;
import tn.enicarthage.speedenicar_projet.common.enums.LocationType; // 👈 NOUVEL IMPORT À VÉRIFIER
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.AppointmentRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.TimeSlotRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.AppointmentResponse;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.TimeSlotResponse;
import tn.enicarthage.speedenicar_projet.module_psychologue.entity.TimeSlot;

import tn.enicarthage.speedenicar_projet.student.dto.request.AppointmentRequest;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepo;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public List<TimeSlotResponse> getAvailableSlotsUntilSunday() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();

        List<String> daysToSearch = new ArrayList<>();
        for (int i = today.getValue(); i <= 7; i++) {
            daysToSearch.add(DayOfWeek.of(i).name());
        }

        List<TimeSlot> slots = timeSlotRepo.findByDayOfWeekInAndAvailableTrueAndDeletedFalse(daysToSearch);
        List<TimeSlotResponse> availableResponses = new ArrayList<>();

        for (TimeSlot slot : slots) {
            DayOfWeek slotDayEnum = DayOfWeek.valueOf(String.valueOf(slot.getDayOfWeek()));
            int daysToAdd = slotDayEnum.getValue() - today.getValue();
            LocalDate slotDate = now.toLocalDate().plusDays(daysToAdd);
            LocalDateTime exactDateTime = LocalDateTime.of(slotDate, LocalTime.parse(slot.getStartTime()));

            if (exactDateTime.isAfter(now)) {

                // 👇 DÉBUT DE LA VÉRIFICATION DES RENDEZ-VOUS 👇
                boolean isSlotTaken = false;
                if (slot.getPsychologist() != null) {
                    // On cherche s'il y a un RDV pour ce psy à cette heure précise
                    // "StatusNot(CANCELLED)" signifie qu'on considère le créneau PRIS si le statut est PENDING, CONFIRMED, etc.
                    // S'il n'y a pas de RDV, ou si le RDV est CANCELLED (refusé), ça renvoie false (donc libre).
                    isSlotTaken = appointmentRepository.existsByPsychologistIdAndDateTimeAndStatusNot(
                            slot.getPsychologist().getId(),
                            exactDateTime,
                            AppointmentStatus.CANCELLED
                    );
                }

                // 👈 On n'ajoute le créneau à la liste QUE s'il n'est pas pris
                if (!isSlotTaken) {
                    TimeSlotResponse response = new TimeSlotResponse();
                    response.setId(slot.getId());

                    if (slot.getPsychologist() != null) {
                        response.setPsychologistId(slot.getPsychologist().getId());
                        response.setPsychologistName(slot.getPsychologist().getFirstName() + " " + slot.getPsychologist().getLastName());
                    }

                    response.setDateTime(exactDateTime);
                    response.setDayOfWeek(slotDayEnum);
                    response.setStartTime(LocalTime.parse(slot.getStartTime()));
                    response.setEndTime(LocalTime.parse(slot.getEndTime()));
                    response.setAvailable(slot.isAvailable());

                    availableResponses.add(response);
                }
            }
        }
        return availableResponses;
    }
    @Transactional
    public AppointmentResponse requestAppointment(Long userId, AppointmentRequest request) {
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        final Long finalPsychologistId;

        if (request.getPsychologistId() == null) {
            DayOfWeek dayNeeded = request.getDateTime().getDayOfWeek();

            TimeSlot slot = timeSlotRepo.findAll().stream()
                    .filter(ts -> ts.getDayOfWeek() == dayNeeded)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Aucun psychologue disponible pour ce créneau"));

            finalPsychologistId = slot.getPsychologist().getId();
        } else {
            finalPsychologistId = request.getPsychologistId();
        }

        User psy = userRepository.findById(finalPsychologistId)
                .orElseThrow(() -> new RuntimeException("Psychologue introuvable avec l'ID : " + finalPsychologistId));

        boolean isTaken = appointmentRepository.existsByPsychologistIdAndDateTimeAndStatusNot(
                finalPsychologistId, request.getDateTime(), AppointmentStatus.CANCELLED);

        if (isTaken) {
            throw new RuntimeException("Ce créneau est déjà réservé.");
        }

        // 👇 1. Détermination du type de consultation en gérant les valeurs nulles
        LocationType locType = LocationType.PRESENTIAL; // Valeur par défaut
        if (request.getLocationType() != null && !request.getLocationType().trim().isEmpty()) {
            try {
                locType = LocationType.valueOf(request.getLocationType().toUpperCase());
            } catch (IllegalArgumentException e) {
                locType = LocationType.PRESENTIAL;
            }
        }

        // 4. Création et sauvegarde de l'entité Appointment
        Appointment appointment = Appointment.builder()
                .student(student)
                .psychologist(psy)
                .dateTime(request.getDateTime())
                .duration(30)
                .status(AppointmentStatus.PENDING)
                .type(AppointmentType.valueOf(request.getType().toUpperCase()))
                .locationType(locType) // 👈 2. AJOUT ICI : On sauvegarde en base de données
                .reason(request.getReason())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // 5. Retourner le DTO
        return AppointmentResponse.builder()
                .id(saved.getId())
                .studentId(student.getId())
                .studentName(student.getUser().getFirstName() + " " + student.getUser().getLastName())
                .psychologistId(psy.getId())
                .psychologistName(psy.getFirstName() + " " + psy.getLastName())
                .dateTime(saved.getDateTime())
                .status(saved.getStatus())
                .type(saved.getType())
                // 👈 3. AJOUT ICI : On renvoie l'info au frontend !
                // (J'utilise .name() au cas où ton DTO attend un String. Si ton DTO attend l'Enum LocationType, enlève le .name())
                .locationType(saved.getLocationType() != null ? saved.getLocationType().name() : "PRESENTIAL")
                .reason(saved.getReason())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private TimeSlotResponse toTimeSlotResponse(TimeSlot ts) {
        TimeSlotResponse response = new TimeSlotResponse();
        response.setId(ts.getId());
        response.setDayOfWeek(ts.getDayOfWeek());

        if (ts.getStartTime() != null) {
            response.setStartTime(LocalTime.parse(ts.getStartTime().toString()));
        }
        if (ts.getEndTime() != null) {
            response.setEndTime(LocalTime.parse(ts.getEndTime().toString()));
        }

        response.setAvailable(ts.isAvailable());

        if (ts.getPsychologist() != null) {
            response.setPsychologistId(ts.getPsychologist().getId());
        }

        return response;
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getStudentAppointments(Long userId, Pageable pageable) {
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        Page<Appointment> appointmentPage = appointmentRepository.findByStudentIdOrderByDateTimeDesc(student.getId(), pageable);
        return appointmentPage.map(this::convertToResponse);
    }

    private AppointmentResponse convertToResponse(Appointment app) {
        String studentName = "Étudiant Inconnu";
        if (app.getStudent() != null && app.getStudent().getUser() != null) {
            studentName = app.getStudent().getUser().getFirstName() + " " + app.getStudent().getUser().getLastName();
        }

        String psyName = "Psychologue Inconnu";
        if (app.getPsychologist() != null) {
            psyName = app.getPsychologist().getFirstName() + " " + app.getPsychologist().getLastName();
        }

        return AppointmentResponse.builder()
                .id(app.getId())
                .studentName(studentName)
                .psychologistName(psyName)
                .dateTime(app.getDateTime())
                .status(app.getStatus())
                .reason(app.getReason())
                .type(app.getType())
                // 👈 4. AJOUT ICI : Pour l'affichage de la liste côté Angular
                .locationType(app.getLocationType() != null ? app.getLocationType().name() : "PRESENTIAL")
                .build();
    }
}