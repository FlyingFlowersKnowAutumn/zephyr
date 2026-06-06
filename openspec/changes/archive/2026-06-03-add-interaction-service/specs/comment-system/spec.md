## ADDED Requirements

### Requirement: 用户可查看文章评论列表
系统 SHALL 提供分页查询指定文章的所有顶级评论（parent_id 为 NULL），每条顶级评论附带其直接子回复列表。

#### Scenario: 查询有空评论的文章
- **WHEN** 用户请求 `GET /api/v1/comments?article_id={id}&page=0&size=20`
- **THEN** 系统返回该文章下 status=approved 的顶级评论分页列表，每条评论包含 authorId、authorName、content、createdAt

#### Scenario: 查询有嵌套回复的评论
- **WHEN** 文章某顶级评论有 3 条子回复
- **THEN** 返回的顶级评论对象中 `replies` 字段包含这 3 条子回复，按 createdAt 升序排列

#### Scenario: 已删除评论不可见
- **WHEN** 某评论 deletedAt 不为 NULL
- **THEN** 系统在查询结果中排除该评论

### Requirement: 用户可创建评论
认证用户 SHALL 能对文章创建顶级评论，内容通过敏感词过滤后保存。

#### Scenario: 创建顶级评论成功
- **WHEN** 认证用户发送 `POST /api/v1/comments`，body 包含 `{ "articleId": "xxx", "content": "写得真好" }`
- **THEN** 系统返回 201，评论 status 为 "approved"，评论数同步至 article-service

#### Scenario: 未认证用户创建评论被拒绝
- **WHEN** 未认证用户发送 `POST /api/v1/comments`
- **THEN** 系统返回 401

#### Scenario: 敏感词过滤
- **WHEN** 评论内容包含敏感词字典中的词汇
- **THEN** 系统将敏感词替换为 `***` 后保存，返回脱敏后的内容

#### Scenario: 空内容评论被拒绝
- **WHEN** 评论内容为空或仅包含空白字符
- **THEN** 系统返回 400，错误码 1001

### Requirement: 用户可回复评论
认证用户 SHALL 能对任意评论创建子回复，系统限制仅一层嵌套（不能回复子回复）。

#### Scenario: 回复评论成功
- **WHEN** 用户对某顶级评论发送 `POST /api/v1/comments`，body 包含 `{ "articleId": "xxx", "parentId": "yyy", "content": "同意" }`
- **THEN** 系统创建 parent_id 指向该顶级评论的子回复，返回 201

#### Scenario: 禁止对子回复再回复
- **WHEN** 用户尝试回复一条 parent_id 不为 NULL 的评论
- **THEN** 系统返回 400，提示只支持一层嵌套回复

### Requirement: 用户可删除自己的评论
认证用户 SHALL 能软删除自己创建的评论。

#### Scenario: 作者删除自己评论
- **WHEN** 评论作者发送 `DELETE /api/v1/comments/{id}`
- **THEN** 系统将该评论的 deletedAt 设为当前时间，返回 204，评论数同步至 article-service

#### Scenario: 非作者无权删除
- **WHEN** 用户尝试删除他人的评论
- **THEN** 系统返回 403

### Requirement: 博主可管理所有评论
博主（role=admin）SHALL 能删除任意评论或修改任意评论的状态。

#### Scenario: 博主删除任意评论
- **WHEN** 博主发送 `DELETE /api/v1/comments/{id}`
- **THEN** 系统执行软删除，返回 204

#### Scenario: 博主修改评论状态
- **WHEN** 博主发送 `PUT /api/v1/comments/{id}/status`，body `{ "status": "rejected" }`
- **THEN** 评论状态更新为 rejected，该评论在公开查询中不可见

### Requirement: 评论计数同步
创建或删除评论后，系统 SHALL 同步更新 article-service 中的 commentCount 字段。

#### Scenario: 创建评论后评论数+1
- **WHEN** 一条评论创建成功
- **THEN** 系统调用 `POST /api/v1/articles/{articleId}/stats` 增加 commentCount

#### Scenario: 删除评论后评论数-1
- **WHEN** 一条评论被软删除
- **THEN** 系统调用 `POST /api/v1/articles/{articleId}/stats` 减少 commentCount
