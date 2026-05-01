package tn.enicarthage.speedenicar_projet.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import tn.enicarthage.speedenicar_projet.scolarity.dto.response.MedicalDocumentResponse;
import tn.enicarthage.speedenicar_projet.student.service.MedicalDocumentService;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class documentController {

    private final MedicalDocumentService documentService;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────
    // 1. ÉTUDIANT : Uploader un document
    // ─────────────────────────────────────────────
    @PostMapping("/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<MedicalDocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            Authentication auth) {

        System.out.println("🚀 UPLOAD HIT ! File: " + file.getOriginalFilename() + ", Type: " + type);

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        MedicalDocumentResponse response = documentService.uploadForStudent(user.getId(), file, type);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────
    // 2. ÉTUDIANT : Voir ses propres documents
    // ─────────────────────────────────────────────
    @GetMapping("/my-documents")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<MedicalDocumentResponse>> getMyDocuments(Authentication auth) {

        // Ton service getStudentDocuments attend directement l'email, c'est parfait !
        List<MedicalDocumentResponse> docs = documentService.getStudentDocuments(auth.getName());

        return ResponseEntity.ok(docs);
    }

    // ─────────────────────────────────────────────
    // 3. PSYCHOLOGUE : Voir les documents d'un étudiant
    // ─────────────────────────────────────────────
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('PSYCHOLOGIST')")
    public ResponseEntity<List<MedicalDocumentResponse>> getDocumentsByStudent(@PathVariable Long studentId) {

        List<MedicalDocumentResponse> docs = documentService.getDocumentsByStudent(studentId);

        return ResponseEntity.ok(docs);
    }

    // ─────────────────────────────────────────────
    // 4. COMMUN : Télécharger un document (Évite l'erreur 400)
    // ─────────────────────────────────────────────
    @GetMapping("/download/{id}")
    @PreAuthorize("hasRole('PSYCHOLOGIST') or hasRole('STUDENT')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {

        // Ton service retourne une org.springframework.core.io.Resource
        Resource resource = documentService.downloadDocument(id);

        // On force le format binaire "octet-stream" pour que le navigateur le télécharge
        // Le nom du fichier sera celui généré avec l'UUID (ex: uuid_nom.pdf)
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}