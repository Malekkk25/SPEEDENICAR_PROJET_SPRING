package tn.enicarthage.speedenicar_projet.module_psychologue.consultation;

import org.springframework.data.jpa.repository.EntityGraph; // Import indispensable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationSessionRepository extends JpaRepository<ConsultationSession, Long> {

    // On ajoute l'EntityGraph pour charger tout le graphe d'objets d'un coup
    @EntityGraph(attributePaths = {"appointment", "appointment.student", "appointment.student.user", "appointment.psychologist"})
    Optional<ConsultationSession> findByRoomId(String roomId);

    @EntityGraph(attributePaths = {"appointment", "appointment.student", "appointment.student.user", "appointment.psychologist"})
    Optional<ConsultationSession> findByAppointmentId(Long appointmentId);

    @Query("""
        SELECT cs FROM ConsultationSession cs
        JOIN FETCH cs.appointment a
        JOIN FETCH a.student s
        JOIN FETCH s.user u
        WHERE u.id = :userId
        AND cs.roomId = :roomId
    """)
    Optional<ConsultationSession> findByRoomIdAndStudentUserId(@Param("roomId") String roomId, @Param("userId") Long userId);

    @Query("""
        SELECT cs FROM ConsultationSession cs 
        JOIN FETCH cs.appointment a 
        JOIN FETCH a.psychologist p
        WHERE p.id = :userId 
        AND cs.roomId = :roomId
    """)
    Optional<ConsultationSession> findByRoomIdAndPsychologistUserId(@Param("roomId") String roomId, @Param("userId") Long userId);
}