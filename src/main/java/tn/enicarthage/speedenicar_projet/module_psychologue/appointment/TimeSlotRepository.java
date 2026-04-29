package tn.enicarthage.speedenicar_projet.module_psychologue.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.module_psychologue.entity.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    @Modifying
    @Query("DELETE FROM TimeSlot t WHERE t.psychologist.id = :psyId")
    void deleteByPsychologistId(@Param("psyId") Long psyId);

    // Changement ici : AndDeletedFalse au lieu de AndIsDeletedFalse
    List<TimeSlot> findByPsychologistIdAndDeletedFalseOrderByDayOfWeekAscStartTimeAsc(Long psychologistId);

    // CORRECTION ICI : Ajout du paramètre "Long psychologistId"
    List<TimeSlot> findByPsychologistIdAndDayOfWeekAndAvailableTrueAndDeletedFalse(Long psychologistId, DayOfWeek dayOfWeek);

    // On remplace 'AndDayOfWeek' par 'AndDayOfWeekIn' et on prend une List<String>
    List<TimeSlot> findByDayOfWeekInAndAvailableTrueAndDeletedFalse(List<String> daysOfWeek);

    List<TimeSlot> findByDayOfWeekAndAvailableTrueAndDeletedFalse(DayOfWeek dayOfWeek);


    List<TimeSlot> findByAvailableTrue();
}