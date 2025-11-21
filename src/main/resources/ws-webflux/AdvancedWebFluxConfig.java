package sample.wswebflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;

@Configuration
public class AdvancedWebFluxConfig {
    @Bean
    public ObjectMapper objectMapper() { return new ObjectMapper(); }

    @Bean
    public Sinks.Many<String> broadcastSink() { return Sinks.many().multicast().directBestEffort(); }

    @Bean
    public SimpleUrlHandlerMapping rxHandlerMapping(WebSocketHandler echo,
                                                    WebSocketHandler json,
                                                    WebSocketHandler upper,
                                                    WebSocketHandler broadcast,
                                                    WebSocketHandler info) {
        SimpleUrlHandlerMapping m = new SimpleUrlHandlerMapping();
        m.setOrder(1);
        m.setUrlMap(Map.of(
                "/ws/rx/echo", echo,
                "/ws/rx/json", json,
                "/ws/rx/upper", upper,
                "/ws/rx/broadcast", broadcast,
                "/ws/rx/info", info
        ));
        return m;
    }

    @Bean
    public WebSocketHandlerAdapter rxHandlerAdapter() { return new WebSocketHandlerAdapter(); }

    @Bean
    public WebSocketHandler echo() {
        return session -> {
            Flux<String> in = session.receive().map(msg -> msg.getPayloadAsText());
            Flux<org.springframework.web.reactive.socket.WebSocketMessage> out = in.map(text -> session.textMessage(text));
            return session.send(out);
        };
    }

    @Bean
    public WebSocketHandler json(ObjectMapper M) {
        return session -> {
            Flux<String> in = session.receive().map(msg -> msg.getPayloadAsText());
            Flux<org.springframework.web.reactive.socket.WebSocketMessage> out = in.map(payload -> {
                try {
                    java.util.Map<?,?> map = M.readValue(payload, java.util.Map.class);
                    String normalized = M.writeValueAsString(map);
                    return session.textMessage(normalized);
                } catch (Exception e) {
                    return session.textMessage("{}");
                }
            });
            return session.send(out);
        };
    }

    @Bean
    public WebSocketHandler upper() {
        return session -> {
            Flux<String> in = session.receive().map(msg -> msg.getPayloadAsText());
            Flux<org.springframework.web.reactive.socket.WebSocketMessage> out = in.map(s -> session.textMessage(s.toUpperCase()));
            return session.send(out);
        };
    }

    @Bean
    public WebSocketHandler broadcast(Sinks.Many<String> sink) {
        return session -> {
            Flux<org.springframework.web.reactive.socket.WebSocketMessage> outbound = sink.asFlux().map(s -> session.textMessage(s));
            Mono<Void> sendMono = session.send(outbound);
            Mono<Void> recvMono = session.receive()
                    .map(msg -> msg.getPayloadAsText())
                    .doOnNext(s -> sink.tryEmitNext(s))
                    .then();
            return Mono.when(sendMono, recvMono);
        };
    }

    @Bean
    public WebSocketHandler info() {
        return session -> {
            String uri = session.getHandshakeInfo().getUri().toString();
            org.springframework.http.HttpHeaders headers = session.getHandshakeInfo().getHeaders();
            String agent = headers.getFirst("User-Agent");
            String id = session.getId();
            String json = String.format("{\"id\":\"%s\",\"uri\":\"%s\",\"ua\":\"%s\"}", id, uri, agent == null ? "" : agent.replace("\"",""));
            return session.send(Flux.just(session.textMessage(json)));
        };
    }
}