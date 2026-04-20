package tn.enicarthage.speedenicar_projet.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.student.entity.JournalEntry;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.JournalEntryRepository;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final StudentProfileRepository studentProfileRepository;

    // ── Créer une entrée ─────────────────────────────────────

    public JournalEntry createEntry(Long userId, JournalEntry journalEntry) {
        StudentProfile student = getStudentByUserId(userId);
        journalEntry.setStudent(student);
        journalEntry.setIsPrivate(true); // toujours privé par défaut
        return journalEntryRepository.save(journalEntry);
    }

    // ── Liste paginée ────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<JournalEntry> getEntries(Long userId, Pageable pageable) {
        StudentProfile student = getStudentByUserId(userId);
        return journalEntryRepository
                .findByStudentIdAndDeletedFalseOrderByCreatedAtDesc(student.getId(), pageable);
    }

    // ── Modifier une entrée ──────────────────────────────────

    public JournalEntry updateEntry(Long userId, Long entryId, JournalEntry updated) {
        StudentProfile student = getStudentByUserId(userId);

        JournalEntry existing = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entrée journal introuvable"));

        // Vérifier que l'entrée appartient bien à cet étudiant
        if (!existing.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Accès refusé à cette entrée");
        }

        existing.setTitle(updated.getTitle());
        existing.setContent(updated.getContent());
        existing.setMood(updated.getMood());

        return journalEntryRepository.save(existing);
    }

    // ── Supprimer une entrée (soft delete) ───────────────────

    public void deleteEntry(Long userId, Long entryId) {
        StudentProfile student = getStudentByUserId(userId);

        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entrée journal introuvable"));

        if (!entry.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Accès refusé à cette entrée");
        }

        entry.setDeleted(true);
        journalEntryRepository.save(entry);
    }

    // ── Helper privé ─────────────────────────────────────────

    private StudentProfile getStudentByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));
    }
}