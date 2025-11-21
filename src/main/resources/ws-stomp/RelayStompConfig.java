package sample.wsstomp;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class RelayStompConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 应用目的地前缀：客户端向 /app/** 发送将进入 @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
        // 启用外部 STOMP 代理转发（RabbitMQ/ActiveMQ 等），用于水平扩展与持久化路由
        registry.enableStompBrokerRelay("/topic", "/queue")
                // 代理地址与端口（RabbitMQ 默认 61613）
                .setRelayHost("localhost")
                .setRelayPort(61613)
                // 客户端连接凭据（浏览器连接到代理时使用）
                .setClientLogin("guest")
                .setClientPasscode("guest")
                // 系统连接凭据（应用与代理的后台通道）
                .setSystemLogin("guest")
                .setSystemPasscode("guest")
                // 虚拟主机（RabbitMQ 的 vhost）
                .setVirtualHost("/");
        // 用户目的地前缀，用于点对点发送
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 STOMP 端点并开启 SockJS 回退；允许通配来源（生产环境请限制域）
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}