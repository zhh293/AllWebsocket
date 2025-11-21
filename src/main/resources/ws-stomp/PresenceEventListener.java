package sample.wsstomp;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class PresenceEventListener {
    @EventListener
    public void onConnect(SessionConnectEvent event) {
        // 客户端建立 STOMP 连接事件：可做在线统计、记录用户信息、绑定上下文
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        accessor.getSessionId();
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        // 客户端断开 STOMP 连接事件：可做资源清理与在线数更新
        event.getSessionId();
    }
}