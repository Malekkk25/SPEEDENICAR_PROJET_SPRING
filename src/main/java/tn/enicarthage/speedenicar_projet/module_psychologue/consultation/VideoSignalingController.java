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

    // Stocke les participants par roomId en mémoire
    // clé = roomId, valeur = Map<senderId, displayName>
    private final Map<String, Map<String, String>> rooms = new ConcurrentHashMap<>();

    @MessageMapping("/signal/{roomId}")
    public void handleSignal(@DestinationVariable String roomId,
                             ConsultationDto.SignalMessage signal) {

        String senderId = signal.getSenderId();
        log.info("Signal reçu - room: {} | type: {} | sender: {}", roomId, signal.getType(), senderId);

        switch (signal.getType()) {

            case "join" -> {
                Map<String, String> participants = rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());

                // ✅ LOG 1 — voir ce que le serveur reçoit exactement
                log.info("JOIN reçu - senderId: '{}' | payload: '{}' | participants actuels: {}",
                        senderId, signal.getPayload(), participants.keySet());

                if (!participants.isEmpty()) {
                    Map.Entry<String, String> existing = participants.entrySet().iterator().next();

                    // ✅ LOG 2 — voir vers quel canal on envoie peer-present
                    String privateChannel = "/topic/room/" + roomId + "/user/" + senderId;
                    log.info("Envoi peer-present vers: {}", privateChannel);

                    Map<String, String> peerPresentPayload = Map.of(
                            "peerId", existing.getKey(),
                            "peerName", existing.getValue()
                    );

                    ConsultationDto.SignalMessage peerPresent = new ConsultationDto.SignalMessage();
                    peerPresent.setType("peer-present");
                    peerPresent.setRoomId(roomId);
                    peerPresent.setSenderId("server");
                    peerPresent.setPayload(peerPresentPayload);

                    messagingTemplate.convertAndSend(privateChannel, peerPresent);
                }

                messagingTemplate.convertAndSend("/topic/room/" + roomId, signal);
                participants.put(senderId, String.valueOf(signal.getPayload()));
                log.info("Participants après ajout: {}", participants);
            }
            case "leave" -> {
                // Retirer le participant
                Map<String, String> participants = rooms.get(roomId);
                if (participants != null) {
                    participants.remove(senderId);
                    if (participants.isEmpty()) {
                        rooms.remove(roomId); // Nettoyer la room vide
                    }
                }
                // Broadcaster le départ
                messagingTemplate.convertAndSend("/topic/room/" + roomId, signal);
                log.info("Participant {} a quitté room {}", senderId, roomId);
            }

            // offer, answer, ice-candidate → simple relay à toute la room
            default -> messagingTemplate.convertAndSend("/topic/room/" + roomId, signal);
        }
    }}