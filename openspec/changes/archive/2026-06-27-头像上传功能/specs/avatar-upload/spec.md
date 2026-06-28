## ADDED Requirements

### Requirement: 前端头像上传弹窗交互
系统前端 SHALL 提供点击头像弹出 Modal 的交互，Modal 内支持拖拽上传和点击选择本地图片，选择后客户端预校验文件类型和大小。

#### Scenario: 点击头像弹出上传弹窗
- **WHEN** 用户在个人信息 Tab 下点击头像区域
- **THEN** 系统弹出 Modal 弹窗，标题为"修改头像"，包含上传区域和取消按钮

#### Scenario: 拖拽文件到上传区域
- **WHEN** 用户将本地图片文件拖拽到 Modal 内的虚线区域
- **THEN** 系统拦截 drop 事件，读取文件并进行客户端校验

#### Scenario: 点击上传区域选择文件
- **WHEN** 用户点击 Modal 内的虚线上传区域
- **THEN** 系统打开本地文件选择对话框，仅允许 jpg/jpeg/png/gif/webp 图片

#### Scenario: 客户端校验不通过
- **WHEN** 用户选择非图片格式或超过 2MB 的文件
- **THEN** 系统弹出 alert 提示，不发起上传请求

#### Scenario: 选择文件后点击上传
- **WHEN** 用户选择了有效文件并点击"上传"按钮
- **THEN** 系统调用 `POST /api/v1/users/{userId}/avatar`，上传成功后关闭弹窗，头像预览更新为新图片

### Requirement: 后端头像上传 API
系统 SHALL 提供 `POST /api/v1/users/{userId}/avatar` 端点，接收图片文件，校验后存储到本地磁盘，并自动更新用户 avatarUrl。

#### Scenario: 成功上传头像
- **WHEN** 已登录用户上传符合要求的图片文件，且 userId 与 token 身份一致
- **THEN** 系统保存文件到 `uploads/avatars/{yyyy/MM/dd}/{uuid}.ext`，调用 `updateProfile` 写入 avatarUrl，删除旧头像文件，返回 `{ avatarUrl: "/uploads/avatars/..." }`, code=0

#### Scenario: 上传非图片文件
- **WHEN** 用户上传文件扩展名不在 jpg/jpeg/png/gif/webp 范围内
- **THEN** 系统返回 code=400，message="只支持 jpg、jpeg、png、gif、webp 格式的图片"

#### Scenario: 上传超过 2MB 的文件
- **WHEN** 用户上传超过 2MB 的文件
- **THEN** 系统返回 code=400，message="图片大小不能超过 2MB"

#### Scenario: 越权上传
- **WHEN** 用户尝试向其他用户的 userId 上传头像
- **THEN** 系统返回 code=403，message="无权操作"

#### Scenario: 未登录上传
- **WHEN** 未携带有效 token 请求上传
- **THEN** 系统抛出 RuntimeException，返回 401

#### Scenario: 上传后自动清理旧头像
- **WHEN** 用户已有通过本地上传的头像，再次上传新头像
- **THEN** 系统在上传成功后删除旧头像文件；若旧头像为外部 URL 则跳过删除；删除失败不影响上传结果

### Requirement: 头像图片公开访问
系统 SHALL 允许任何人通过返回的 URL 路径访问已上传的头像图片，无需登录认证。

#### Scenario: 公开访问头像文件
- **WHEN** 客户端请求 `GET /uploads/avatars/{yyyy/MM/dd}/{uuid}.ext`
- **THEN** 系统返回对应的图片文件流

#### Scenario: 访问不存在的头像文件
- **WHEN** 客户端请求不存在的文件路径
- **THEN** 系统返回 404
