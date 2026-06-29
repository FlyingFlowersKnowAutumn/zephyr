## 1. 网关白名单修正

- [x] 1.1 将 `JwtAuthenticationFilter.java` WHITE_LIST 中的 `/api/v1/auth/forgot-password` 改为 `/api/v1/auth/password`

## 2. 后端——GuestLoginDTO

- [x] 2.1 在 `blog-common` 中创建 `GuestLoginDTO`，包含 `email` 和 `code` 字段（带校验注解）

## 3. 后端——免密登录

- [x] 3.1 在 `UserService.java` 中实现 `guestLogin(GuestLoginDTO dto)` 方法：校验验证码 → 查邮箱 → 已注册则返回用户，未注册则自动创建用户后返回
- [x] 3.2 在 `AuthController.java` 中新增 `@PostMapping("/guest-login")` 端点，调用 `guestLogin`，返回 `LoginResponseDTO`

## 4. 前端——API 层

- [x] 4.1 在 `auth.ts` 中新增 `guestLogin(dto: GuestLoginDTO)` 函数，调用 `POST /api/v1/auth/guest-login`
- [x] 4.2 在 `types/index.ts` 中新增 `GuestLoginDTO` 接口

## 5. 前端——登录页

- [x] 5.1 修改 `LoginView.vue` 的 `handlePasswordlessLogin()`，改为调用 `guestLogin()` API
