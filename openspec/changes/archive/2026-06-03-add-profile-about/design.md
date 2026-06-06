## Context

现有 user-service 已定义 `User`（含 displayName/bio/avatarUrl）、`UserProfile`（含 website/github/twitter/about）和 `AboutConfig`（含 content/skills/socialLinks）三个实体及其 Repository。前端使用 Pinia store 管理认证状态，API 层通过 axios 实例调用后端。现需补齐 Controller/Service/DTO 层和前端页面以形成完整闭环。

## Goals / Non-Goals

**Goals:**
- 提供用户资料查看和编辑 API（GET/PUT `/api/v1/users/{userId}/profile`）
- 提供关于页配置查看和编辑 API（GET/PUT `/api/v1/about`）
- 前端个人中心页：查看/编辑本人资料
- 前端关于页：公开展示博主信息，博主可编辑

**Non-Goals:**
- 头像文件上传（使用外部 URL）
- 密码修改
- OAuth 第三方账号绑定

## Decisions

### 1. UserProfile 与 User 数据合并返回

**决策**: `UserProfileDTO` 合并 User 和 UserProfile 两张表的数据，对外暴露一个统一的用户资料视图。

**理由**: 前端不需要感知后端表结构。displayName/bio/avatarUrl 在 users 表，website/github/twitter/about 在 user_profiles 表，合并后减少前端请求次数。

### 2. 编辑权限：仅本人可编辑

**决策**: PUT `/api/v1/users/{userId}/profile` 通过 `X-User-Id` 请求头校验调用者身份，仅允许本人编辑。

**理由**: 当前项目没有后台管理页面，仅博主本人登录后编辑自己的资料。无需 RBAC。

### 3. 关于页单例配置

**决策**: `AboutConfig` 按 `userId` 查询，GET `/api/v1/about` 默认返回博主（role=blogger）的配置。首次访问时若无配置则返回空默认值。

**理由**: 关于页是博主专属内容，访客无需指定 userId；自动创建默认值避免空指针。

### 4. 前端架构

**决策**: 
- `ProfileView.vue` — 路由 `/profile`（本人，需登录）和 `/profile/:userId`（查看他人）
- `AboutView.vue` — 路由 `/about`（公开），博主登录后显示编辑入口
- 新增 `src/api/profile.ts` 和 `src/api/about.ts`

**理由**: 遵循项目已有的 View + API + Store 分层模式。

## Risks / Trade-offs

- **UserProfile 表可能不存在记录**: 首次编辑时需 upsert（saveOrUpdate）
- **关于页无博主时返回空**: 不阻塞页面渲染，前端显示"暂无介绍"占位
