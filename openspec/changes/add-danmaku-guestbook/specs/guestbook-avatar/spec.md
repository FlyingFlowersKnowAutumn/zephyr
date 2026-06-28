## ADDED Requirements

### Requirement: 创建留言时固化头像
系统 SHALL 在创建 Guestbook 消息时接受并存储用户的 avatarUrl，后续查询时一并返回。

#### Scenario: 带头像创建留言
- **WHEN** 已登录用户创建留言且请求包含 avatarUrl
- **THEN** 系统将 avatarUrl 存入 guestbook 记录，返回的 GuestbookDTO 包含 avatarUrl

#### Scenario: 无头像创建留言
- **WHEN** 用户创建留言时未提供 avatarUrl
- **THEN** 系统将 avatarUrl 存为 null，返回的 GuestbookDTO 中 avatarUrl 为 null

### Requirement: 查询留言返回头像
系统 SHALL 在查询 Guestbook 列表时返回每条留言的 avatarUrl 字段。

#### Scenario: 查询历史留言
- **WHEN** 客户端请求 GET /guestbook
- **THEN** 返回的每条 GuestbookDTO 均包含 avatarUrl 字段（可能为 null）
