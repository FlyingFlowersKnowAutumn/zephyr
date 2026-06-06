## Why

博客缺少博主个人主页和关于页面，读者无法了解博主背景与技术栈，博主也无处展示个人资料。AboutConfig 和 UserProfile 实体/仓库已存在，只需补齐 Controller/Service/DTO 和前端页面即可完成该功能闭环。

## What Changes

- 后端新增 `UserProfileController` + `UserProfileService`，支持查看和编辑用户资料（displayName, bio, avatar, website, github, twitter, about）
- 后端新增 `AboutController` + `AboutService`，支持查看和编辑关于页配置（content, skills, socialLinks）
- 新增 `UserProfileDTO`、`AboutDTO` 到 blog-common
- `UserDTO` 补充 `displayName`、`bio` 字段
- 前端新增 `ProfileView.vue` 个人中心页（查看/编辑资料）
- 前端新增 `AboutView.vue` 关于页（公开展示）
- 前端新增 `profile.ts` API 文件，新增 `about.ts` API 文件
- 路由新增 `/profile`、`/profile/:userId`、`/about`

## Capabilities

### New Capabilities

- `user-profile`: 用户资料查看与编辑，支持 displayName/bio/avatar/website/github/twitter/about 字段
- `about-page`: 关于页配置管理与公开展示，支持 content/skills/socialLinks

### Modified Capabilities

_（无现有 specs，无需修改）_

## Impact

- **blog-common**: 新增 `UserProfileDTO.java`、`AboutDTO.java`（内嵌 SkillsDTO、SocialLinksDTO）
- **user-service**: 新增 `UserProfileService`、`AboutService`、`UserProfileController`、`AboutController`
- **blog-frontend**: 新增 `src/api/profile.ts`、`src/api/about.ts`、`src/views/ProfileView.vue`、`src/views/AboutView.vue`，修改 `src/router/index.ts`、`src/types/index.ts`
