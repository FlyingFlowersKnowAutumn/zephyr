## Why

个人中心页面（ProfileView.vue）已具备编辑性别、自我介绍的功能，但头像仅支持手动输入外部 URL，无法从本地提交图片文件。之前的 `add-profile-about` 设计明确将头像上传划为 Non-Goal，现在需要补齐这个缺口，完成个人信息修改的最后一环。

## What Changes

- 新增 `AvatarController`：独立的头像上传控制器，{userId} 权限校验
- 新增 `FileUploadService`：文件校验、存储、URL 生成，支持扩展名白名单和大小限制
- 头像上传 API：`POST /api/v1/users/{userId}/avatar`，上传后自动保存 avatarUrl 到用户 profile
- 文件存储于 user-service 本地磁盘，按日期分目录：`uploads/avatars/{yyyy/MM/dd}/{uuid}.ext`
- 前端头像区域改为点击弹出 Modal，Modal 内支持拖拽上传和点击选择本地文件
- 前端选择文件后客户端预校验（类型+大小），通过后调用上传 API，成功后自动回写头像预览

## Capabilities

### New Capabilities

- `avatar-upload`: 用户点击头像弹出上传弹窗，支持拖拽/点击两种方式提交本地图片，后端存储并自动关联到用户资料

### Modified Capabilities

（无现有 capability 需要修改）

## Impact

| 组件 | 文件 | 影响 |
|------|------|------|
| 后端 Controller | `AvatarController.java`（新增） | 头像上传端点 |
| 后端 Service | `FileUploadService.java`（新增） | 文件校验与存储逻辑 |
| 后端配置 | `user-service application.yml` | `file.upload.path` 和 `file.upload.base-url` |
| 前端页面 | `ProfileView.vue` | 新增 Modal 弹窗、拖拽区域、上传逻辑 |
