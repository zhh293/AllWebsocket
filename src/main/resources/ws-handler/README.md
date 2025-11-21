# Spring WebSocket Handler（MVC 风格）

## 特点
- 使用 `WebSocketHandler`（如 `TextWebSocketHandler`），通过 `WebSocketConfigurer` 注册路径。
- 与 Spring MVC 生态天然融合，易于添加拦截器、跨域、认证、限流等。
- 适合轻量实时通信、Echo、通知推送等。

## 关键文件
- 路径注册：`src/main/resources/ws-handler/WsConfig.java:14-16`（将处理器映射到 `"/ws/echo"`）
- 处理器实现：`src/main/resources/ws-handler/EchoHandler.java:10-12`（Echo 文本消息）
- 前端示例：`src/main/resources/ws-handler/index.html:7-9`（连接 `ws://localhost:8082/ws/echo` 并发送）

## 重要函数
- `registerWebSocketHandlers(...)`：注册处理器与跨域策略（`src/main/resources/ws-handler/WsConfig.java:14-16`）。
- `handleTextMessage(...)`：收到文本消息时的处理逻辑（`src/main/resources/ws-handler/EchoHandler.java:10-12`）。

## 使用时机
- 希望利用 Spring MVC 的拦截器、`HandshakeInterceptor`、跨域配置等能力。
- 简单聊天室、Echo 服务、轻量级的实时通知。
- 需要在 WebSocket 握手阶段与现有安全体系集成。

## 细节
- 可对接 `HandshakeHandler/Interceptor` 做认证与上下文绑定。
- 与 `@ServerEndpoint` 相比，路径与拦截器声明更集中在 Spring 配置中，维护更直观。

## 何时选择它
- 面向 Web 应用的实时功能，与 MVC 深度结合。
- 需要在握手与处理链中插入 Spring 拦截器的场景。