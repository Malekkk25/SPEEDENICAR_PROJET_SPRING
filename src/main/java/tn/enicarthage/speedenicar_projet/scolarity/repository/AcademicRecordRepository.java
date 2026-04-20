package tn.enicarthage.speedenicar_projet.scolarity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.student.entity.AcademicRecord;

import java.util.List;

@Repository
public interface AcademicRecordRepository
        extends JpaRepository<AcademicRecord, Long> {

    List<AcademicRecord> findByStudentId(Long studentId);

    List<AcademicRecord> findByStudentIdAndSemester(
            Long studentId, String semester);

    @Query("SELECT a FROM AcademicRecord a " +
            "WHERE a.student.id = :studentId " +
            "AND a.deleted = false " +
            "ORDER BY a.semester ASC")
    List<AcademicRecord> findActiveByStudentId(@Param("studentId") Long studentId);
}