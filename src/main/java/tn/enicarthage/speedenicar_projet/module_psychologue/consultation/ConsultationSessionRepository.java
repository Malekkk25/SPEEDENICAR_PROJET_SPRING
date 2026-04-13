package tn.enicarthage.speedenicar_projet.module_psychologue.consultation;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationSessionRepository extends JpaRepository<ConsultationSession, Long> {

    Optional<ConsultationSession> findByRoomId(String roomId);

    Optional<ConsultationSession> findByAppointmentId(Long appointmentId);

    @Query("""
        SELECT cs FROM ConsultationSession cs
        JOIN cs.appointment a
        WHERE a.student.user.id = :userId
        AND cs.roomId = :roomId
    """)
    Optional<ConsultationSession> findByRoomIdAndStudentUserId(String roomId, Long userId);

    @Query("SELECT cs FROM ConsultationSession cs " +
            "JOIN cs.appointment a " +
            "WHERE a.psychologist.id = :userId " + // <-- Enlevez juste le ".user"
            "AND cs.roomId = :roomId")
    Optional<ConsultationSession> findByRoomIdAndPsychologistUserId(@Param("roomId") String roomId, @Param("userId") Long userId);
}

