# WebFlux 响应式 WebSocket

## 特点
- 基于 Reactor 的响应式处理模型，天然支持高并发与背压。
- `WebSocketHandler` 通过流式 `receive/send` 处理消息，表达异步管线。
- 适合 I/O 密集、数据流推送、实时日志/指标等场景。

## 关键文件
- 路径映射：`src/main/resources/ws-webflux/WebFluxConfig.java:13-18`（将 `"/ws/rx/echo"` 映射到处理器）
- 处理器适配器：`src/main/resources/ws-webflux/WebFluxConfig.java:21-22`
- Echo 处理器：`src/main/resources/ws-webflux/WebFluxConfig.java:25-30`（接收文本并原样发送）

## 重要函数
- `SimpleUrlHandlerMapping`：声明 WebSocket 路由（`src/main/resources/ws-webflux/WebFluxConfig.java:13-18`）。
- `WebSocketHandlerAdapter`：适配 Reactive WebSocket 处理器（`src/main/resources/ws-webflux/WebFluxConfig.java:21-22`）。
- `WebSocketHandler`：基于 Flux/Mono 的收发管线（`src/main/resources/ws-webflux/WebFluxConfig.java:25-30`）。

## 使用时机
- 高并发、长连接、流式数据处理，对资源占用敏感的系统。
- 需要与响应式数据源（R2DBC、Reactive Redis）或流式处理（Kafka）衔接。
- 对背压与异步管线有明确要求。

## 细节
- 需添加 `spring-boot-starter-webflux` 依赖；与 MVC 共存需避免路由/端口冲突。
- 错误处理、取消订阅与资源清理需在流式管线中正确实现。
- 更适合服务端推送与聚合转换，编码风格不同于传统 MVC。

## 何时选择它
- 面向高并发和响应式数据流的实时系统。
- 需要在服务端以流式方式处理与转发数据。