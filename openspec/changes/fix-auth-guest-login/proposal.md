## Why

修改密码和免密登录两个功能均不可用——前者因网关白名单路径错误导致 401，后者因后端从未实现免密登录端点。用户在登录页点击这两个入口后均无法完成操作，影响核心体验。

## What Changes

- 修正网关白名单中「修改密码」端点路径：`/api/v1/auth/forgot-password` → `/api/v1/auth/password`
- 新增后端 `POST /api/v1/auth/guest-login` 端点，支持邮箱 + 验证码免密登录
- 免密登录时，若邮箱未注册则自动创建账号（默认用户名 "Vent"、默认头像 "/Note/mm.jpg"）
- 前端免密登录表单改为调用新端点

## Capabilities

### New Capabilities
- `guest-login`: 免密登录——输入邮箱和验证码即可登录，未注册用户自动创建账号

### Modified Capabilities
<!-- None -->

## Impact

| 层 | 文件 | 变更 |
|------|------|------|
| 网关 | `JwtAuthenticationFilter.java` | 修正 WHITE_LIST 路径 |
| 后端 | `AuthController.java` | 新增 guest-login 端点 |
| 后端 | `UserService.java` | 新增 guestLogin 方法 |
| 后端 | `blog-common` | 新增 GuestLoginDTO |
| 前端 | `auth.ts` | 新增 guestLogin API 函数 |
| 前端 | `LoginView.vue` | 免密登录调用新 API |
