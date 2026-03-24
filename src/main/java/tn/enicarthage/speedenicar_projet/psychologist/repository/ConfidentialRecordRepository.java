package tn.enicarthage.speedenicar_projet.psychologist.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.common.enums.AlertSeverity;
import tn.enicarthage.speedenicar_projet.psychologist.entity.ConfidentialRecord;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConfidentialRecordRepository extends JpaRepository<ConfidentialRecord ,Long > {

    Page<ConfidentialRecord> findByStudentIdAndPsychologistIdAndDeletedFalseOrderBySessionDateDesc(
            Long studentId, Long psychologistId, Pageable pageable);

    Page<ConfidentialRecord> findByPsychologistIdAndDeletedFalseOrderBySessionDateDesc(
            Long psychologistId, Pageable pageable);

    List<ConfidentialRecord> findByStudentIdAndDeletedFalseOrderBySessionDateDesc(
            Long studentId);

    @Query("SELECT cr FROM ConfidentialRecord cr " +
            "JOIN FETCH cr.student sp " +
            "JOIN FETCH sp.user u " +
            "WHERE cr.psychologist.id = :psyId " +
            "AND cr.riskLevel IN :levels " +
            "AND cr.deleted = false " +
            "ORDER BY cr.sessionDate DESC")
    List<ConfidentialRecord> findByPsychologistAndRiskLevels(
            @Param("psyId") Long psychologistId,
            @Param("levels") List<AlertSeverity> riskLevels);


    @Query("SELECT cr FROM ConfidentialRecord cr " +
            "JOIN FETCH cr.student sp " +
            "JOIN FETCH sp.user u " +
            "WHERE cr.psychologist.id = :psyId " +
            "AND cr.followUpRequired = true " +
            "AND cr.deleted = false " +
            "ORDER BY cr.sessionDate ASC")
    List<ConfidentialRecord> findPendingFollowUps(@Param("psyId") Long psychologistId);

    @Query("SELECT cr FROM ConfidentialRecord cr " +
            "WHERE cr.student.id = :studentId " +
            "AND cr.deleted = false " +
            "ORDER BY cr.sessionDate DESC")
    List<ConfidentialRecord> findLatestByStudent(
            @Param("studentId") Long studentId, Pageable pageable);

    long countByPsychologistIdAndSessionDateBetweenAndDeletedFalse(
            Long psychologistId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT cr.student.id FROM ConfidentialRecord cr " +
            "WHERE cr.psychologist.id = :psyId " +
            "AND cr.deleted = false")
    List<Long> findDistinctStudentIds(@Param("psyId") Long psychologistId);
}






