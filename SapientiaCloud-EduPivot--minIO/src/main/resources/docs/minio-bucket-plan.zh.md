# SapientiaCloud MinIO 存储桶规划（中文版）

> 参考资料：  
> - MinIO 官方 `mc ilm` 语法（https://min.io/docs/minio/linux/reference/minio-mc/mc-ilm.html）  
> - 生命周期管理实践（https://www.minio.org.cn/docs/minio/container/administration/object-management/object-lifecycle-management.html）

## 存储桶矩阵

| 业务编码 | 桶名称 | 访问策略 | 版本控制 / 生命周期 | 备注 |
| --- | --- | --- | --- | --- |
| `USER_AVATAR` | `sapientiacloud-user-avatar` | 私有 + 临时签名 URL | 开启版本控制，保留 3 个版本 | 小文件多覆盖，写入后刷新 CDN |
| `COURSE_PUBLIC` | `sapientiacloud-course-public` | 公共读 | 开启版本控制，保留 10 个版本，45 天转低频 | 启用治理模式对象锁，保护精品课件 |
| `COURSE_PRIVATE` | `sapientiacloud-course-private` | Token 授权 | 开启版本控制，90/180 天转储 | 建议启用 SSE-KMS，细分 IAM 前缀权限 |
| `LIVE_PLAYBACK` | `sapientiacloud-live-playback` | Token 授权 | 开启版本控制，30/120 天转储 | 大文件 MPU，写入后触发转码流程 |
| `AI_QA_ASSET` | `sapientiacloud-ai-qa` | 应用上下文私有 | 开启版本控制，90 天过期 | 打标签记录会话/用户，推送审计流 |

## 部署命令示例

### 创建桶并启用版本控制

```bash
mc mb edu/user-avatar && mc version enable edu/user-avatar
mc ilm rule add --id avatar-vers --bucket edu/user-avatar --num-versions 3
```

### 公共读桶（COURSE_PUBLIC）

```bash
mc anonymous set public edu/course-public
mc ilm rule add --id course-public-tier \
  --bucket edu/course-public \
  --transition-days 45 --storage-class STANDARD_IA
mc retention set governance edu/course-public --default 7d
```

### 私有桶分层（COURSE_PRIVATE / LIVE_PLAYBACK）

```bash
# COURSE_PRIVATE
mc version enable edu/course-private
mc ilm rule add --id cp-cold --bucket edu/course-private \
  --transition-days 90 --storage-class WARM
mc ilm rule add --id cp-archive --bucket edu/course-private \
  --transition-days 180 --storage-class COLD

# LIVE playback
mc ilm rule add --id live-cold --bucket edu/live-playback \
  --transition-days 30 --storage-class WARM
mc ilm rule add --id live-archive --bucket edu/live-playback \
  --transition-days 120 --storage-class COLD
```

### AI_QA_ASSET 自动过期

```bash
mc ilm rule add --id ai-expire \
  --bucket edu/ai-qa --expiry-days 90
```

### 审计与通知

```bash
mc event add edu/user-avatar arn:minio:sqs::audit:kafka \
  --event put,delete --prefix avatars/
mc admin trace -v --all
```

> `edu` 为预配置的 `mc alias set edu https://minio.example.com ACCESS SECRET`。根据实际环境调整域名、凭证及存储级别（例如 `STANDARD_IA`、`WARM`、`COLD` 需要在 MinIO 分层策略中预先定义）。


