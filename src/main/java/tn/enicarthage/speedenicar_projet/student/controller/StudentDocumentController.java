package tn.enicarthage.speedenicar_projet.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.scolarity.dto.response.MedicalDocumentResponse;
import tn.enicarthage.speedenicar_projet.student.service.MedicalDocumentService;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/student/documents")
@RequiredArgsConstructor
public class StudentDocumentController {

    private final MedicalDocumentService documentService; // 👈 Le nom doit être exactement celui-ci
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<MedicalDocumentResponse>> uploadDocument(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Cet appel doit correspondre à la signature dans ton service
        MedicalDocumentResponse response = documentService.uploadForStudent(user.getId(), file, type);

        return ResponseEntity.ok(ApiResponse.ok(response, "Document déposé"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicalDocumentResponse>>> getMyDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {

        // ✅ Cet appel doit correspondre à ta méthode dans MedicalDocumentService
        List<MedicalDocumentResponse> docs = documentService.getStudentDocuments(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.ok(docs));
    }
}