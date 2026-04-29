package tn.enicarthage.speedenicar_projet.user.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.common.enums.Role;
import tn.enicarthage.speedenicar_projet.module_psychologue.appointment.Appointment;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    Page<User> findByRoleAndDeletedFalseOrderByLastNameAsc(Role role, Pageable pageable);

    @Query("SELECT u FROM User u " +
           "WHERE u.deleted = false " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:enabled IS NULL OR u.enabled = :enabled) " +
           "AND (:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "  OR LOWER(u.lastName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "  OR LOWER(u.email) LIKE LOWER(CONCAT('%',:search,'%'))) " +
           "ORDER BY u.createdAt DESC")
    Page<User> findWithFilters(
            @Param("role") Role role,
            @Param("enabled") Boolean enabled,
            @Param("search") String search,
            Pageable pageable);

    long countByRoleAndDeletedFalse(Role role);

    long countByEnabledTrueAndDeletedFalse();

    long countByDeletedFalse();

}
