## 1. 后端 DTO 层 (blog-common)

- [x] 1.1 创建 `UserProfileDTO.java`（userId, displayName, bio, avatarUrl, website, github, twitter, about）
- [x] 1.2 创建 `UpdateUserProfileDTO.java`（displayName, bio, avatarUrl, website, github, twitter, about 均为可选）
- [x] 1.3 创建 `AboutDTO.java`（content, skills[], socialLinks{}）
- [x] 1.4 创建 `UpdateAboutDTO.java`（content, skills[], socialLinks{} 均为可选）
- [x] 1.5 `UserDTO.java` 补充 displayName、bio 字段

## 2. 后端 Service 层 (user-service)

- [x] 2.1 创建 `UserProfileService.java`（getProfile, updateProfile — upsert 逻辑）
- [x] 2.2 创建 `AboutService.java`（getAbout — 按博主用户查询并返回默认值；saveAbout — upsert）

## 3. 后端 Controller 层 (user-service)

- [x] 3.1 创建 `UserProfileController.java`（GET /api/v1/users/{userId}/profile, PUT /api/v1/users/{userId}/profile — 校验身份）
- [x] 3.2 创建 `AboutController.java`（GET /api/v1/about, PUT /api/v1/about — 校验博主角色）

## 4. 前端类型与 API 层

- [x] 4.1 `src/types/index.ts` 新增 UserProfileDTO、UpdateUserProfileDTO、AboutDTO、UpdateAboutDTO 类型
- [x] 4.2 创建 `src/api/profile.ts`（getUserProfile, updateUserProfile）
- [x] 4.3 创建 `src/api/about.ts`（getAbout, updateAbout）

## 5. 前端页面与路由

- [x] 5.1 创建 `ProfileView.vue`（查看/编辑个人资料，含编辑模式切换）
- [x] 5.2 创建 `AboutView.vue`（公开展示 + 博主编辑入口）
- [x] 5.3 `src/router/index.ts` 新增 `/profile`、`/profile/:userId`、`/about` 路由
