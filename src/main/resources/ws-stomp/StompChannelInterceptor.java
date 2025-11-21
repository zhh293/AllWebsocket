package sample.wsstomp;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

public class StompChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 预发送拦截：可在 CONNECT/SUBSCRIBE/SEND 等命令到达通道前进行检查或增强
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 示例：读取客户端自定义头部做鉴权；实际可验证 token 并绑定到用户上下文
            String token = accessor.getFirstNativeHeader("Authorization");
        }
        return message;
    }
}