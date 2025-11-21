# STOMP 消息代理（支持 SockJS）

## 特点
- 在 WebSocket 上层引入 STOMP 协议语义（`subscribe`、`send`、`destination`）。
- 适合聊天室、广播、订阅-发布模型；客户端可用 SockJS 提供回退与重连体验。
- 服务端通过 `SimpMessagingTemplate` 与简单代理快速开发。

## 关键文件
- 启用代理与前缀：`src/main/resources/ws-stomp/StompConfig.java:12-16`
- 注册端点（含 SockJS）：`src/main/resources/ws-stomp/StompConfig.java:17-20`
- 控制器映射：`src/main/resources/ws-stomp/ChatController.java:9-11`（`/app/chat` → 广播到 `/topic/messages`）
- 前端示例：`src/main/resources/ws-stomp/index.html:8-13`（订阅 `/topic/messages` 并发送到 `/app/chat`）

## 重要函数
- `configureMessageBroker(...)`：配置应用前缀与简单代理（`src/main/resources/ws-stomp/StompConfig.java:12-16`）。
- `registerStompEndpoints(...)`：注册 STOMP 端点与 SockJS 回退（`src/main/resources/ws-stomp/StompConfig.java:17-20`）。
- `@MessageMapping` + `@SendTo`：消息路由与广播（`src/main/resources/ws-stomp/ChatController.java:9-11`）。

## 使用时机
- 群聊、广播通知、订阅-发布模型、房间/主题管理等。
- 希望更高层语义与开箱即用的消息分发。
- 需要跨浏览器兼容与更好的断线重连体验。

## 细节
- 客户端目的地规范：应用前缀默认 `/app`，订阅前缀示例 `/topic`。
- 可替换内置简单代理为外部消息代理以支持更复杂拓扑。
- 认证与权限可通过 `ChannelInterceptor` 实现上下文传播与校验。

## 何时选择它
- 优先构建消息型实时系统，聚焦业务而非协议细节。
- 需要广播/订阅能力、房间管理与良好的客户端兼容性。