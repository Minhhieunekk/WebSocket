package com.example.websocket.websocket_config;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerAdvance extends TextWebSocketHandler {

    // Lưu trữ các session theo userId (A, B, C)
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        sessions.put(userId, session);
        session.sendMessage(new TextMessage("User " + userId + " connected!"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received message: " + payload);

        String[] parts = payload.split(":", 2);
        if (parts.length < 2) {
            session.sendMessage(new TextMessage("Invalid format. Use 'B:Hello' to send to B."));
            return;
        }

        String receiverId = parts[0];
        String content = parts[1];

        WebSocketSession receiverSession = sessions.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage("From " + getUserIdFromSession(session) + ": " + content));
        } else {
            session.sendMessage(new TextMessage("User " + receiverId + " is not online."));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().removeIf(s -> s.getId().equals(session.getId()));
    }

    private String getUserIdFromSession(WebSocketSession session) {
        return session.getUri().getQuery().replace("user=", ""); // Trích xuất user từ URL
    }
}
