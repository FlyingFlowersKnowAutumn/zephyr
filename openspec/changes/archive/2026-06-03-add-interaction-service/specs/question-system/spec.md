## ADDED Requirements

### Requirement: 用户可浏览公开提问列表
系统 SHALL 提供分页查询公开提问的列表，支持按状态和类型过滤。

#### Scenario: 查询公开且未关闭的提问
- **WHEN** 用户请求 `GET /api/v1/questions?status=open&is_public=true&page=0&size=10`
- **THEN** 系统返回 status=open 且 is_public=true 的提问分页列表

#### Scenario: 博主查看所有提问（含私密）
- **WHEN** 博主请求 `GET /api/v1/questions?page=0&size=10`
- **THEN** 系统返回所有提问（包括 is_public=false），按 createdAt 降序排列

### Requirement: 认证用户可创建提问
认证用户 SHALL 能提交提问，可选择公开或私密。

#### Scenario: 创建公开提问
- **WHEN** 认证用户发送 `POST /api/v1/questions`，body 包含 `{ "title": "Vue3 响应式原理", "content": "...", "isPublic": true }`
- **THEN** 系统创建提问，status 为 "open"，返回 201

#### Scenario: 创建私密提问
- **WHEN** 认证用户发送 `POST /api/v1/questions`，body 包含 `{ "title": "...", "content": "...", "isPublic": false }`
- **THEN** 系统创建提问，is_public=false，仅博主和提问者可见

#### Scenario: 未认证用户创建提问被拒绝
- **WHEN** 未认证用户发送 `POST /api/v1/questions`
- **THEN** 系统返回 401

### Requirement: 用户可查看提问详情
系统 SHALL 返回提问详情，包含所有回答列表。

#### Scenario: 查看有回答的提问
- **WHEN** 用户请求 `GET /api/v1/questions/{id}`
- **THEN** 系统返回提问详情，`answers` 数组包含所有回答，采纳的回答 isAccepted=true

#### Scenario: 非提问者查看私密提问被拒绝
- **WHEN** 非提问者且非博主请求私密提问详情
- **THEN** 系统返回 404

### Requirement: 博主可回答提问
博主（role=admin）SHALL 能对提问进行回答。

#### Scenario: 博主回答问题
- **WHEN** 博主发送 `POST /api/v1/questions/{id}/answers`，body `{ "content": "Vue3 使用 Proxy..." }`
- **THEN** 系统创建回答，返回 201

#### Scenario: 非博主回答被拒绝
- **WHEN** 普通用户或游客发送 `POST /api/v1/questions/{id}/answers`
- **THEN** 系统返回 403

### Requirement: 博主可采纳答案
博主 SHALL 能标记某条回答为「已采纳」，同一提问只能有一条被采纳的回答。

#### Scenario: 采纳答案
- **WHEN** 博主发送 `PUT /api/v1/questions/{id}/answers/{answerId}/accept`
- **THEN** 该回答 isAccepted=true，提问 status 自动变为 "answered"

#### Scenario: 更换采纳答案
- **WHEN** 博主对另一条回答执行采纳操作
- **THEN** 之前被采纳的回答 isAccepted 变为 false，新回答 isAccepted=true

### Requirement: 提问者可关闭提问
提问者 SHALL 能关闭自己提出的提问（status 变为 closed）。

#### Scenario: 提问者关闭提问
- **WHEN** 提问者发送 `PUT /api/v1/questions/{id}/close`
- **THEN** 提问 status 变为 "closed"
