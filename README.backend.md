# 后端开发说明

## 技术栈

后端整体基于以下组合构建：

- Java 17
- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- Spring Cloud Alibaba 2023.0.1.0
- Spring Security + JWT
- OpenFeign + Gateway + Sentinel
- MyBatis-Plus + JPA + PageHelper
- MySQL + MongoDB + Redis
- MinIO
- Kafka
- Knife4j / OpenAPI 3
- Zipkin + ELK
- Spring AI Alibaba DashScope
- LiveKit + coturn

## 微服务说明

| 模块 | 端口 | 主要职责 | 主要依赖 |
| --- | --- | --- | --- |
| `SapientiaCloud-EduPivot--gateway` | 31600 | 路由转发、跨域、文档聚合、流控治理 | Nacos、Redis、Sentinel |
| `SapientiaCloud-EduPivot--system` | 31601 | 用户、角色、权限、通知、仪表盘 | MySQL、Redis |
| `SapientiaCloud-EduPivot--auth` | 31602 | 登录注册、JWT、手机号验证码、身份选择、OAuth2 | MySQL、Redis |
| `SapientiaCloud-EduPivot--minIO` | 31603 | 文件上传下载、桶管理、文件信息 | MinIO |
| `SapientiaCloud-EduPivot--teacher` | 31604 | 教师信息域管理 | MySQL |
| `SapientiaCloud-EduPivot--student` | 31605 | 学生信息、课堂练习提交 | MySQL |
| `SapientiaCloud-EduPivot--course` | 31606 | 课程、章节、论坛、题库、任务 | MySQL、MongoDB |
| `SapientiaCloud-EduPivot--classroom` | 31607 | 课堂记录、课堂练习、学生座位、WebSocket 同步 | MySQL、MongoDB、Redis |
| `SapientiaCloud-EduPivot--celestial-hub` | 31608 | AI 对话、知识库、文档向量化、AI 出题 | MongoDB、Redis、Kafka、DashScope |
| `SapientiaCloud-EduPivot--live` | 31609 | 直播房间、LiveKit token、SSE 事件、直播消息 | MySQL、MongoDB、Redis、Kafka、LiveKit |

## 网关路由

当前 Nacos 中的网关路由约定如下：

| 路径前缀 | 目标服务 |
| --- | --- |
| `/api/system/**` | `SapientiaCloud-EduPivot--system` |
| `/api/auth/**` | `SapientiaCloud-EduPivot--auth` |
| `/api/minIO/**` | `SapientiaCloud-EduPivot--minIO` |
| `/api/teacher/**` | `SapientiaCloud-EduPivot--teacher` |
| `/api/student/**` | `SapientiaCloud-EduPivot--student` |
| `/api/course/**` | `SapientiaCloud-EduPivot--course` |
| `/api/classroom/**` | `SapientiaCloud-EduPivot--classroom` |
| `/api/classroom/ws/**` | `SapientiaCloud-EduPivot--classroom` WebSocket 通道 |
| `/api/live/**` | `SapientiaCloud-EduPivot--live` |
| `/api/celestial-hub/**` | `SapientiaCloud-EduPivot--celestial-hub` |
| `/api/celestial-hub/live/**` | `SapientiaCloud-EduPivot--live` 兼容别名 |

## 中间件与端口

`docker-compose.yaml` 默认暴露以下端口：

| 组件 | 端口 | 说明 |
| --- | --- | --- |
| MySQL | 3306 | 主关系型数据库 |
| Redis | 6379 | 缓存与短期 token |
| Redis Stack UI | 8001 | Redis 可视化 |
| Nacos | 8848 / 9848 | 注册中心 / 配置中心 |
| Sentinel Dashboard | 8858 | 限流治理控制台 |
| MinIO | 31589 / 31590 | 对象存储 API / 控制台 |
| MongoDB | 27017 | 文档数据库 |
| Zipkin | 9411 | 链路追踪 |
| Kafka | 9092 / 29092 | 消息队列 |
| Elasticsearch | 9200 / 9300 | 日志检索 |
| Kibana | 5601 | 日志看板 |
| Logstash | 9600 | 日志采集 |
| coturn | 3478 | TURN/STUN |
| LiveKit | 7880 / 7881 | 实时音视频 |
| egress | 8080 | 直播录制相关服务 |

## 配置组织方式

### 本地文件

每个服务本地 `application.yaml` 只保留最小启动信息：

- 服务端口
- `spring.application.name`
- 从 Nacos 拉取配置的 `import`
- Nacos 地址

### 配置中心

真正的共享配置和服务配置在 `init/nacos-yaml/`：

- `application.yaml`：共享配置，例如数据源、Redis、Knife4j、Zipkin、Sentinel
- `SapientiaCloud-EduPivot--*.yaml`：各服务私有配置

建议把这两类配置分别理解为：

- 平台级公共配置
- 业务服务级配置

## 本地启动步骤

### 1. 启动基础设施

```bash
docker compose up -d
```

### 2. 初始化数据库

至少完成以下导入：

- `init/mysql/nacos.sql`
- `init/mysql/sapientiacloud_edupivot.sql`

如果你希望按模块逐步搭库，也可以补充执行：

- `init/mysql/table/system.sql`
- `init/mysql/table/teacher.sql`
- `init/mysql/table/student.sql`
- `init/mysql/table/course.sql`
- `init/mysql/table/classroom.sql`

Mongo 初始化脚本参考：

- `init/mongodb/init.js`
- `init/mongodb/sapientiacloud_edupivot.js`
- `init/mongodb/new/*.js`

### 3. 导入 Nacos Data ID

需要导入以下配置文件：

```text
application.yaml
SapientiaCloud-EduPivot--auth.yaml
SapientiaCloud-EduPivot--system.yaml
SapientiaCloud-EduPivot--gateway.yaml
SapientiaCloud-EduPivot--minIO.yaml
SapientiaCloud-EduPivot--teacher.yaml
SapientiaCloud-EduPivot--student.yaml
SapientiaCloud-EduPivot--course.yaml
SapientiaCloud-EduPivot--classroom.yaml
SapientiaCloud-EduPivot--celestial-hub.yaml
SapientiaCloud-EduPivot--live.yaml
```

### 4. 编译项目

```bash
mvn -Dmaven.repo.local=./.m2/repository -DskipTests compile
```

### 5. 启动服务

可在 IDE 中分别运行各模块的 `*Application.java`，也可以用 Maven 单独启动模块。

应用入口类如下：

- `SapientiaCloudEduPivotAuthApplication`
- `SapientiaCloudEduPivotSystemApplication`
- `SapientiaCloudEduPivotGatewayApplication`
- `SapientiaCloudEduPivotMinioApplication`
- `SapientiaCloudEduPivotTeacherApplication`
- `SapientiaCloudEduPivotStudentApplication`
- `SapientiaCloudEduPivotCourseApplication`
- `SapientiaCloudEdPivotClassroomApplication`
- `SapientiaCloudEdPivotCelestialHubApplication`
- `SapientiaCloudEduPivotLiveApplication`

推荐启动顺序：

1. `auth`
2. `system`
3. `teacher`
4. `student`
5. `course`
6. `classroom`
7. `minIO`
8. `celestial-hub`
9. `live`
10. `gateway`

## 关键业务能力

### 认证与身份

`auth` 服务当前已覆盖：

- 用户名密码登录
- 手机号验证码登录
- JWT 校验与注销
- 用户注册
- 手机号绑定与密码重置
- 身份选择
- GitHub OAuth2 登录

### 课程与课堂

`course` 与 `classroom` 组合承载：

- 课程、章节、论坛、题库、任务
- 教学记录与课堂练习
- 学生座位编排与实时同步

课堂同步接口的关键路径：

- `POST /seat-sync/ws-token`
- `GET /ws/seat`

### AI 助教

`celestial-hub` 目前提供：

- AI 对话会话与消息管理
- 文件上传与文档向量化
- 知识检索
- AI 自动出题

### 直播教学

`live` 服务负责：

- 直播房间创建、开始、结束
- LiveKit 房间 token 签发
- SSE 事件订阅
- 直播间消息与成员心跳
- 直播录制文件落桶

## 接口文档与监控

推荐优先通过网关访问：

- `http://localhost:31600/doc.html`

其他常用控制台：

- `http://localhost:8848/nacos`
- `http://localhost:8858`
- `http://localhost:31590`
- `http://localhost:9411`
- `http://localhost:5601`

## 开发注意事项

- 配置中心中存在示例密码、API Key 与公网 IP，仅适合本地演示，不应直接用于生产。
- `live` 和 `celestial-hub` 的配置对外部服务依赖更强，迁移环境时优先检查：
  - LiveKit 地址
  - TURN 地址
  - MinIO 回放桶
  - DashScope API Key
  - Kafka 地址
- `gateway` 文档聚合依赖服务注册状态，某个服务未注册时不会出现在聚合文档中。
- 如果服务启动时报 Nacos 配置缺失，优先检查 `spring.application.name` 对应的 Data ID 是否已经导入。
