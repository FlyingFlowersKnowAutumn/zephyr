## Context

当前认证系统有两个缺陷：
1. 修改密码 `PUT /api/v1/auth/password` 不在网关白名单，返回 401
2. 免密登录端点 `POST /api/v1/auth/guest-login` 未实现，前端用验证码充当密码调 login 端点，永远失败

网关白名单使用 `startsWith` 匹配，只需修正路径即可修复问题 1。

## Goals / Non-Goals

**Goals:**
- 修复修改密码 401：白名单路径对齐后端实际端点
- 实现免密登录：邮箱 + 验证码即可登录，未注册邮箱自动创建账号

**Non-Goals:**
- 不改变现有密码登录流程
- 不引入手机号等其他免密方式

## Decisions

### 1. 免密登录实现方式

**方案 A**（采用）：新增独立端点 `POST /api/v1/auth/guest-login`，接受 `{ email, code }`。校验验证码 → 查邮箱 → 已注册则登录，未注册则先注册后登录。

**方案 B**：在现有 login 端点增加验证码登录分支。

选 A 的原因：职责分离清晰，login 端点已有密码校验语义，混入验证码逻辑会增加复杂度和出错风险。

### 2. 自动注册默认值

| 字段 | 默认值 | 说明 |
|------|--------|------|
| username | `Vent` + 4 位随机数 | 防止重复 |
| password | 随机 UUID（bcrypt 加密） | 用户不可见，日后通过修改密码设置 |
| avatarUrl | `/Note/mm.jpg` | 预先放在 blog-frontend/public/Note/ |
| role | `user` | 普通用户 |

### 3. 网关白名单

`/api/v1/auth/guest-login` 已在 WHITE_LIST，无需改动。修正 `/api/v1/auth/forgot-password` → `/api/v1/auth/password`。

## Risks / Trade-offs

- **用户名重复**：`Vent` + 4 位随机数碰撞概率极低（十万分之一），可接受
- **验证码泄露**：和注册/改密使用同一套验证码机制，安全性一致
- **邮箱未验证即注册**：和当前注册流程一致（发送验证码即视为拥有该邮箱），后续可增强
