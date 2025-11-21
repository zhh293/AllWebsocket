# STOMP 消息代理（教学版）

## 目录概览
- 基础版：`StompConfig.java`（应用前缀、简单代理、端点+SockJS）
- 拓展版：`AdvancedStompConfig.java`（用户目的地、通道拦截、消息转换、传输限制）
- 代理转发：`RelayStompConfig.java`（接入外部 STOMP 代理，RabbitMQ/ActiveMQ 等）
- 拦截器：`StompChannelInterceptor.java`（在 CONNECT/SUBSCRIBE/SEND 等命令上做鉴权/审计）
- 事件：`PresenceEventListener.java`（监听连接与断开，记录在线状态）
- 控制器：`StompController.java`（广播、直发、给指定用户）
- 前端：`index.html`（SockJS+STOMP 最小示例）

## 扩展内容与用途
- 简单代理与用户目的地：`AdvancedStompConfig#configureMessageBroker` 启用 `/topic`、`/queue`、`/user`，并设定 `/app` 前缀
- 端点与回退：注册原生端点与 SockJS 端点，配置心跳与跨域策略
- 通道拦截：在 `configureClientInboundChannel/OutboundChannel` 注入拦截器，处理鉴权与审计
- 消息转换：增加 `MappingJackson2MessageConverter`，让 `@MessageMapping` 支持 JSON 自动序列化
- 传输限制：`configureWebSocketTransport` 控制消息大小、缓冲区与发送时间，保护服务端
- 外部代理：`RelayStompConfig#configureMessageBroker` 使用 `enableStompBrokerRelay` 接入企业消息系统

## 每步做什么
1. 客户端通过 SockJS/STOMP 连接 `/ws` 或 `/ws-sock`；握手成功后进入会话
2. 客户端向 `/app/**` 发送消息，服务端对应 `@MessageMapping` 接收处理
3. 服务端返回到订阅目的地（如 `/topic/**`），或通过 `SimpMessagingTemplate` 转发到用户目的地 `/user/{id}/queue/**`
4. 通道拦截器在入站/出站对命令与头部进行处理（鉴权、限流、审计）
5. 事件监听记录连接/断开，维护在线状态或做资源清理

## 教学要点/常见坑
- 目的地规范：发送走 `/app`，订阅走 `/topic`/`/queue`；用户目的地必须订阅 `/user/queue/...`
- SockJS 与跨域：开发可用通配，生产务必收敛到可信域
- 外部代理：接入后由代理负责主题路由与持久化，注意账号与 vhost 配置
- 转换器顺序：`configureMessageConverters` 返回 `false` 以保留默认转换器；返回 `true` 时仅使用你提供的列表
- 顺序与限速：`setPreservePublishOrder` 保序，对消息风暴场景要配合限流策略

## 快速测试
- 载入前端：`src/main/resources/ws-stomp/index.html`（通过服务访问或本地打开）
- 连接：`SockJS('/ws')` + `Stomp.over(sock)`
- 订阅：`client.subscribe('/topic/messages', cb)`
- 发送：`client.send('/app/chat', {}, 'hello')`，在控制台看到广播消息

## 选择建议
- 需要广播/房间/私信与回退兼容：优先 STOMP
- 只做轻量点对点或少量通知：考虑 Handler 或 Jakarta
- 需要与消息系统融合扩展：使用 `enableStompBrokerRelay`