package tn.enicarthage.speedenicar_projet.module_psychologue.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.common.enums.DocStatus;

import java.util.List;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {

    // ═══════════════════════════════════════════════════════════════
    // 🔹 POUR ÉTUDIANT (getMyDocuments, upload)
    // ═══════════════════════════════════════════════════════════════

    // Tous les documents d'un étudiant (triés récent → ancien)
    List<MedicalDocument> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    // Documents d'un étudiant par status
    List<MedicalDocument> findByStudentIdAndStatusOrderByCreatedAtDesc(Long studentId, DocStatus status);

    // Compteur documents étudiant
    long countByStudentId(Long studentId);

    // Vérif doublon fichier
    boolean existsByStudentIdAndFileName(Long studentId, String fileName);

    // ═══════════════════════════════════════════════════════════════
    // 🔹 POUR PSYCHOLOGUE (dashboard, liste)
    // ═══════════════════════════════════════════════════════════════

    // Documents par status (PENDING pour travail psy)
    List<MedicalDocument> findByStatusOrderByCreatedAtAsc(DocStatus status);
    Page<MedicalDocument> findByStatusOrderByCreatedAtAsc(DocStatus status, Pageable pageable);

    // Compteurs par status (dashboard)
    long countByStatus(DocStatus status);

    // ═══════════════════════════════════════════════════════════════
    // 🔹 SÉCURITÉ & DOWNLOAD
    // ═══════════════════════════════════════════════════════════════

    // Vérif document existe + status OK pour download
    boolean existsByIdAndStatus(Long id, DocStatus status);

    // ═══════════════════════════════════════════════════════════════
    // 🔹 RECHERCHE AVANCÉE (avec JOIN FETCH anti-N+1)
    // ═══════════════════════════════════════════════════════════════

    // Documents étudiant + infos complètes (évite N+1 queries)
    @Query("SELECT d FROM MedicalDocument d " +
            "JOIN FETCH d.student s " +
            "JOIN FETCH s.user u " +
            "WHERE s.id = :studentId " +
            "ORDER BY d.createdAt DESC")
    List<MedicalDocument> findByStudentIdWithStudent(@Param("studentId") Long studentId);

    // Liste PENDING psy + infos étudiant
    @Query("SELECT d FROM MedicalDocument d " +
            "JOIN FETCH d.student s " +
            "JOIN FETCH s.user u " +
            "WHERE d.status = 'PENDING' " +
            "ORDER BY d.createdAt ASC")
    List<MedicalDocument> findPendingWithStudentInfo();

    // Recherche par nom fichier
    List<MedicalDocument> findByFileNameContainingIgnoreCaseOrderByCreatedAtDesc(String fileName);
}