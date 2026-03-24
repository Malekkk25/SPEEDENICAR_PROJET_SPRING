package tn.enicarthage.speedenicar_projet.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.student.entity.MoodEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long> {

    Page<MoodEntry> findByStudentIdAndDeletedFalseOrderByDateDesc(Long studentId, Pageable pageable);

    Optional<MoodEntry> findByStudentIdAndDate(Long studentId, LocalDate date);

    boolean existsByStudentIdAndDate(Long studentId, LocalDate date);

    @Query("SELECT m FROM MoodEntry m " +
            "WHERE m.student.id = :studentId " +
            "AND m.date BETWEEN :startDate AND :endDate " +
            "AND m.deleted = false " +
            "ORDER BY m.date ASC")
    List<MoodEntry> findByStudentIdAndDateBetween(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(m.moodLevel) FROM MoodEntry m " +
            "WHERE m.student.id = :studentId " +
            "AND m.date BETWEEN :startDate AND :endDate " +
            "AND m.deleted = false")
    Double getAverageMood(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(m) FROM MoodEntry m " +
            "WHERE m.student.id = :studentId " +
            "AND m.moodLevel <= 2 " +
            "AND m.date BETWEEN :startDate AND :endDate " +
            "AND m.deleted = false")
    Long countNegativeMoods(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT m.moodLevel, COUNT(m) FROM MoodEntry m " +
            "WHERE m.student.id = :studentId " +
            "AND m.date BETWEEN :startDate AND :endDate " +
            "AND m.deleted = false " +
            "GROUP BY m.moodLevel " +
            "ORDER BY m.moodLevel")
    List<Object[]> getMoodDistribution(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}

