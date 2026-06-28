## 1. 后端 — 文件上传服务

- [x] 1.1 新建 `FileUploadService` — `uploadAvatar(MultipartFile)` 校验类型/大小，按日期分目录存储，返回 URL；`deleteOldAvatar(String)` 将 URL 还原为文件路径并删除
- [x] 1.2 新建 `AvatarController` — `POST /api/v1/users/{userId}/avatar`，鉴权后获取旧 avatarUrl，上传成功后写入新 URL 到 profile，删除旧文件
- [x] 1.3 `application.yml` 配置 `file.upload.path`（默认 `uploads`）和 `file.upload.base-url`（默认 `/uploads`）
- [x] 1.4 新建 `WebMvcConfig` — 映射 `/uploads/**` 到本地 `uploads/` 目录，使头像文件可通过 HTTP 访问

## 2. 后端 — 安全白名单

- [x] 2.1 `JwtAuthenticationFilter` 白名单新增 `GET /uploads/**`，确保头像图片无需登录即可访问

## 3. 前端 — ProfileView Modal 弹窗

- [x] 3.1 新增状态变量：`avatarModalVisible`、`fileInputRef`、`selectedFile`、`uploading`
- [x] 3.2 新增方法：`openAvatarModal`、`closeAvatarModal`、`triggerFileInput`、`handleFileSelect`、`handleDrop`、`validateAndSelectFile`、`uploadAvatar`
- [x] 3.3 模板新增 Modal 结构：遮罩层 + 弹窗（标题栏 + 拖拽上传区域 + 隐藏 file input + 底部上传按钮）
- [x] 3.4 样式新增：`.avatar-modal-overlay`、`.avatar-modal`、`.avatar-modal-header`、`.avatar-modal-body`、`.upload-area`、`.avatar-modal-footer`、`.upload-btn`

## 4. 验证

- [ ] 4.1 点击头像弹出 Modal，拖拽图片文件可正常读取
- [ ] 4.2 点击上传区域打开本地文件选择器
- [ ] 4.3 选择非图片/超 2MB 文件时 alert 提示，不发起网络请求
- [ ] 4.4 选择有效文件点击上传，成功后弹窗关闭、头像预览更新
- [ ] 4.5 确认 Sidebar 和 Header 中的头像同步更新
- [ ] 4.6 确认头像 URL 无需登录即可访问
