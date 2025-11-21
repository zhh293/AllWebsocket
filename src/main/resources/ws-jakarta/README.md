# Jakarta WebSocket 标准端点（教学版）

## 目录概览
- `WebSocketConfig.java`：注册 `@ServerEndpoint` 的必要配置（示例版）
- `CallServer.java`：最小 Echo 端点（文本）
- `AdvancedCallServer.java`：升级端点，支持文本、二进制分片、JSON、Pong 心跳、子协议与自定义握手
- `JsonMessage.java`：JSON 消息模型
- `JsonMessageDecoder.java` / `JsonMessageEncoder.java`：对象与 JSON 的编解码
- `CustomConfigurator.java`：握手阶段从请求中提取上下文（如 `userId`）

## 扩展内容与用途
- 多消息类型：`AdvancedCallServer` 提供 `@OnMessage` 的多重重载以处理文本、二进制、JSON、Pong 控制帧
- 分片支持：二进制重载带 `boolean last`，适合大文件或流式数据上传
- 子协议：`subprotocols = {"json"}`，让客户端通过 `Sec-WebSocket-Protocol: json` 与服务端约定语义
- 编解码器：使用 `decoders/encoders` 将 `String` 与对象互转，处理逻辑更类型安全
- 自定义握手：通过 `CustomConfigurator#modifyHandshake` 在握手阶段读取查询参数与头部，放入 `UserProperties`
- 心跳：`@OnOpen` 主动发送 `Ping`，在 `onPong` 统计活跃度与延迟

## 每步做什么
1. 客户端发起连接到 `/ws/jakarta/adv/{userId}`，可携带 `?userId=xxx&token=...`
2. `CustomConfigurator` 在握手阶段解析参数，写入 `UserProperties`
3. `@OnOpen` 读取 `EndpointConfig/UserProperties`，将 `Session` 加入在线表，并发送 `Ping`
4. 收到消息：
   - 文本：`onText` 原样回显或路由协议
   - 二进制：`onBinary` 聚合分片，最后一片处理
   - JSON：`onJson` 直接收发 `JsonMessage` 对象，无需手动解析
   - Pong：`onPong` 记录心跳
5. 错误与关闭：`onError/onClose` 清理在线表与资源

## 教学要点/常见坑
- 端点生命周期：容器通常为每连接实例化端点对象，共享状态存储在并发集合
- 发送方式：`getAsyncRemote()` 适合高并发，`getBasicRemote()` 同步可能阻塞
- 安全：握手阶段校验 Origin、Token；必要时拒绝握手
- 大消息：启用分片并在服务端聚合，控制内存与背压
- 与 Spring 集成：需要 `ServerEndpointExporter`；如需 Bean 注入，可在自定义 `Configurator` 获取 `ApplicationContext`

## 快速测试
- 文本：`new WebSocket('ws://localhost:8082/ws/jakarta/adv/1001')` 发送字符串
- JSON：设置子协议，示例（浏览器原生不易设置）：可用库或自定义客户端；或直接向 `CallServer` 走纯文本 JSON
- 二进制：使用 `WebSocket#send(new Uint8Array(...))` 发送字节数组

## 与项目现有演示的关系
- 当前项目的 WebRTC 信令演示采用 Jakarta 端点：`src/main/java/com/zhh/handsome/democall/ws/CallServer.java`
- 可将教学示例迁移到 `src/main/java` 后与现有演示并行，用于对比与练手