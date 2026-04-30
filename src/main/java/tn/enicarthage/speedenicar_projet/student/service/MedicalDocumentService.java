package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.enicarthage.speedenicar_projet.common.enums.DocStatus;
import tn.enicarthage.speedenicar_projet.module_psychologue.document.MedicalDocument;
import tn.enicarthage.speedenicar_projet.module_psychologue.document.MedicalDocumentRepository;
import tn.enicarthage.speedenicar_projet.scolarity.dto.response.MedicalDocumentResponse;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository; // 👈 IMPORT AJOUTÉ

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalDocumentService {

    // 👈 Les 3 repositories sont maintenant bien déclarés avec les bons noms
    private final MedicalDocumentRepository medicalDocumentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public MedicalDocumentResponse uploadForStudent(Long userId, MultipartFile file, String type) {
        // 1. Trouver le profil étudiant associé à cet utilisateur
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        // 2. Créer le document à partir du fichier reçu
        MedicalDocument doc = new MedicalDocument();
        doc.setFileName(file.getOriginalFilename());
        doc.setFileType(type);
        // Pour l'instant, on met un chemin fictif ou le nom du fichier.
        // Dans un vrai projet, c'est ici qu'on sauvegarde le fichier sur le disque.
        doc.setFilePath("uploads/" + file.getOriginalFilename());
        doc.setStatus(DocStatus.PENDING); // En attente de validation par la scolarité/psy
        doc.setStudent(student);

        // 3. Sauvegarder dans la base de données
        MedicalDocument savedDoc = medicalDocumentRepository.save(doc);

        // 4. LE VRAI RETOUR : On renvoie le document sauvegardé sous forme de DTO
        return mapToResponse(savedDoc);
    }

    public List<MedicalDocumentResponse> getStudentDocuments(String email) {
        // 1. Trouver l'utilisateur grâce à son email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 2. Trouver son profil étudiant
        StudentProfile student = studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        // 3. Récupérer tous ses documents dans la base de données
        List<MedicalDocument> documents = medicalDocumentRepository
                .findByStudentIdOrderByCreatedAtDesc(student.getId());

        // 4. LE VRAI RETOUR : On convertit la liste de MedicalDocument en MedicalDocumentResponse
        return documents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- Helper UNIQUE pour convertir l'entité en DTO ---
    private MedicalDocumentResponse mapToResponse(MedicalDocument doc) {
        return MedicalDocumentResponse.builder()
                .id(doc.getId())
                .fileName(doc.getFileName())
                // ⚠️ NOTE : Si ton DTO MedicalDocumentResponse attend un Enum "DocStatus", laisse "doc.getStatus()".
                // S'il attend un String, ajoute ".name()" comme ceci : "doc.getStatus().name()"
                .status(doc.getStatus())
                .rejectionReason(doc.getRejectionReason())
                .build();
    }
}