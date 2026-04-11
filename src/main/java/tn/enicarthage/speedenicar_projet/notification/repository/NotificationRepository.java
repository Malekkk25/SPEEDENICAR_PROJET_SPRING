package tn.enicarthage.speedenicar_projet.notification.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.speedenicar_projet.notification.entity.Notification;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdAndDeletedFalseOrderByCreatedAtDesc(
            Long recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(
            Long recipientId, Pageable pageable);

    long countByRecipientIdAndReadFalseAndDeletedFalse(Long recipientId);

    @Modifying
   /* @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.recipient.id = :userId AND n.read = false")*/
    @Query("UPDATE Notification n SET n.read = true, n.readAt = LOCAL DATETIME " +
            "WHERE n.recipient.id = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") Long userId);
    //int markAllAsRead(@Param("userId") Long userId);
}
