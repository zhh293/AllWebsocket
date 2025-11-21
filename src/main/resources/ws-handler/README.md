# Spring WebSocket Handler（教学版）

## 目录概览
- `WsConfig.java`：集中注册所有 WebSocket 路由与可配置项（跨域、拦截器、握手、自定义 Principal、SockJS、装饰器）
- 处理器实现：
  - `EchoHandler.java`：文本 Echo
  - `JsonHandler.java`：解析并回发 JSON
  - `BinaryHandler.java`：二进制 Echo
  - `PartialBinaryHandler.java`：启用分片的二进制 Echo
  - `LifecycleHandler.java`：演示连接建立/错误/关闭等生命周期回调
  - `PongHandler.java`：处理客户端 Pong 控制帧
- `index.html`：最小前端连接并发送示例

## 扩展内容与用途
- 路由注册：在一个类中统一声明多条路径，便于教学与对比（`/ws/echo`、`/ws/json`、`/ws/bin` 等）
- 跨域控制：`setAllowedOrigins`/`setAllowedOriginPatterns`，开发与生产差异化配置
- 握手拦截器：在握手阶段解析 `userId` 等参数，写入 `attributes` 用于后续业务
- 自定义 `HandshakeHandler`：决定 `Principal` 身份，用于按用户维度路由
- SockJS 回退：兼容旧浏览器或受限网络，配置心跳/缓存/流大小/客户端库 URL
- 处理器装饰器：统一注入日志、统计、限流等横切逻辑

## 每步做什么
1. `WsConfig#registerWebSocketHandlers` 注册处理器到指定路径；配置跨域、拦截器、握手、SockJS
2. 客户端连接不同路径进行功能演示（Echo/JSON/二进制/分片/生命周期/Pong）
3. 握手阶段将用户标识写入 `attributes`，`HandshakeHandler` 决定 `Principal`
4. 在处理器内部按类型化方法处理消息（文本/二进制/控制帧），并根据需要回发或路由

## 教学要点/常见坑
- 分片消息：在需要处理大消息时重写 `supportsPartialMessages()` 并在服务端汇聚
- 身份绑定：在握手阶段绑定用户上下文后，处理器中不要再做重复鉴权
- 资源清理：在 `LifecycleHandler` 的关闭/错误回调中释放资源
- 跨域策略：开发环境可以 `*`，生产必须收敛到可信域名

## 快速测试
- 文本：`ws://localhost:8082/ws/echo` 发送任意文本原样返回
- JSON：`ws://localhost:8082/ws/json` 发送 JSON 文本，规范化回发
- 二进制：`ws://localhost:8082/ws/bin` 发送 `Uint8Array`
- 分片：`ws://localhost:8082/ws/bin-partial` 发送大消息分片
- 生命周期：`ws://localhost:8082/ws/lifecycle` 观察连接/关闭回调
- Pong：`ws://localhost:8082/ws/pong` 由客户端触发 Pong 控制帧