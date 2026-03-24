package tn.enicarthage.speedenicar_projet.messaging.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.messaging.entity.Conversation;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c " +
           "WHERE (c.participantOne.id = :userId OR c.participantTwo.id = :userId) " +
           "AND c.deleted = false " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM Conversation c " +
           "WHERE ((c.participantOne.id = :u1 AND c.participantTwo.id = :u2) " +
           "   OR  (c.participantOne.id = :u2 AND c.participantTwo.id = :u1)) " +
           "AND c.deleted = false")
    Optional<Conversation> findBetweenUsers(@Param("u1") Long user1, @Param("u2") Long user2);
}
