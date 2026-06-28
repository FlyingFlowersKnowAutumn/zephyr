## ADDED Requirements

### Requirement: 用户可切换点赞状态
认证用户 SHALL 能对文章执行点赞 toggle 操作——如果未点赞则创建点赞，已点赞则取消点赞。

#### Scenario: 未点赞用户点赞
- **WHEN** 用户首次对文章发送 `POST /api/v1/articles/{id}/reaction`
- **THEN** 系统创建 reactions 记录（user_id, article_id），返回 `{ "liked": true, "likeCount": N }`，同步 article-service 计数

#### Scenario: 已点赞用户取消点赞
- **WHEN** 用户再次对同一文章发送 `POST /api/v1/articles/{id}/reaction`
- **THEN** 系统删除该 reactions 记录，返回 `{ "liked": false, "likeCount": N-1 }`，同步 article-service 计数

#### Scenario: 未认证用户点赞被拒绝
- **WHEN** 未认证用户发送 `POST /api/v1/articles/{id}/reaction`
- **THEN** 系统返回 401

### Requirement: 用户可查询点赞状态
认证用户 SHALL 能查询自己对某篇文章的点赞状态。

#### Scenario: 已点赞用户查询
- **WHEN** 用户发送 `GET /api/v1/articles/{id}/reaction`
- **THEN** 系统返回 `{ "liked": true }`

#### Scenario: 未点赞用户查询
- **WHEN** 用户发送 `GET /api/v1/articles/{id}/reaction`
- **THEN** 系统返回 `{ "liked": false }`

### Requirement: 文章可获取总点赞数
系统 SHALL 提供文章的总点赞数查询。

#### Scenario: 查询文章点赞数
- **WHEN** 请求 `GET /api/v1/articles/{id}/reaction`（无需认证）
- **THEN** 系统返回该文章的 likeCount（从 reactions 表 COUNT 计算）

### Requirement: ~BREAKING~ article-service 移除旧点赞端点
article-service 的 `POST /api/v1/articles/{id}/like` 端点 SHALL 被移除，点赞功能完全由 interaction-service 接管。

#### Scenario: 旧端点不可用
- **WHEN** 请求 `POST /api/v1/articles/{id}/like`
- **THEN** 系统返回 404（网关不再路由此路径）
