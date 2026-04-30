package tn.enicarthage.speedenicar_projet.module_psychologue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.speedenicar_projet.module_psychologue.document.MedicalDocument;

import java.util.List;

public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {
    List<MedicalDocument> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
