package sample.wsjakarta;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

@Component
@ServerEndpoint("/ws/jakarta/{userId}")
public class CallServer {
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {}

    @OnMessage
    public void onMessage(Session session, String message) { session.getAsyncRemote().sendText(message); }

    @OnClose
    public void onClose(@PathParam("userId") Long userId) {}

    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Long userId) {}
}