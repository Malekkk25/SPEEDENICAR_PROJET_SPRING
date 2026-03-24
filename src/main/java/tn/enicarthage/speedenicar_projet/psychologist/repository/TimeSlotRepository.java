package tn.enicarthage.speedenicar_projet.psychologist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.psychologist.entity.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByPsychologistIdAndDeletedFalseOrderByDayOfWeekAscStartTimeAsc(
            Long psychologistId);

    List<TimeSlot> findByPsychologistIdAndDayOfWeekAndAvailableTrueAndDeletedFalse(
            Long psychologistId, DayOfWeek dayOfWeek);

    @Query("SELECT ts FROM TimeSlot ts " +
            "WHERE ts.psychologist.id = :psyId " +
            "AND ts.dayOfWeek = :day " +
            "AND ts.available = true " +
            "AND ts.deleted = false " +
            "AND ts.startTime >= :fromTime " +
            "ORDER BY ts.startTime ASC")
    List<TimeSlot> findAvailableSlots(
            @Param("psyId") Long psychologistId,
            @Param("day") DayOfWeek dayOfWeek,
            @Param("fromTime") LocalTime fromTime);

    @Query("SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END " +
            "FROM TimeSlot ts " +
            "WHERE ts.psychologist.id = :psyId " +
            "AND ts.dayOfWeek = :day " +
            "AND ts.startTime < :endTime " +
            "AND ts.endTime > :startTime " +
            "AND ts.deleted = false " +
            "AND (:excludeId IS NULL OR ts.id != :excludeId)")
    boolean existsOverlapping(
            @Param("psyId") Long psychologistId,
            @Param("day") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    void deleteByPsychologistId(Long psychologistId);
}

