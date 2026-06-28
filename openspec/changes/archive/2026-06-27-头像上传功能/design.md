## Context

当前 `UserProfileService.updateProfile()` 已处理 displayName、bio、avatarUrl、gender 字段的写入。前端 ProfileView.vue 已有完整的表单 UI，但头像仅支持手动填写 URL 字符串。之前的 `add-profile-about` 设计将头像上传划为 Non-Goal。现在需要补齐本地文件上传能力。

架构约束：微服务 + Gateway 单入口，user-service 负责用户数据。项目无 Nginx、无 CDN、无 OSS 依赖。

## Goals / Non-Goals

**Goals:**
- 用户点击头像弹出上传弹窗，支持拖拽和点击两种方式选择本地图片
- 前端客户端预校验文件类型和大小，通过后调用后端上传
- 上传成功后自动将 avatarUrl 写入用户 profile（一次请求完成）
- 头像图片通过 URL 公开访问，无需登录

**Non-Goals:**
- 不引入 OSS / MinIO / S3 等外部存储
- 不支持图片裁剪（可后续迭代）
- 不修改性别和自我介绍已有的实现

## Decisions

### 1. 存储方案：user-service 本地磁盘 + 日期分目录

**选择**: 文件存储在 `uploads/avatars/{yyyy/MM/dd}/{uuid}.ext`，路径可通过 `file.upload.path` 配置，返回的 URL 前缀通过 `file.upload.base-url` 配置。

**理由**: 日期分目录避免单目录文件过多；配置化路径便于不同环境切换；UUID 文件名防冲突。

### 2. 独立 Controller + Service

**选择**: 新建 `AvatarController` 和 `FileUploadService`，而非在 `UserProfileController` 中追加方法。

**理由**: 单一职责——`UserProfileController` 负责资料 CRUD，`AvatarController` 负责文件上传。`FileUploadService` 可复用于后续其他文件上传场景。

### 3. 上传即保存

**选择**: `POST /api/v1/users/{userId}/avatar` 上传文件后立即调用 `userProfileService.updateProfile()` 将 avatarUrl 写入数据库，一次请求完成。

**理由**: 简化前端交互——无需先上传再点保存两步操作。上传成功即头像生效。

### 4. 弹窗式 UI

**选择**: 点击头像弹出 Modal 弹窗，弹窗内包含虚线拖拽区域（支持拖拽 + 点击选择），选择文件后显示"上传"按钮。

**理由**: 符合主流产品交互习惯；弹窗聚焦操作，不影响表单其他字段；拖拽区域对桌面用户更友好。

### 5. 前后端双重校验

**选择**: 前端在选择文件时客户端校验类型和大小（`validateAndSelectFile`），后端 `FileUploadService.validateFile()` 服务端再次校验。

**理由**: 前端校验提供即时反馈（不消耗网络请求），后端校验保证安全性（不可绕过）。双重校验是文件上传的标准实践。

### 6. 文件命名：`{uuid}.{ext}`

使用 `UUID.randomUUID()` + 原始扩展名。不包含 userId 以避免信息泄露（头像 URL 可公开访问，不应暴露用户 ID）。

### 7. 旧头像清理

**选择**: 上传成功后自动删除旧头像文件。`AvatarController` 在更新 profile 前获取旧 avatarUrl，新文件写入成功后调用 `FileUploadService.deleteOldAvatar()` 清理。

**理由**: 避免磁盘堆积无用文件。容错策略：URL 为 null 或外部链接时跳过，删除失败仅 log warn 不影响主流程。

## Risks / Trade-offs

- **头像 URL 路由问题**: avatarUrl 返回路径格式为 `/uploads/avatars/...`，不在 `/api/v1/**` 下。需要在 gateway 添加 `/uploads/**` 路由，或通过其他方式 serve 静态文件
- **容器化部署**: 若后续 Docker 化，`uploads/` 目录需要通过 volume 持久化
