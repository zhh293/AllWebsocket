# Jakarta WebSocket 标准端点

## 特点
- 使用容器级标准 API `@ServerEndpoint`，直接操控会话与协议原语。
- 适合自定义信令、点对点通信、需要低层控制的场景。
- 与 Spring 集成需通过 `ServerEndpointExporter` 注册端点。

## 关键文件
- 端点注册（示例版本）：`src/main/resources/ws-jakarta/WebSocketConfig.java:9-10`
- 最小端点模板：`src/main/resources/ws-jakarta/CallServer.java:13`（路径 `"/ws/jakarta/{userId}"`）
- 当前项目的完整端点：`src/main/java/com/zhh/handsome/democall/ws/CallServer.java:19`（路径 `"/demo/ws/call/{userId}"`）
- 当前项目的注册配置：`src/main/java/com/zhh/handsome/democall/config/WebSocketConfig.java:9-12`

## 重要函数
- 生命周期方法：
  - `@OnOpen` 建立连接（示例：`src/main/resources/ws-jakarta/CallServer.java:15-16`）
  - `@OnMessage` 收到消息时处理（示例：`src/main/resources/ws-jakarta/CallServer.java:18-19`）
  - `@OnClose` 连接关闭（示例：`src/main/resources/ws-jakarta/CallServer.java:21-22`）
  - `@OnError` 错误回调（示例：`src/main/resources/ws-jakarta/CallServer.java:24-25`）
- 在当前项目端点里，信令路由与交换见：`src/main/java/com/zhh/handsome/democall/ws/CallServer.java:32-71`

## 使用时机
- 需要直接使用 `Session`、手动管理路由与负载（如 WebRTC SDP/ICE）。
- 追求最小抽象与高定制化控制。
- 与非 Spring 组件或容器特性深度耦合的场景。

## 细节
- 端点类可加 `@Component` 参与 Spring 扫描（已在当前项目中使用，`src/main/java/com/zhh/handsome/democall/ws/CallServer.java:18`）。
- 若要同时使用拦截器、跨域控制，需要结合容器/Filter 或改用 Handler/STOMP 方案。
- 会话管理需自行维护（当前项目使用 `ConcurrentHashMap`，`src/main/java/com/zhh/handsome/democall/ws/CallServer.java:22-23`）。

## 何时选择它
- 自定义信令服务器、点对点通信、协议桥接。
- 明确需要 `@ServerEndpoint` 的标准行为或已有容器层扩展。