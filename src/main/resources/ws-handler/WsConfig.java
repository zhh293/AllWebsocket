package sample.wshandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Configuration
public class WsConfig implements WebSocketConfigurer {
    @Autowired
    private EchoHandler echoHandler;
    @Autowired
    private JsonHandler jsonHandler;
    @Autowired
    private BinaryHandler binaryHandler;
    @Autowired
    private PartialBinaryHandler partialBinaryHandler;
    @Autowired
    private LifecycleHandler lifecycleHandler;
    @Autowired
    private PongHandler pongHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册一个最基本的文本 Echo 处理器到路径 "/ws/echo"
        // setAllowedOrigins：允许的跨域来源（严格匹配）；生产环境建议改为具体域名而非 "*"
        // setAllowedOriginPatterns：允许的跨域来源（通配符匹配），用于子域场景，例如 "https://*.example.com"
        // addInterceptors：握手阶段拦截器（鉴权、参数校验、将属性放入 WebSocket 会话等）
        // setHandshakeHandler：自定义握手处理（可决定用户身份 Principal、协议选择等）
        var base = registry.addHandler(echoHandler, "/ws/echo");
        base.setAllowedOrigins("*");
        base.setAllowedOriginPatterns("*");
        base.addInterceptors(
                // 将 HTTP Session 属性复制到 WebSocket 会话，常用于在握手阶段携带用户上下文
                new HttpSessionHandshakeInterceptor(),
                // 自定义握手拦截器：示例从参数中读取 userId 放到 attributes，用于后续业务使用
                new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        var uri = request.getURI();
                        var query = uri.getQuery();
                        if (query != null && query.contains("userId=")) {
                            var kv = java.util.Arrays.stream(query.split("&"))
                                    .map(s -> s.split("=", 2))
                                    .filter(arr -> arr.length == 2 && "userId".equals(arr[0]))
                                    .findFirst();
                            kv.ifPresent(arr -> attributes.put("userId", arr[1]));
                        }
                        return true; // 返回 true 才会继续握手
                    }
                    @Override
                    public void afterHandshake(ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                    }
                }
        );
        base.setHandshakeHandler(
                // 自定义握手处理：示例中根据请求头/参数决定 Principal（用户身份），便于后续按用户维度进行消息路由
                new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        String uid = (String) attributes.get("userId");
                        if (uid == null) uid = request.getHeaders().getFirst("X-User-Id");
                        final String id = uid != null ? uid : ("anon-" + java.util.UUID.randomUUID());
                        return () -> id;
                    }
                }
        );

        // 为当前路径开启 SockJS 支持，并对常见参数进行配置（心跳、缓存、流大小、客户端库 URL 等）
        var sock = base.withSockJS();
        sock.setHeartbeatTime(25000);           // SockJS 心跳间隔（毫秒）
        sock.setSessionCookieNeeded(true);      // 需要会话 Cookie（某些环境下用于粘滞/会话识别）
        sock.setHttpMessageCacheSize(1000);     // HTTP 消息缓存大小（条数）
        sock.setStreamBytesLimit(512 * 1024);   // XHR 流式传输的字节上限
        sock.setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");

        // 通过装饰器为处理器添加统一的功能（例如日志、统计、限流等）
        base.setWebSocketHandlerDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
            @Override
            public WebSocketHandler decorate(WebSocketHandler handler) {
                return new WebSocketHandlerDecorator(handler) {
                    // 这里可以重写 afterConnectionEstablished / handleMessage / afterConnectionClosed 等，加入统一的横切逻辑
                };
            }
        });

        // 可注册更多路径及不同的策略，例如：为需要更严格跨域与自定义 Principal 的路径进行单独配置
        var strict = registry.addHandler(echoHandler, "/ws/echo-strict");
        strict.setAllowedOrigins("https://example.com");
        strict.addInterceptors(new HttpSessionHandshakeInterceptor());
        strict.setHandshakeHandler(new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                // 严格模式：不允许匿名，必须携带 X-User-Id，否则拒绝（示例返回随机，但真实场景可以抛出异常并在拦截器中处理）
                String uid = request.getHeaders().getFirst("X-User-Id");
                return () -> (uid != null ? uid : ("strict-" + java.util.UUID.randomUUID()));
            }
        });
        // 如需 SockJS 回退（老旧浏览器），也可以开启
        strict.withSockJS();

        registry.addHandler(jsonHandler, "/ws/json").setAllowedOrigins("*");
        registry.addHandler(binaryHandler, "/ws/bin").setAllowedOrigins("*");
        registry.addHandler(partialBinaryHandler, "/ws/bin-partial").setAllowedOrigins("*");
        registry.addHandler(lifecycleHandler, "/ws/lifecycle").setAllowedOrigins("*");
        registry.addHandler(pongHandler, "/ws/pong").setAllowedOrigins("*");
    }
}