# 前端联调说明

## 说明

当前仓库不包含前端源码，这份文档用于前端同学或独立前端仓库进行联调时快速对齐接口入口、实时通信方式与环境变量约定。

如果你准备新建前端项目，建议将它视作一个独立仓库，通过网关与本仓库中的微服务通信。

## 建议技术栈

推荐沿用以下组合：

- Vue 3
- Vite
- TypeScript
- Pinia
- Vue Router
- Axios
- Naive UI 或其他成熟组件库

这不是强制限制，但能和当前接口设计、实时通信形态以及旧文档中的技术方向保持一致。

## 统一接入原则

- 所有 REST 请求统一走网关：`http://localhost:31600`
- 所有业务接口都以 `/api/` 开头
- 不建议直接访问 `31601` 到 `31609` 这些内部服务端口
- 本地开发可以直接从前端开发服务器跨域访问网关，`gateway` 已配置全局 CORS

## 推荐环境变量

```env
VITE_API_BASE_URL=http://localhost:31600
VITE_MINIO_BASE_URL=http://localhost:31589
VITE_LIVEKIT_URL=ws://localhost:7880
VITE_APP_NAME=SapientiaCloud-EduPivot
```

如果前端部署到 HTTPS 域名，`VITE_LIVEKIT_URL` 应改成对应的 `wss://` 地址。

## 页面与业务域建议

| 业务域 | 推荐页面/模块 | 主要接口前缀 |
| --- | --- | --- |
| 认证中心 | 登录、注册、找回密码、身份选择、OAuth 回调 | `/api/auth/**` |
| 系统管理 | 用户、角色、权限、通知、仪表盘 | `/api/system/**` |
| 教师中心 | 教师资料、授课信息 | `/api/teacher/**` |
| 学生中心 | 学生资料、课堂练习提交 | `/api/student/**` |
| 课程中心 | 课程列表、章节、论坛、任务、题库 | `/api/course/**` |
| 课堂中心 | 教学记录、课堂练习、座位同步 | `/api/classroom/**` |
| 文件中心 | 上传、下载、预览、文件详情 | `/api/minIO/**` |
| AI 助教 | 对话、文档上传、知识检索、出题 | `/api/celestial-hub/**` |
| 直播中心 | 直播房间、直播消息、成员心跳、事件订阅 | `/api/live/**` |

## 登录与鉴权

- 登录、注册、手机号验证码、身份选择等能力由 `auth` 服务提供。
- 登录后按后端返回结果存储 JWT，并在后续请求中携带：

```http
Authorization: Bearer <token>
```

- 接口权限由后端控制，前端路由权限建议根据用户角色和权限码再做一层展示控制。

## 实时通信接入

### 课堂座位同步 WebSocket

用于 3D 教室或课堂座位实时同步。

1. 先申请一次性 WebSocket Token：

```http
POST /api/classroom/seat-sync/ws-token?recordId={recordId}
```

2. 拿到 token 后建立连接：

```text
ws://localhost:31600/api/classroom/ws/seat?recordId={recordId}&token={token}
```

约定说明：

- token 默认是短时、一次性凭证
- `recordId` 必须与申请 token 时使用的记录 ID 保持一致
- 前端应在连接失败时重新申请 token，而不是重复复用旧 token

### 直播事件 SSE

用于接收直播房间成员数、状态变化等实时事件。

1. 申请 SSE token：

```http
POST /api/live/live-room/sse-token?classroomId={classroomId}
```

2. 建立订阅：

```text
GET /api/live/subscribe?classroomId={classroomId}&token={token}
```

说明：

- SSE token 同样是短时凭证
- 直播页刷新后建议重新签发 token
- 订阅地址应始终走网关，而不是直接访问 `live` 服务

### LiveKit 音视频

直播房间的音视频能力依赖 LiveKit。

前端接入流程建议如下：

1. 创建或获取直播房间信息
2. 调用房间令牌接口获取访问 token
3. 使用 `VITE_LIVEKIT_URL` 连接 LiveKit Server
4. 将后端返回的 `roomName` 与 token 一并交给 LiveKit SDK

常见接口：

- `POST /api/live/live-room/add`
- `POST /api/live/live-room/token/{roomId}`
- `GET /api/live/live-room/active`
- `POST /api/live/live-room/heartbeat`
- `POST /api/live/live-room/leave`

## 文件与资源接入

- 普通业务文件统一通过 `minIO` 服务上传下载
- AI 文档上传通过 `celestial-hub` 的文件接口处理，再触发向量化
- 直播录制文件默认落到 MinIO 的 `sapientiacloud-live-playback` 桶

如果前端需要直接展示公开资源，建议把“公开桶”和“受保护桶”分开处理：

- 公开资源可直接使用 URL
- 私有资源优先通过业务接口换取可访问地址，不要自行拼接路径

## 联调建议

- 所有 UUID 在前端统一按字符串处理，不要转成数字。
- 实时能力页面要明确区分三类连接：REST、WebSocket、SSE。
- 直播页除 token 外，还需要维护心跳，否则后端会回收成员会话。
- 若通过网关访问 `doc.html`，可直接查看聚合后的 OpenAPI 文档。

## 排查清单

- 网关可访问，但业务接口 404：先检查 Nacos 中是否导入了对应服务配置。
- WebSocket 握手失败：确认先申请了 `ws-token`，且 `recordId` 与 token 一致。
- SSE 401：通常是 token 过期或已被消费，需要重新申请。
- 直播能进房但没声音/画面：优先检查 `VITE_LIVEKIT_URL`、coturn、LiveKit 端口映射。
