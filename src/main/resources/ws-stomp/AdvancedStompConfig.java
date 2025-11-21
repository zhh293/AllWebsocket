package sample.wsstomp;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class AdvancedStompConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 设置应用目的地前缀，客户端发送到 /app/** 才会路由到 @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
        // 启用内置简单代理，支持主题广播 /topic、点对点队列 /queue、用户专属目的地 /user
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        // 用户目的地前缀，用于 convertAndSendToUser("user","/queue/..",payload)
        registry.setUserDestinationPrefix("/user");
        // 保持消息发布顺序，对某些严格顺序消费场景有帮助
        registry.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 原生 WebSocket STOMP 端点，允许任意来源（生产环境请改为具体域名）
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        // SockJS 端点，开启心跳；用于旧浏览器或受限网络下的回退
        registry.addEndpoint("/ws-sock").setAllowedOriginPatterns("*").withSockJS().setHeartbeatTime(25000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 客户端入站通道拦截器：可在 CONNECT/SUBSCRIBE/SEND 等命令上做鉴权、限流、审计
        registration.interceptors(new StompChannelInterceptor());
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 客户端出站通道拦截器：可统一添加头部、做统计或过滤
        registration.interceptors(new StompChannelInterceptor());
    }

    @Override
    public boolean configureMessageConverters(List<org.springframework.messaging.converter.MessageConverter> converters) {
        // 增加 JSON 消息转换器，使 @MessageMapping 支持对象参数/返回值自动序列化
        converters.add(new MappingJackson2MessageConverter());
        // 返回 false：保留 Spring 默认转换器；返回 true：仅使用当前列表
        return false;
    }

    @Override
    public void configureWebSocketTransport(org.springframework.web.socket.config.annotation.WebSocketTransportRegistration registration) {
        // 设置发送时间上限（毫秒），避免大消息/慢客户端阻塞
        registration.setSendTimeLimit(20000);
        // 发送缓冲区上限（字节），防止堆积占用过大内存
        registration.setSendBufferSizeLimit(512 * 1024);
        // 单条消息大小上限（字节），保护服务端不被超大消息拖垮
        registration.setMessageSizeLimit(256 * 1024);
    }
}