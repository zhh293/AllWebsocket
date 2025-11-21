package sample.wshandler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Component
public class PongHandler extends AbstractWebSocketHandler {
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
    }
}