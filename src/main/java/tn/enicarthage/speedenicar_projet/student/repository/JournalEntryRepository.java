package tn.enicarthage.speedenicar_projet.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.student.entity.JournalEntry;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;

import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    Page<JournalEntry> findByStudentIdAndDeletedFalseOrderByCreatedAtDesc(
            Long studentId, Pageable pageable);

}