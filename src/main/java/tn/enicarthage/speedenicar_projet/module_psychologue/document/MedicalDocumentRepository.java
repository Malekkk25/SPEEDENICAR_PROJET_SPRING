package tn.enicarthage.speedenicar_projet.module_psychologue.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.common.enums.DocStatus;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {

    Page<MedicalDocument> findByStudentIdAndDeletedFalseOrderByCreatedAtDesc(
            Long studentId, Pageable pageable);

    Page<MedicalDocument> findByStatusAndDeletedFalseOrderByCreatedAtAsc(
            DocStatus status, Pageable pageable);

    long countByStatusAndDeletedFalse(DocStatus status);

    long countByStudentIdAndStatusAndDeletedFalse(Long studentId, DocStatus status);
}

