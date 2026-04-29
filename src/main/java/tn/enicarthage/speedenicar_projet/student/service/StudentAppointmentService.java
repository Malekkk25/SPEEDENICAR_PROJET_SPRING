package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentStatus;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentType;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.AppointmentRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.TimeSlotRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.AppointmentResponse;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.TimeSlotResponse;
import tn.enicarthage.speedenicar_projet.module_psychologue.entity.TimeSlot;

import tn.enicarthage.speedenicar_projet.student.dto.request.AppointmentRequest;
import tn.enicarthage.speedenicar_projet.student.dto.response.AvailableDayResponse;
import tn.enicarthage.speedenicar_projet.student.dto.response.AvailableSlotResponse;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        // 1. Créer la liste des jours en format TEXTE (String) pour la BDD
        List<String> daysToSearch = new ArrayList<>();
        for (int i = today.getValue(); i <= 7; i++) {
            daysToSearch.add(DayOfWeek.of(i).name()); // Transforme l'Enum en String (ex: "MONDAY")
        }

        // 2. L'appel au repository n'aura plus d'erreur, il reçoit bien sa List<String>
        List<TimeSlot> slots = timeSlotRepo.findByDayOfWeekInAndAvailableTrueAndDeletedFalse(daysToSearch);

        List<TimeSlotResponse> availableResponses = new ArrayList<>();

        for (TimeSlot slot : slots) {
            // 3. Reconvertir le String de la BDD en Enum Java pour pouvoir faire des maths avec
            DayOfWeek slotDayEnum = DayOfWeek.valueOf(String.valueOf(slot.getDayOfWeek()));

            // Calcul du décalage en jours
            int daysToAdd = slotDayEnum.getValue() - today.getValue();
            LocalDate slotDate = now.toLocalDate().plusDays(daysToAdd);

            // Date et heure exactes du créneau
            LocalDateTime exactDateTime = LocalDateTime.of(slotDate, LocalTime.parse(slot.getStartTime()));

            // 4. On vérifie que le créneau n'est pas déjà passé
            if (exactDateTime.isAfter(now)) {

                // Création de la réponse avec les setters (comme vu précédemment)
                TimeSlotResponse response = new TimeSlotResponse();
                response.setId(slot.getId());

                if (slot.getPsychologist() != null) {
                    response.setPsychologistId(slot.getPsychologist().getId());
                    response.setPsychologistName(slot.getPsychologist().getFirstName() + " " + slot.getPsychologist().getLastName());
                }

                response.setDateTime(exactDateTime);
                response.setDayOfWeek(slotDayEnum); // Assure-toi que le setter accepte un Enum (ou fais slotDayEnum.name() s'il veut un String)
                response.setStartTime(LocalTime.parse(slot.getStartTime()));
                response.setEndTime(LocalTime.parse(slot.getEndTime()));
                response.setAvailable(slot.isAvailable()); // ou getAvailable() selon ton Entity

                availableResponses.add(response);
            }
        }

        return availableResponses;
    }
    @Transactional
    public AppointmentResponse requestAppointment(Long userId, AppointmentRequest request) {
        // 1. Récupérer le profil de l'étudiant
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        // Utilisation d'une variable finale pour le Stream
        final Long finalPsychologistId;

        if (request.getPsychologistId() == null) {
            // On extrait le jour directement depuis l'objet LocalDateTime
            DayOfWeek dayNeeded = request.getDateTime().getDayOfWeek();

            TimeSlot slot = timeSlotRepo.findAll().stream()
                    .filter(ts -> ts.getDayOfWeek() == dayNeeded) // Comparaison d'Enums directe
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Aucun psychologue disponible pour ce créneau"));

            finalPsychologistId = slot.getPsychologist().getId();
        } else {
            finalPsychologistId = request.getPsychologistId();
        }

        // 2. Récupérer l'entité du psychologue
        User psy = userRepository.findById(finalPsychologistId)
                .orElseThrow(() -> new RuntimeException("Psychologue introuvable avec l'ID : " + finalPsychologistId));

        // 3. Vérifier la disponibilité
        boolean isTaken = appointmentRepository.existsByPsychologistIdAndDateTimeAndStatusNot(
                finalPsychologistId, request.getDateTime(), AppointmentStatus.CANCELLED);

        if (isTaken) {
            throw new RuntimeException("Ce créneau est déjà réservé.");
        }

        // 4. Création et sauvegarde de l'entité Appointment
        Appointment appointment = Appointment.builder()
                .student(student)
                .psychologist(psy)
                .dateTime(request.getDateTime())
                .duration(30)
                .status(AppointmentStatus.PENDING)
                .type(AppointmentType.valueOf(request.getType().toUpperCase()))
                .reason(request.getReason())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // 5. Retourner le DTO pour éviter l'erreur ByteBuddy (Hibernate Proxy)
        return AppointmentResponse.builder()
                .id(saved.getId())
                .studentId(student.getId())
                .studentName(student.getUser().getFirstName() + " " + student.getUser().getLastName())
                .psychologistId(psy.getId())
                .psychologistName(psy.getFirstName() + " " + psy.getLastName())
                .dateTime(saved.getDateTime())
                .status(saved.getStatus())
                .type(saved.getType())
                .reason(saved.getReason())
                .createdAt(LocalDateTime.now())
                .build();
    }




    private TimeSlotResponse toTimeSlotResponse(TimeSlot ts) {
        // 1. On instancie un objet vide
        TimeSlotResponse response = new TimeSlotResponse();

        // 2. On remplit les champs un par un (plus d'erreur d'ordre !)
        response.setId(ts.getId());
        response.setDayOfWeek(ts.getDayOfWeek()); // Ton Enum venant de la base

        // 3. Gestion des heures
        if (ts.getStartTime() != null) {
            // Note : Si ts.getStartTime() renvoie DÉJÀ un LocalTime, enlève le LocalTime.parse()
            response.setStartTime(LocalTime.parse(ts.getStartTime().toString()));
        }
        if (ts.getEndTime() != null) {
            response.setEndTime(LocalTime.parse(ts.getEndTime().toString()));
        }

        // 4. Gestion de la disponibilité
        // Si ta variable Entity est un boolean primitif, c'est isAvailable(). Si c'est un Boolean wrapper, c'est getAvailable().
        response.setAvailable(ts.isAvailable());

        // 5. Gestion de l'ID du psychologue
        if (ts.getPsychologist() != null) {
            // Attention : Assure-toi que ta classe TimeSlotResponse a bien un champ "psychologistId"
            response.setPsychologistId(ts.getPsychologist().getId());
        }

        return response;
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getStudentAppointments(Long userId, Pageable pageable) {
        // 1. Récupérer le profil
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        // 2. Récupérer la page d'entités
        Page<Appointment> appointmentPage = appointmentRepository.findByStudentIdOrderByDateTimeDesc(student.getId(), pageable);

        // 3. Mapper en DTO à l'intérieur de la transaction
        return appointmentPage.map(this::convertToResponse);
    }

    private AppointmentResponse convertToResponse(Appointment app) {
        // ICI : Hibernate a besoin de la session pour faire app.getStudent().getUser()
        // Si StudentProfile est un Lazy Proxy, app.getStudent().getUser() déclenche l'erreur "no session"

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
                .build();
    }
}