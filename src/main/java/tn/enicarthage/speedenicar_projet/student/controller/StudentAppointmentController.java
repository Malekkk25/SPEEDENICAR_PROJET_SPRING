package tn.enicarthage.speedenicar_projet.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.AppointmentResponse;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.TimeSlotResponse;
import tn.enicarthage.speedenicar_projet.student.dto.request.AppointmentRequest;
import tn.enicarthage.speedenicar_projet.student.dto.response.AvailableSlotResponse;
import tn.enicarthage.speedenicar_projet.student.service.StudentAppointmentService;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/student/appointments")
@RequiredArgsConstructor
public class StudentAppointmentController {

    private final StudentAppointmentService appointmentService;
    private final UserRepository userRepository; // Ajoute ça

    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getMyAppointments(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Utilisation de ton helper extractUserId pour récupérer l'ID depuis la base
        Long userId = extractUserId(userDetails);

        Page<AppointmentResponse> appointments = appointmentService.getStudentAppointments(userId, pageable);

        return ResponseEntity.ok(appointments);
    }
    /**
     * GET /api/student/appointments/available
     * Pour récupérer la liste des créneaux libres
     */
    @GetMapping("/available")
    public ResponseEntity<List<TimeSlotResponse>> getAvailableSlots() { // <-- On change ici
        return ResponseEntity.ok(appointmentService.getAvailableSlotsUntilSunday());
    }

    /**
     * POST /api/student/appointments/reserve
     * Pour qu'un étudiant réserve un créneau
     */
    @PostMapping("/reserve")
    public ResponseEntity<AppointmentResponse> reserve(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AppointmentRequest request) {

        // 1. Vérification de sécurité
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Extraction sécurisée de l'ID via ton helper
        Long userId = extractUserId(userDetails);

        // 3. Appel au service avec l'ID extrait
        AppointmentResponse response = appointmentService.requestAppointment(userId, request);
        return ResponseEntity.ok(response);
    }

    // Ta méthode helper sécurisée
    private Long extractUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable en base : " + email))
                .getId();
    }

}

