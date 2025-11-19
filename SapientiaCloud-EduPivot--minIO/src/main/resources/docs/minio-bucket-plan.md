# SapientiaCloud MinIO Bucket Plan

> 参考资料：  
> - MinIO 官方 `mc ilm` 语法（https://min.io/docs/minio/linux/reference/minio-mc/mc-ilm.html）  
> - 生命周期管理实践（https://www.minio.org.cn/docs/minio/container/administration/object-management/object-lifecycle-management.html）

## Bucket Matrix

| Code | Bucket | Access | Versioning / ILM | Notes |
| --- | --- | --- | --- | --- |
| `USER_AVATAR` | `sapientiacloud-user-avatar` | Private + presign | Versioning on, keep 3 | Tiny objects, overwrite heavy, refresh CDN after write |
| `COURSE_PUBLIC` | `sapientiacloud-course-public` | Public read | Versioning on, keep 10, transition -> ILM | Object Lock (Governance) to protect flagship courseware |
| `COURSE_PRIVATE` | `sapientiacloud-course-private` | Token gated | Versioning on, transition 90/180 days | SSE-KMS + detailed IAM prefixes |
| `LIVE_PLAYBACK` | `sapientiacloud-live-playback` | Token gated | Versioning on, 30/120 day transitions | Large MPU uploads, trigger transcoding |
| `AI_QA_ASSET` | `sapientiacloud-ai-qa` | Private, app-context auth | Versioning on, expire 90 days | Tag session/user, push audit stream |

## Provisioning Snippets

### Create + Enable Versioning

```bash
mc mb edu/user-avatar && mc version enable edu/user-avatar
mc ilm rule add --id avatar-vers --bucket edu/user-avatar --num-versions 3
```

### Public Read (COURSE_PUBLIC)

```bash
mc anonymous set public edu/course-public
mc ilm rule add --id course-public-tier \
  --bucket edu/course-public \
  --transition-days 45 --storage-class STANDARD_IA
mc retention set governance edu/course-public --default 7d
```

### Private Buckets with Tiering

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

### AI_QA_ASSET Expiration

```bash
mc ilm rule add --id ai-expire \
  --bucket edu/ai-qa --expiry-days 90
```

### Audit & Notification

```bash
mc event add edu/user-avatar arn:minio:sqs::audit:kafka \
  --event put,delete --prefix avatars/
mc admin trace -v --all
```

> `edu` 为预先配置的 `mc alias set edu https://minio.example.com ACCESS SECRET`。根据环境调整域名/凭证/存储级别（`STANDARD_IA`, `WARM`, `COLD` 等需事先在 MinIO Tiering 中定义）。

