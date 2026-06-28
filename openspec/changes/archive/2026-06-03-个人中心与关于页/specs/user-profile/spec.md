## ADDED Requirements

### Requirement: 查看用户资料
系统 SHALL 提供 API 供客户端查询任意用户的公开资料，包含 displayName、bio、avatarUrl、website、github、twitter、about 字段。

#### Scenario: 查询存在的用户资料
- **WHEN** 客户端请求 `GET /api/v1/users/{userId}/profile`
- **THEN** 系统返回该用户的 UserProfileDTO，code=0

#### Scenario: 查询不存在的用户
- **WHEN** 客户端请求不存在的 userId
- **THEN** 系统返回 code=404，message="用户不存在"

### Requirement: 编辑本人资料
系统 SHALL 允许已登录用户编辑自己的资料。请求头 `Authorization` 中的用户身份必须与 `{userId}` 一致。

#### Scenario: 成功编辑资料
- **WHEN** 已登录用户 PUT `/api/v1/users/{userId}/profile` 且 userId 与 token 身份一致
- **THEN** 系统更新 displayName/bio/avatarUrl/website/github/twitter/about 字段，返回更新后的 UserProfileDTO

#### Scenario: 越权编辑
- **WHEN** 用户尝试编辑他人的 userId
- **THEN** 系统返回 code=403，message="无权操作"

#### Scenario: 未登录编辑
- **WHEN** 未携带有效 token 请求
- **THEN** 系统返回 code=401
