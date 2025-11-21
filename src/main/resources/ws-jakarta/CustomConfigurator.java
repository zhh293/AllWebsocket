package sample.wsjakarta;

import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class CustomConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, jakarta.websocket.server.HandshakeResponse response) {
        // 握手阶段自定义：从查询参数/头部/HttpSession 中提取信息放入 UserProperties
        // 端点的 @OnOpen 可通过 EndpointConfig 读取这些属性，实现鉴权与上下文绑定
        var params = request.getParameterMap();
        if (params != null && params.containsKey("userId") && !params.get("userId").isEmpty()) {
            sec.getUserProperties().put("userId", params.get("userId").get(0));
        }
    }
}