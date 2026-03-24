package tn.enicarthage.speedenicar_projet.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.student.entity.Absence;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    Page<Absence> findByStudentIdAndDeletedFalseOrderByStartDateDesc(Long studentId, Pageable pageable);

    List<Absence> findByStudentIdAndJustifiedFalseAndDeletedFalse(Long studentId);

    @Query("SELECT a FROM Absence a " +
            "WHERE a.student.id = :studentId " +
            "AND a.startDate >= :startDate " +
            "AND a.endDate <= :endDate " +
            "AND a.deleted = false " +
            "ORDER BY a.startDate DESC")
    List<Absence> findByStudentIdAndPeriod(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(
            value = "SELECT * FROM absence WHERE deleted = 0 AND justified = 0 " +
                    "AND (julianday(end_date) - julianday(start_date)) > 3 " +
                    "ORDER BY start_date DESC",
            countQuery = "SELECT count(*) FROM absence WHERE deleted = 0 AND justified = 0 " +
                    "AND (julianday(end_date) - julianday(start_date)) > 3",
            nativeQuery = true
    )
    Page<Absence> findProlongedUnjustified(Pageable pageable);

    @Query("SELECT COUNT(a) FROM Absence a " +
            "WHERE a.student.id = :studentId " +
            "AND a.justified = false " +
            "AND a.deleted = false")
    Long countUnjustified(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(a) FROM Absence a " +
            "WHERE a.student.id = :studentId " +
            "AND a.deleted = false " +
            "AND a.startDate >= :startDate")
    Long getTotalAbsenceDays(@Param("studentId") Long studentId, @Param("startDate") LocalDate startDate);
}

