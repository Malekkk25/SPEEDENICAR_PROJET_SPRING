package tn.enicarthage.speedenicar_projet.module_psychologue.appointment;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.common.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment ,Long> {

Page<Appointment> findByStudentIdAndDeletedFalseOrderByDateTimeDesc(
        Long studentId , Pageable pageable
        );
    Page<Appointment> findByPsychologistIdAndDeletedFalseOrderByDateTimeDesc(
            Long psychologistId ,Pageable pageable);

    List<Appointment> findByPsychologistIdAndStatusAndDeletedFalseOrderByDateTimeAsc(
            Long psychologistId , AppointmentStatus status
            );

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.student sp " +
            "JOIN FETCH sp.user u " +
            "WHERE a.psychologist.id = :psyId " +
            "AND a.dateTime BETWEEN :start AND :end " +
            "AND a.status IN :statuses " +
            "AND a.deleted = false " +
            "ORDER BY a.dateTime ASC")
    List<Appointment> findByPsychologistAndDateRange(
            @Param("psyId") Long psychologistId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.psychologist.id = :psyId " +
            "AND a.dateTime BETWEEN :start AND :end " +
            "AND a.status NOT IN ('CANCELLED') " +
            "AND a.deleted = false")
    List<Appointment> findOccupiedSlots(
            @Param("psyId") Long psychologistId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    long countByPsychologistIdAndStatusAndDeletedFalse(
            Long psychologistId, AppointmentStatus status);

    long countByPsychologistIdAndDateTimeBetweenAndStatusNotAndDeletedFalse(
            Long psychologistId, LocalDateTime start, LocalDateTime end,
            AppointmentStatus excludeStatus);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.student sp " +
            "JOIN FETCH sp.user " +
            "WHERE a.psychologist.id = :psyId " +
            "AND a.status = 'PENDING' " +
            "AND a.deleted = false " +
            "ORDER BY a.dateTime ASC")
    List<Appointment> findPendingRequests(@Param("psyId") Long psychologistId);

    List<Appointment> findByDateTimeAfterAndStatusNot(LocalDateTime now, AppointmentStatus status);

    boolean existsByPsychologistIdAndDateTimeAndStatusNot(Long psyId, LocalDateTime dateTime, AppointmentStatus status);
    List<Appointment> findByStudentId(Long studentId);
    List<Appointment> findByStudentIdAndDateTimeBetween(Long studentId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.student s " +
            "JOIN FETCH s.user " +
            "JOIN FETCH a.psychologist " +
            "WHERE s.id = :studentId")
    Page<Appointment> findByStudentIdOrderByDateTimeDesc(@Param("studentId") Long studentId, Pageable pageable);
    long countByStatus(String status);
}
