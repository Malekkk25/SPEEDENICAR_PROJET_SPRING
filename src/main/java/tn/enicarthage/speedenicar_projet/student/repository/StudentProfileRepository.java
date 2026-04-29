package tn.enicarthage.speedenicar_projet.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {



    Optional<StudentProfile> findByUserId(Long userId);

    boolean existsByStudentId(String studentId);

    @Query("SELECT sp FROM StudentProfile sp " +
            "JOIN FETCH sp.user u " +
            "WHERE u.id = :userId AND sp.deleted = false")
    Optional<StudentProfile> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("SELECT sp FROM StudentProfile sp " +
            "JOIN FETCH sp.user u " +
            "WHERE sp.studentId = :studentId AND sp.deleted = false")
    Optional<StudentProfile> findByStudentIdWithUser(@Param("studentId") String studentId);


}

