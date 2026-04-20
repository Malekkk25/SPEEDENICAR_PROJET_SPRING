package tn.enicarthage.speedenicar_projet.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.common.enums.ReportStatus;
import tn.enicarthage.speedenicar_projet.common.enums.UrgencyLevel;
import tn.enicarthage.speedenicar_projet.student.entity.DifficultyReport;

import java.util.List;

@Repository
public interface DifficultyReportRepository extends JpaRepository<DifficultyReport, Long> {

    Page<DifficultyReport> findByStudentIdAndDeletedFalseOrderByCreatedAtDesc(
            Long studentId, Pageable pageable);

    List<DifficultyReport> findByStudentIdAndStatusAndDeletedFalse(
            Long studentId, ReportStatus status);

    List<DifficultyReport> findByStudentIdAndUrgencyAndDeletedFalse(
            Long studentId, UrgencyLevel urgency);

    Long countByStudentIdAndStatusAndDeletedFalse(
            Long studentId, ReportStatus status);
}