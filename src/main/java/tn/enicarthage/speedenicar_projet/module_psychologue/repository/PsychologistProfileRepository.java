package tn.enicarthage.speedenicar_projet.module_psychologue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.module_psychologue.entity.PsychologistProfile;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsychologistProfileRepository extends JpaRepository<PsychologistProfile, Long> {

    Optional<PsychologistProfile> findByUserId(Long userId);

    Optional<PsychologistProfile> findByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    @Query("SELECT pp FROM PsychologistProfile pp " +
            "JOIN FETCH pp.user u " +
            "WHERE u.id = :userId AND pp.deleted = false")
    Optional<PsychologistProfile> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("SELECT pp FROM PsychologistProfile pp " +
            "JOIN FETCH pp.user u " +
            "WHERE pp.deleted = false AND u.enabled = true " +
            "ORDER BY u.lastName ASC")
    List<PsychologistProfile> findAllActive();
}

