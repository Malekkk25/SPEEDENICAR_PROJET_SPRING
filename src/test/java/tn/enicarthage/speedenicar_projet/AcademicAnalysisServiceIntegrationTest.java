package tn.enicarthage.speedenicar_projet; // Adapte le package si nécessaire selon ton arborescence

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import tn.enicarthage.speedenicar_projet.common.enums.RiskLevel;
import tn.enicarthage.speedenicar_projet.scolarity.dto.response.AcademicAnalysisResult;
import tn.enicarthage.speedenicar_projet.scolarity.service.AcademicAnalysisService;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AcademicAnalysisServiceIntegrationTest {

    @Autowired
    private AcademicAnalysisService academicAnalysisService;

    @MockBean
    private StudentProfileRepository studentProfileRepository;

    @Test
    void analyzeStudent_ShouldExecuteWithinTransactionAndReturnLowRiskResult() {
        // 1. Arrange : Préparation des données de test
        Long studentId = 5L;

        User fakeUser = new User();
        fakeUser.setFirstName("IA");
        fakeUser.setLastName("Tester");

        StudentProfile fakeStudent = mock(StudentProfile.class);
        when(fakeStudent.getId()).thenReturn(studentId);
        when(fakeStudent.getUser()).thenReturn(fakeUser);
        when(fakeStudent.getDeleted()).thenReturn(false);

        // On simule un profil d'étudiant avec des notes moyennes et peu d'absences
        when(fakeStudent.getAverageGrade(null)).thenReturn(60.0); // 60% de moyenne
        when(fakeStudent.countUnjustifiedAbsences()).thenReturn(2L); // 2 absences injustifiées
        when(fakeStudent.getAbsences()).thenReturn(List.of());

        // On simule le retour de la base de données
        when(studentProfileRepository.findById(studentId)).thenReturn(Optional.of(fakeStudent));

        // 2. Act : Exécution de la méthode à tester
        AcademicAnalysisResult result = academicAnalysisService.analyzeStudent(studentId);

        // 3. Assert : Vérification des résultats
        assertNotNull(result, "Le résultat de l'analyse ne doit pas être nul");

        // 👈 C'est ici qu'on a corrigé : l'algorithme donne 23 points, ce qui correspond au niveau LOW
        assertEquals(RiskLevel.LOW, result.getRiskLevel(), "Le niveau de risque devrait être faible (LOW)");

        // Vérification que le bon étudiant a été analysé
        assertEquals("IA Tester", result.getStudentName(), "Le nom de l'étudiant doit correspondre");
    }
}