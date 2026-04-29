package tn.enicarthage.speedenicar_projet.module_psychologue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsychologistProfileRepository extends JpaRepository<User, Long> {

}

