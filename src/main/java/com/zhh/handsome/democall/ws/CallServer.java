package com.zhh.handsome.democall.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/demo/ws/call/{userId}")
@Slf4j
public class CallServer {
    private static final ObjectMapper M = new ObjectMapper();
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        SESSIONS.put(userId, session);
        log.debug("Opened connection for userId: {}", userId);
        send(session, msg("HEARTBEAT", null, null, null));
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long userId) {
        try {
            Map<?,?> map = M.readValue(message, Map.class);
            String action = (String) map.get("action");
            String callId = (String) map.get("callId");
            Long toUserId = map.get("toUserId") == null ? null : Long.valueOf(map.get("toUserId").toString());
            if ("INITIATE".equals(action)) {
                if (toUserId == null || !SESSIONS.containsKey(toUserId)) {
                    send(userId, msg("ERROR", callId, null, Map.of("reason","USER_OFFLINE")));
                    return;
                }
                String id = callId != null ? callId : java.util.UUID.randomUUID().toString();
                send(toUserId, msg("RINGING", id, userId, map));
                log.debug("Initiated call from {} to {} with id {}", userId, toUserId, id);
                send(userId, msg("INITIATE_ACK", id, toUserId, null));
                log.debug("Sent INITIATE_ACK to {}", userId);
                return;
            }
            if ("ANSWER".equals(action) || "REJECT".equals(action) || "CANCEL".equals(action) || "END".equals(action)
                    || "ICE".equals(action) || "SDP_OFFER".equals(action) || "SDP_ANSWER".equals(action)) {
                Long target = toUserId;
                if (target == null && map.get("fromUserId") != null) {
                    target = Long.valueOf(map.get("fromUserId").toString());
                }
                if (target != null && SESSIONS.containsKey(target)) {
                    send(target, msg(action, callId, userId, map.get("payload")));
                } else {
                    send(userId, msg("ERROR", callId, null, Map.of("reason","CALL_NOT_FOUND")));
                }
                return;
            }
            if ("HEARTBEAT".equals(action)) {
                send(userId, msg("HEARTBEAT", callId, null, null));
                return;
            }
            send(userId, msg("ERROR", callId, null, Map.of("reason","UNSUPPORTED_ACTION")));
        } catch (Exception e) {
            send(userId, msg("ERROR", null, null, Map.of("reason","INVALID_PAYLOAD")));
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        SESSIONS.remove(userId);
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Long userId) {
        SESSIONS.remove(userId);
    }

    private void send(Long userId, Map<String,Object> data) {
        Session s = SESSIONS.get(userId);
        send(s, data);
    }

    private void send(Session s, Map<String,Object> data) {
        try {
            if (s != null && s.isOpen()) s.getBasicRemote().sendText(M.writeValueAsString(data));
        } catch (Exception ignored) {}
    }

    private Map<String,Object> msg(String action, String callId, Long toUserId, Object payload) {
        Map<String,Object> m = new java.util.HashMap<>();
        m.put("action", action);
        if (callId != null) m.put("callId", callId);
        if (toUserId != null) m.put("toUserId", toUserId);
        if (payload != null) m.put("payload", payload);
        m.put("timestamp", Instant.now().toEpochMilli());
        return m;
    }
}