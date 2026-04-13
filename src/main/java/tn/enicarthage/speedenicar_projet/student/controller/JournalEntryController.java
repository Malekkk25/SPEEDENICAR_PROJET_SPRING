package tn.enicarthage.speedenicar_projet.student.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.student.dto.request.JournalEntryRequest;
import tn.enicarthage.speedenicar_projet.student.dto.response.JournalEntryResponse;
import tn.enicarthage.speedenicar_projet.student.entity.JournalEntry;
import tn.enicarthage.speedenicar_projet.student.service.JournalEntryService;

@RestController
@RequestMapping("/api/v1/student/journal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    // POST /api/v1/student/journal
    @PostMapping
    public ResponseEntity<ApiResponse<JournalEntryResponse>> createEntry(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody JournalEntryRequest request) {

        Long userId = Long.parseLong(user.getUsername());

        JournalEntry entry = JournalEntry.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .mood(request.getMood())
                .build();

        JournalEntry saved = journalEntryService.createEntry(userId, entry);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toResponse(saved), "Entrée créée avec succès"));
    }

    // GET /api/v1/student/journal
    @GetMapping
    public ResponseEntity<ApiResponse<Page<JournalEntryResponse>>> getEntries(
            @AuthenticationPrincipal UserDetails user,
            @PageableDefault(size = 10) Pageable pageable) {

        Long userId = Long.parseLong(user.getUsername());
        Page<JournalEntryResponse> page = journalEntryService
                .getEntries(userId, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    // PUT /api/v1/student/journal/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> updateEntry(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @Valid @RequestBody JournalEntryRequest request) {

        Long userId = Long.parseLong(user.getUsername());

        JournalEntry updated = JournalEntry.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .mood(request.getMood())
                .build();

        JournalEntry saved = journalEntryService.updateEntry(userId, id, updated);

        return ResponseEntity.ok(
                ApiResponse.ok(toResponse(saved), "Entrée modifiée avec succès"));
    }

    // DELETE /api/v1/student/journal/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEntry(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {

        Long userId = Long.parseLong(user.getUsername());
        journalEntryService.deleteEntry(userId, id);

        return ResponseEntity.ok(
                ApiResponse.ok(null, "Entrée supprimée avec succès"));
    }

    // ── Mapper entité → response ─────────────────────────────

    private JournalEntryResponse toResponse(JournalEntry entry) {
        return JournalEntryResponse.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .mood(entry.getMood())
                .isPrivate(entry.getIsPrivate())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}