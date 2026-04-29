package tn.enicarthage.speedenicar_projet.messaging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.common.enums.NotificationType;
import tn.enicarthage.speedenicar_projet.common.exception.BusinessException;
import tn.enicarthage.speedenicar_projet.common.exception.ResourceNotFoundException;
import tn.enicarthage.speedenicar_projet.messaging.dto.ConversationResponse;
import tn.enicarthage.speedenicar_projet.messaging.dto.MessageResponse;
import tn.enicarthage.speedenicar_projet.messaging.dto.SendMessageRequest;
import tn.enicarthage.speedenicar_projet.messaging.entity.Conversation;
import tn.enicarthage.speedenicar_projet.messaging.entity.Message;
import tn.enicarthage.speedenicar_projet.messaging.repository.ConversationRepository;
import tn.enicarthage.speedenicar_projet.messaging.repository.MessageRepository;
import tn.enicarthage.speedenicar_projet.notification.service.NotificationService;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MessagingService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    // ═══════════════════════════════════════════════════════
    //  CONVERSATIONS
    // ═══════════════════════════════════════════════════════

    public Page<ConversationResponse> getConversations(Long userId, Pageable pageable) {
        return conversationRepo.findByUserId(userId, pageable)
                .map(conv -> toConversationResponse(conv, userId));
    }

    // ═══════════════════════════════════════════════════════
    //  MESSAGES
    // ═══════════════════════════════════════════════════════

    public Page<MessageResponse> getMessages(Long userId, Long conversationId,
                                             Pageable pageable) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation", "id", conversationId));

        if (!conv.involvesUser(userId)) {
            throw new BusinessException("Vous n'avez pas accès à cette conversation");
        }

        return messageRepo
                .findByConversationIdAndDeletedFalseOrderByCreatedAtDesc(
                        conversationId, pageable)
                .map(msg -> toMessageResponse(msg, userId));
    }

    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", senderId));
        User receiver = userRepo.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", request.getReceiverId()));

        if (senderId.equals(request.getReceiverId())) {
            throw new BusinessException("Vous ne pouvez pas vous envoyer un message");
        }

        // Find or create conversation
        Conversation conversation = conversationRepo
                .findBetweenUsers(senderId, request.getReceiverId())
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .participantOne(sender)
                            .participantTwo(receiver)
                            .build();
                    return conversationRepo.save(newConv);
                });

        // Create message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentName(request.getAttachmentName())
                .build();

        Message saved = messageRepo.save(message);

        // Update conversation preview
        conversation.updateLastMessage(request.getContent());
        conversationRepo.save(conversation);

        // Push via WebSocket
        MessageResponse response = toMessageResponse(saved, senderId);
        try {
            messagingTemplate.convertAndSendToUser(
                    receiver.getId().toString(),
                    "/queue/messages",
                    toMessageResponse(saved, receiver.getId()));
        } catch (Exception e) {
            log.warn("WebSocket push failed for message to user {}", receiver.getId());
        }

        // Send notification
        notificationService.send(receiver,
                "Nouveau message de " + sender.getFullName(),
                request.getContent().length() > 100
                        ? request.getContent().substring(0, 97) + "..."
                        : request.getContent(),
                NotificationType.MESSAGE,
                "/messages/" + conversation.getId(),
                sender.getFullName());

        log.debug("Message sent from {} to {} in conversation {}",
                senderId, receiver.getId(), conversation.getId());

        return response;
    }

    @Transactional
    public void markConversationAsRead(Long userId, Long conversationId) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation", "id", conversationId));
        if (!conv.involvesUser(userId)) {
            throw new BusinessException("Vous n'avez pas accès à cette conversation");
        }
        messageRepo.markConversationAsRead(conversationId, userId);
    }

    public long getUnreadMessageCount(Long userId) {
        return messageRepo.countByReceiverIdAndReadFalseAndDeletedFalse(userId);
    }

    // ═══════════════════════════════════════════════════════
    //  MAPPERS
    // ═══════════════════════════════════════════════════════

    private ConversationResponse toConversationResponse(Conversation c, Long currentUserId) {
        User otherUser = c.getOtherParticipant(currentUserId);
        long unread = messageRepo
                .countByConversationIdAndReceiverIdAndReadFalseAndDeletedFalse(
                        c.getId(), currentUserId);

        return ConversationResponse.builder()
                .id(c.getId())
                .otherUserId(otherUser.getId())
                .otherUserName(otherUser.getFullName())
                .otherUserRole(otherUser.getRole().name())
                .lastMessagePreview(c.getLastMessagePreview())
                .lastMessageAt(c.getLastMessageAt())
                .unreadCount(unread)
                .build();
    }

    private MessageResponse toMessageResponse(Message m, Long currentUserId) {
        return MessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFullName())
                .receiverId(m.getReceiver().getId())
                .receiverName(m.getReceiver().getFullName())
                .content(m.getContent())
                .read(m.getRead())
                .readAt(m.getReadAt())
                .attachmentUrl(m.getAttachmentUrl())
                .attachmentName(m.getAttachmentName())
                .createdAt(m.getCreatedAt())
                .isMine(m.getSender().getId().equals(currentUserId))
                .build();
    }
}