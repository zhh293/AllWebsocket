package sample.wsjakarta;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/ws/jakarta/adv/{userId}",
        decoders = {JsonMessageDecoder.class},
        encoders = {JsonMessageEncoder.class},
        configurator = CustomConfigurator.class,
        subprotocols = {"json"})
public class AdvancedCallServer {
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("userId") Long userId) {
        // 握手成功后加入在线表；可从 EndpointConfig/UserProperties 读取在握手阶段传入的上下文
        SESSIONS.put(userId, session);
        // 服务端主动发送 Ping，客户端将自动回 Pong；用于活跃度检测
        session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[]{0}));
    }

    @OnMessage
    public void onText(Session session, String message) {
        // 文本消息：最简单的 echo 示例
        session.getAsyncRemote().sendText(message);
    }

    @OnMessage
    public void onBinary(Session session, ByteBuffer data, boolean last) {
        // 二进制分片：只有在最后一片时回应；实际业务可在此聚合分片
        if (last) session.getAsyncRemote().sendBinary(data);
    }

    @OnMessage
    public void onJson(Session session, JsonMessage msg) {
        // JSON 对象消息：通过 Decoder/Encoder 进行类型安全收发
        session.getAsyncRemote().sendObject(msg);
    }

    @OnMessage
    public void onPong(Session session, PongMessage message) {
        // 客户端 Pong 回应：可记录心跳延迟与活跃状态
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Long userId) {
        // 传输错误：移除在线并可记录日志/告警
        SESSIONS.remove(userId);
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        // 连接关闭：清理在线表与资源
        SESSIONS.remove(userId);
    }
}