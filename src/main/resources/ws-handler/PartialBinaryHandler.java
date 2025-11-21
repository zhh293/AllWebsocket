package sample.wshandler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Component
public class PartialBinaryHandler extends BinaryWebSocketHandler {
    @Override
    public boolean supportsPartialMessages() { return true; }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        session.sendMessage(message);
    }
}