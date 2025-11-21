package sample.wswebflux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

@Configuration
public class WebFluxConfig {
    @Bean
    public SimpleUrlHandlerMapping handlerMapping(WebSocketHandler echo) {
        SimpleUrlHandlerMapping m = new SimpleUrlHandlerMapping();
        m.setOrder(1);
        m.setUrlMap(Map.of("/ws/rx/echo", echo));
        return m;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() { return new WebSocketHandlerAdapter(); }

    @Bean
    public WebSocketHandler echo() {
        return session -> session.receive()
                .map(msg -> msg.getPayloadAsText())
                .map(text -> session.textMessage(text))
                .as(session::send);
    }
}