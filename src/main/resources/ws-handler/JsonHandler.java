package sample.wshandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class JsonHandler extends TextWebSocketHandler {
    private static final ObjectMapper M = new ObjectMapper();
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        var map = M.readValue(message.getPayload(), java.util.Map.class);
        session.sendMessage(new TextMessage(M.writeValueAsString(map)));
    }
}