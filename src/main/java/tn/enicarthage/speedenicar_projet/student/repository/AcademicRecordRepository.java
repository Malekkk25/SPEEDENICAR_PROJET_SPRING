package tn.enicarthage.speedenicar_projet.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.student.entity.AcademicRecord;

import java.util.List;

@Repository
public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {

    List<AcademicRecord> findByStudentIdAndDeletedFalseOrderBySemesterAsc(Long studentId);

    List<AcademicRecord> findByStudentIdAndSemesterAndDeletedFalse(
            Long studentId, String semester);
}