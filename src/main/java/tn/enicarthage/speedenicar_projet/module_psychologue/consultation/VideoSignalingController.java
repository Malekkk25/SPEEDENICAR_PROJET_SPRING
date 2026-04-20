package tn.enicarthage.speedenicar_projet.module_psychologue.consultation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère le signaling WebRTC via WebSocket STOMP.
 *
 * Flux de signaling (côté serveur = simple relais) :
 *  1. Participant A s'abonne à /topic/room/{roomId}
 *  2. Participant A envoie un "join" → serveur relaye à /topic/room/{roomId}
 *  3. Participant B reçoit le "join" et envoie un "offer" (SDP)
 *  4. Participant A reçoit l'"offer" et répond avec "answer"
 *  5. Échange des ICE candidates entre A et B
 *  6. Connexion P2P établie → flux vidéo direct (pas par le serveur)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class VideoSignalingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConsultationSessionRepository sessionRepository;

    /**
     * Participants actifs par room : roomId → Set<senderId>
     * Utilisé pour savoir si un peer est déjà présent.
     */
    private final Map<String, Map<String, String>> roomParticipants = new ConcurrentHashMap<>();

    /**
     * Envoi d'un message de signaling WebRTC.
     * Destination cliente : /app/signal/{roomId}
     * Broadcast vers     : /topic/room/{roomId}
     */
    @MessageMapping("/signal/{roomId}")
    public void handleSignal(
            @DestinationVariable String roomId,
            @Payload ConsultationDto.SignalMessage message,
            Authentication auth) {

        // Vérifier que la room existe
        if (sessionRepository.findByRoomId(roomId).isEmpty()) {
            log.warn("Signal reçu pour une room inexistante: {}", roomId);
            return;
        }

        message.setRoomId(roomId);
        String senderId = auth.getName(); // email ou username JWT
        message.setSenderId(senderId);

        log.debug("Signal [{}] dans room {} de {}", message.getType(), roomId, senderId);

        switch (message.getType()) {
            case "join" -> handleJoin(roomId, senderId, message);
            case "leave" -> handleLeave(roomId, senderId, message);
            // offer, answer, ice-candidate → relayés directement
            default -> broadcast(roomId, message, senderId);
        }
    }

    private void handleJoin(String roomId, String senderId, ConsultationDto.SignalMessage message) {
        roomParticipants.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        Map<String, String> participants = roomParticipants.get(roomId);

        boolean peerPresent = !participants.isEmpty();
        String peerId = participants.keySet().stream().findFirst().orElse(null);
        String peerName = peerId != null ? participants.get(peerId) : null;

        // Enregistrer ce participant
        String displayName = message.getPayload() != null ? message.getPayload().toString() : senderId;
        participants.put(senderId, displayName);

        // Informer les autres qu'un nouveau participant a rejoint
        broadcast(roomId, message, senderId);

        // Envoyer en retour si un peer est déjà là (pour déclencher l'offer WebRTC)
        if (peerPresent) {
            ConsultationDto.SignalMessage readyMsg = new ConsultationDto.SignalMessage();
            readyMsg.setType("peer-present");
            readyMsg.setRoomId(roomId);
            readyMsg.setSenderId("server");
            readyMsg.setPayload(Map.of("peerId", peerId, "peerName", peerName != null ? peerName : ""));

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/user/" + senderId,
                    readyMsg
            );
        }

        log.info("Participant {} a rejoint la room {}. Participants: {}", senderId, roomId, participants.size());
    }

    private void handleLeave(String roomId, String senderId, ConsultationDto.SignalMessage message) {
        Map<String, String> participants = roomParticipants.getOrDefault(roomId, new ConcurrentHashMap<>());
        participants.remove(senderId);

        if (participants.isEmpty()) {
            roomParticipants.remove(roomId);
        }

        broadcast(roomId, message, senderId);
        log.info("Participant {} a quitté la room {}", senderId, roomId);
    }

    /**
     * Broadcast à tous les participants de la room SAUF l'expéditeur.
     */
    private void broadcast(String roomId, ConsultationDto.SignalMessage message, String senderId) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }}
