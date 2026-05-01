package tn.enicarthage.speedenicar_projet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import tn.enicarthage.speedenicar_projet.student.dto.response.StudentProfileResponse;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;
import tn.enicarthage.speedenicar_projet.student.service.StudentProfileService;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest // 👈 Démarre le contexte Spring Boot
class StudentProfileServiceIntegrationTest {

    @Autowired // 👈 C'est le VRAI service géré par Spring (avec le @Transactional activé)
    private StudentProfileService studentProfileService;

    @MockBean // 👈 Remplace le vrai Repository dans le contexte Spring par un Mock
    private StudentProfileRepository studentProfileRepository;

    @Test
    void contextLoads() {
        // Test très simple pour vérifier que Spring Boot arrive à créer ton Service
        assertNotNull(studentProfileService);
    }

    @Test
    void getProfileByUserId_ShouldReturnProfile_WithSpringContext() {
        // 1. Arrange
        Long userId = 1L;
        User fakeUser = new User();
        fakeUser.setFirstName("Intégration");
        fakeUser.setLastName("Test");

        StudentProfile fakeProfile = new StudentProfile();
        fakeProfile.setId(100L);
        fakeProfile.setDepartment("Génie Mécanique");
        fakeProfile.setUser(fakeUser);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(fakeProfile));

        // 2. Act
        StudentProfileResponse response = studentProfileService.getProfileByUserId(userId);

        // 3. Assert
        assertNotNull(response);
        assertEquals("Intégration", response.getFirstName());
        assertEquals("Génie Mécanique", response.getDepartment());
    }
}