package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalDocumentService {

    private final MedicalDocumentRepository medicalDocumentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.base-path:uploads/}")
    private String basePath;

    // ─────────────────────────────────────────────
    // STUDENT
    // ─────────────────────────────────────────────

    @Transactional
    public MedicalDocumentResponse uploadForStudent(Long userId,
                                                    MultipartFile file,
                                                    String type) {
        validateFile(file);

        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        String relativePath = storeFile(file, student.getId());

        MedicalDocument doc = new MedicalDocument();
        doc.setFileName(file.getOriginalFilename());
        doc.setMimeType(type);
        doc.setMimeType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setFilePath(relativePath);
        doc.setStatus(DocStatus.PENDING);
        doc.setStudent(student);

        return mapToDto(medicalDocumentRepository.save(doc));
    }

    @Transactional(readOnly = true)
    public List<MedicalDocumentResponse> getStudentDocuments(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        StudentProfile student = studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        return medicalDocumentRepository.findByStudentIdWithStudent(student.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(Long docId, Long userId) {
        MedicalDocument doc = medicalDocumentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));

        // Seul le student propriétaire peut supprimer
        if (!doc.getStudent().getUser().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé");
        }

        // Supprimer le fichier physique
        deleteFile(doc.getFilePath());

        medicalDocumentRepository.delete(doc);
    }

    // ─────────────────────────────────────────────
    // PSY (voit tout — psy unique dans l'app)
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MedicalDocumentResponse> getAllDocuments() {
        return medicalDocumentRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicalDocumentResponse> getDocumentsByStudent(Long studentId) {
        return medicalDocumentRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicalDocumentResponse> getDocumentsByStatus(DocStatus status) {
        return medicalDocumentRepository.findByStatusOrderByCreatedAtAsc(status)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalDocumentResponse validateDocument(Long docId, Long psyUserId) {
        MedicalDocument doc = getMedicalDocumentOrThrow(docId);

        if (!doc.isPending()) {
            throw new RuntimeException("Seul un document PENDING peut être validé");
        }

        User psy = userRepository.findById(psyUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        doc.validate(psy);
        return mapToDto(medicalDocumentRepository.save(doc));
    }

    @Transactional
    public MedicalDocumentResponse rejectDocument(Long docId,
                                                  Long psyUserId,
                                                  String reason) {
        MedicalDocument doc = getMedicalDocumentOrThrow(docId);

        if (!doc.isPending()) {
            throw new RuntimeException("Seul un document PENDING peut être rejeté");
        }

        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Une raison de rejet est obligatoire");
        }

        User psy = userRepository.findById(psyUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        doc.reject(psy, reason);
        return mapToDto(medicalDocumentRepository.save(doc));
    }

    // ─────────────────────────────────────────────
    // TÉLÉCHARGEMENT (student + psy)
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Resource downloadDocument(Long docId) {
        MedicalDocument doc = getMedicalDocumentOrThrow(docId);
        try {
            Path filePath = Paths.get(basePath).resolve(doc.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Fichier introuvable sur le serveur");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur lors du chargement du fichier");
        }
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Fichier vide ou absent");
        }

        // Max 10 MB
        if (file.getSize() > 10L * 1024 * 1024) {
            throw new RuntimeException("Fichier trop volumineux (max 10 MB)");
        }

        List<String> allowedTypes = List.of(
                "application/pdf",
                "image/jpeg",
                "image/png"
        );

        if (!allowedTypes.contains(file.getContentType())) {
            throw new RuntimeException(
                    "Type de fichier non autorisé. Acceptés : PDF, JPEG, PNG"
            );
        }
    }

    private String storeFile(MultipartFile file, Long studentId) {
        try {
            Path dir = Paths.get(basePath, "student_" + studentId);
            Files.createDirectories(dir);

            String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(uniqueName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // Chemin relatif uniquement
            return "student_" + studentId + "/" + uniqueName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier : " + e.getMessage());
        }
    }

    private void deleteFile(String relativePath) {
        try {
            Path file = Paths.get(basePath).resolve(relativePath);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // On log l'erreur mais on ne bloque pas la suppression en BDD
            System.err.println("Impossible de supprimer le fichier : " + relativePath);
        }
    }

    private MedicalDocument getMedicalDocumentOrThrow(Long docId) {
        return medicalDocumentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));
    }

    private MedicalDocumentResponse mapToDto(MedicalDocument doc) {
        return MedicalDocumentResponse.builder()
                .id(doc.getId())
                .studentId(doc.getStudent() != null ? doc.getStudent().getId() : null)
                .fileName(doc.getFileName())
                .fileType(doc.getMimeType())
                .fileSize(doc.getFileSize())
                .status(doc.getStatus() != null ? doc.getStatus().name() : null)
                .rejectionReason(doc.getRejectionReason())
                .createdAt(doc.getCreatedAt())
                .validationDate(doc.getValidationDate())
                .build();
    }
}