package tn.enicarthage.speedenicar_projet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentStatus;
import tn.enicarthage.speedenicar_projet.common.exception.BusinessException;
import tn.enicarthage.speedenicar_projet.module_psychologue.PsychologistService;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.AppointmentRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.TimeSlotRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.document.MedicalDocumentRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.dto.AppointmentResponse;
import tn.enicarthage.speedenicar_projet.module_psychologue.repository.ConfidentialRecordRepository;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.AbsenceRepository;
import tn.enicarthage.speedenicar_projet.student.repository.MoodEntryRepository;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest // 👈 Démarrage du contexte global Spring Boot
class PsychologistServiceIntegrationTest {

    @Autowired // 👈 Injection du VRAI service géré par Spring (avec @Transactional actif)
    private PsychologistService psychologistService;

    // ═══════════════════════════════════════════════════════
    // MOCKS DU CONTEXTE SPRING (Base de données simulée)
    // ═══════════════════════════════════════════════════════
    @MockBean private UserRepository userRepo;
    @MockBean private AppointmentRepository appointmentRepo;
    @MockBean private TimeSlotRepository timeSlotRepo;
    @MockBean private ConfidentialRecordRepository recordRepo;
    @MockBean private MedicalDocumentRepository documentRepo;
    @MockBean private StudentProfileRepository studentProfileRepo;
    @MockBean private MoodEntryRepository moodEntryRepo;
    @MockBean private AbsenceRepository absenceRepo;

    // ═══════════════════════════════════════════════════════
    // TEST 1 : VÉRIFICATION DU CHARGEMENT SPRING
    // ═══════════════════════════════════════════════════════
    @Test
    void contextLoads() {
        // Si le service contient des erreurs d'injection (ex: un bean manquant),
        // ce test plantera avant même d'arriver ici.
        assertNotNull(psychologistService, "Le PsychologistService doit être chargé par Spring");
    }

    // ═══════════════════════════════════════════════════════
    // TEST 2 : CONFIRMATION D'UN RENDEZ-VOUS (Validation @Transactional)
    // ═══════════════════════════════════════════════════════
    @Test
    void confirmAppointment_ShouldExecuteWithinTransaction_AndReturnResponse() {
        // 1. Arrange
        Long psyId = 1L;
        Long appointmentId = 100L;

        // Préparation du Psy
        User mockPsy = new User();
        mockPsy.setId(psyId);
        mockPsy.setFirstName("Dr. Freud");
        mockPsy.setLastName("Sigmund");

        // Préparation de l'Étudiant (nécessaire pour le mapping toAppointmentResponse)
        User mockStudentUser = new User();
        mockStudentUser.setFirstName("Patient");
        mockStudentUser.setLastName("Zero");

        StudentProfile mockStudent = new StudentProfile();
        mockStudent.setId(10L);
        mockStudent.setUser(mockStudentUser);
        mockStudent.setDepartment("Génie Électrique");

        // Préparation du Rendez-vous
        Appointment mockAppointment = spy(new Appointment()); // On utilise un spy pour surveiller l'objet réel
        mockAppointment.setId(appointmentId);
        mockAppointment.setPsychologist(mockPsy);
        mockAppointment.setStudent(mockStudent);
        mockAppointment.setStatus(AppointmentStatus.PENDING);
        mockAppointment.setDateTime(LocalDateTime.now().plusDays(1));

        // Simulation des appels BDD
        when(appointmentRepo.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Act
        AppointmentResponse response = psychologistService.confirmAppointment(psyId, appointmentId);

        // 3. Assert
        assertNotNull(response);
        assertEquals(AppointmentStatus.CONFIRMED, response.getStatus()); // Vérifie que la méthode métier a marché
        assertEquals("Dr. Freud Sigmund", response.getPsychologistName());

        // Vérifie que save() a bien été appelé dans la transaction
        verify(appointmentRepo, times(1)).save(mockAppointment);
    }

    // ═══════════════════════════════════════════════════════
    // TEST 3 : GESTION DES EXCEPTIONS MÉTIER
    // ═══════════════════════════════════════════════════════
    @Test
    void cancelAppointment_ShouldThrowBusinessException_WhenPsyDoesNotOwnIt() {
        // 1. Arrange
        Long currentPsyId = 1L; // Le psy connecté
        Long wrongPsyId = 2L;   // Le psy à qui appartient vraiment le RDV
        Long appointmentId = 50L;

        User wrongPsy = new User();
        wrongPsy.setId(wrongPsyId);

        Appointment mockAppointment = new Appointment();
        mockAppointment.setId(appointmentId);
        mockAppointment.setPsychologist(wrongPsy); // 👈 C'est ici que l'erreur va se déclencher

        when(appointmentRepo.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));

        // 2 & 3. Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            psychologistService.cancelAppointment(currentPsyId, appointmentId, "Indisponible");
        });

        assertEquals("Ce rendez-vous ne vous appartient pas", exception.getMessage());

        // On vérifie que la transaction s'est bien arrêtée (pas de sauvegarde BDD)
        verify(appointmentRepo, never()).save(any());
    }
}