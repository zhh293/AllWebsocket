# WebFlux 响应式 WebSocket（教学版）

## 目录概览
- 基础版：`WebFluxConfig.java`（单一路由 `/ws/rx/echo` 的最小 Echo）
- 拓展版：`AdvancedWebFluxConfig.java`（多路由与广播、握手信息等）
- 说明：`README.md`（本文件）

## 扩展内容与用途
- 多路由：一次性注册 Echo/JSON/Upper/Broadcast/Info 五个处理器，便于演示不同管线
- 广播：基于 `Sinks.many().multicast().directBestEffort()` 实现简易广播总线，所有连接共享一个流
- JSON 处理：使用 `ObjectMapper` 在管线中解析与规范化回发
- 握手信息：在 `info` 处理器中返回 `sessionId/uri/User-Agent`
- 响应式编排：`Mono.when(send, recv)` 组合发送与接收；`Flux.map/doOnNext` 进行转换与副作用

## 每步做什么
1. `rxHandlerMapping` 注册五条路由到处理器：`/ws/rx/echo|json|upper|broadcast|info`
2. 客户端连接不同路径发送消息
3. Echo/Upper：将输入映射为输出并发送；JSON：解析后规范化回发
4. Broadcast：将客户端消息 `tryEmitNext` 到广播总线，所有连接的 `send` 订阅该总线并推送
5. Info：连接后立即向客户端发送会话与请求信息

## 教学要点/常见坑
- 背压与资源：广播场景下注意慢客户端导致的堆积；必要时使用背压策略与限流
- 错误处理：管线中捕获异常并回默认值或关闭连接；示例中 JSON 解析失败回 `{}`
- 关闭与取消：保证 `session.send` 与 `receive.then()` 正确结束，避免泄漏
- 与 MVC 共存：避免与 MVC 的路由冲突；建议将 WebFlux WebSocket 路径置于专用前缀 `/ws/rx/*`

## 快速测试
- Echo：`ws://localhost:8082/ws/rx/echo` 发送文本原样返回
- JSON：`ws://localhost:8082/ws/rx/json` 发送 `{"a":1}` 返回规范化 JSON
- Upper：`ws://localhost:8082/ws/rx/upper` 文本转大写
- Broadcast：`ws://localhost:8082/ws/rx/broadcast` 多客户端同时连接并互相广播
- Info：`ws://localhost:8082/ws/rx/info` 连接后收到握手信息

## 选择建议
- 需要响应式与高并发：优先 WebFlux
- 业务较简单或希望 MVC 风格：考虑 Handler
- 需要消息语义（房间/订阅）：考虑 STOMP