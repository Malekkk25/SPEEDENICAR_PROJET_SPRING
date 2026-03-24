package tn.enicarthage.speedenicar_projet.messaging.repository;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.messaging.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(
            Long conversationId, Pageable pageable);

    Page<Message> findByConversationIdAndDeletedFalseOrderByCreatedAtDesc(
            Long conversationId, Pageable pageable);

    long countByReceiverIdAndReadFalseAndDeletedFalse(Long receiverId);

    long countByConversationIdAndReceiverIdAndReadFalseAndDeletedFalse(
            Long conversationId, Long receiverId);

    @Modifying
    @Query("UPDATE Message m SET m.read = true, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.conversation.id = :convId " +
           "AND m.receiver.id = :userId " +
           "AND m.read = false")
    int markConversationAsRead(
            @Param("convId") Long conversationId,
            @Param("userId") Long userId);
}
