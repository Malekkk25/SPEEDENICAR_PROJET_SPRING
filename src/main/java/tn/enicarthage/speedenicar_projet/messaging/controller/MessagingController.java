package tn.enicarthage.speedenicar_projet.messaging.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.common.dto.ApiResponse;
import tn.enicarthage.speedenicar_projet.messaging.dto.ConversationResponse;
import tn.enicarthage.speedenicar_projet.messaging.dto.MessageResponse;
import tn.enicarthage.speedenicar_projet.messaging.dto.SendMessageRequest;
import tn.enicarthage.speedenicar_projet.messaging.service.MessagingService;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService messagingService;

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> getConversations(
            @AuthenticationPrincipal UserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = Long.parseLong(user.getUsername());
        return ResponseEntity.ok(
                ApiResponse.ok(messagingService.getConversations(userId, pageable)));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @PageableDefault(size = 30) Pageable pageable) {
        Long userId = Long.parseLong(user.getUsername());
        return ResponseEntity.ok(
                ApiResponse.ok(messagingService.getMessages(userId, id, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody SendMessageRequest request) {
        Long userId = Long.parseLong(user.getUsername());
        MessageResponse response = messagingService.sendMessage(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Message envoyé"));
    }

    @PutMapping("/conversations/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markConversationAsRead(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        Long userId = Long.parseLong(user.getUsername());
        messagingService.markConversationAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Conversation marquée comme lue"));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserDetails user) {
        Long userId = Long.parseLong(user.getUsername());
        return ResponseEntity.ok(
                ApiResponse.ok(Map.of("count",
                        messagingService.getUnreadMessageCount(userId))));
    }
}
