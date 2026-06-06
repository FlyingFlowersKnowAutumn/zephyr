## Context

当前博客已有 user-service（用户认证/JWT/游客）和 article-service（文章CRUD/搜索/标签），但缺少互动能力。按照开发流程规划，阶段三为互动系统。现有 article-service 中有一个简易的 `POST /{id}/like` 自增端点，但不支持用户去重，将在此变更中移除。

**约束条件**：
- 服务间通信：当前未启用 Nacos/RocketMQ，使用直连模式（`localhost` 硬编码路由）
- 数据库：所有服务共享一个本地 PostgreSQL 实例
- 前端：Vue 3 + TypeScript + Pinia，已有 auth/article store 模式可参考

## Goals / Non-Goals

**Goals:**
- 创建独立的 interaction-service 微服务（端口 8003，interaction_db）
- 实现评论嵌套回复（parent_id 邻接表），只支持一层嵌套
- 实现基于用户的点赞 toggle（存在删除，不存在创建）
- 实现提问/回答功能，博主可采纳答案
- 敏感词过滤（内存字典匹配）
- 评论/点赞数量通过 interaction-service 回调同步到 article-service

**Non-Goals:**
- 不实现 users_ref / articles_ref 引用表同步（单 PG 实例，无需副本）
- 不引入 RocketMQ 事件驱动（当前阶段使用同步 HTTP 回调）
- 不支持评论的无限层级嵌套（仅一层：顶级评论 + 子回复）
- 不做评论区实时推送（WebSocket）
- 不做评论通知（邮件/站内信）

## Decisions

### 1. 数据隔离：直接引用 vs 引用表

**选择：直接引用 user_id / article_id**

```
文档方案 (引用表)                    本方案 (直接引用)
─────────────────────              ─────────────────────
interaction_db                     interaction_db
├── users_ref ← RocketMQ           ├── comments.user_id → UUID
├── articles_ref ← RocketMQ        ├── comments.article_id → UUID
├── comments (FK→refs)             ├── reactions.user_id → UUID
└── reactions (FK→refs)            └── questions.user_id → UUID
```

理由：当前所有服务共享一个 PostgreSQL 实例，没有网络隔离需求。引用表只增加 ~200 行同步代码而无实际价值。后续如需跨库查询，可以通过应用层聚合或引入引用表（架构上只是多加一个 Consumer）。

### 2. 评论计数同步：RocketMQ 异步 vs HTTP 同步回调

**选择：HTTP 同步回调 article-service**

```
interaction-service                   article-service
    │                                       │
    │  POST /api/v1/articles/{id}/stats     │
    │  { "commentCount": +1 }               │
    │──────────────────────────────────────▶│
    │                                       │  UPDATE articles SET comment_count = ?
    │◀──────────────────────────────────────│
```

理由：当前未启用 RocketMQ，引入消息队列只为一个计数同步过于繁重。同步回调延迟可接受（~5ms 同机调用），失败时评论已保存、计数略低，属于可接受的不一致，后续切 RocketMQ 只需替换调用方式。

### 3. 评论嵌套：邻接表 vs 闭包表 vs 嵌套集

**选择：邻接表（parent_id），限制一层嵌套**

```sql
parent_id UUID REFERENCES comments(id)  -- NULL=顶级, 非NULL=回复
```

理由：博客场景评论量小（< 1000/文章），不需要闭包表的复杂查询优化。一层嵌套满足「评论 + 回复」需求，避免多层级带来的 UI 复杂度。

### 4. 点赞：Toggle 模式 vs 计数模式

**选择：Toggle 模式（存在则删除，不存在则创建）**

```
-- 唯一约束保证同一用户同一文章只有一条记录
CREATE UNIQUE INDEX idx_reactions_unique 
  ON reactions(user_id, article_id)
  WHERE user_id IS NOT NULL;
```

理由：这是主流交互（YouTube/Twitter/掘金），用户操作直观。`like_count` 重新计算 `SELECT COUNT(*) FROM reactions WHERE article_id = ?`，由 interaction-service 同步给 article-service。

### 5. 敏感词过滤：内存字典 vs NLP API vs 正则

**选择：内存字典匹配（DFA 算法）**

```java
// 启动时从 classpath:sensitive-words.txt 加载
// DFA 状态机 O(n) 匹配，支持中文
SensitiveWordFilter.filter(text) → String (脱敏后)
```

理由：个人博客不需要企业级审核。DFA 算法性能好（O(n)），字典可随时更新。初期不集成第三方 API。

### 6. 提问可见性

**选择：数据库字段 `is_public` + 查询过滤**

游客提交时选择公开/私密，后端查询按 `is_public` 过滤。博主始终可见所有提问。

## Data Model

### interaction_db 表结构

```
┌─────────────────────┐     ┌──────────────────────┐
│     comments        │     │     reactions        │
├─────────────────────┤     ├──────────────────────┤
│ id          UUID PK │     │ id           UUID PK │
│ article_id  UUID    │     │ user_id      UUID    │
│ user_id     UUID    │     │ article_id   UUID    │
│ parent_id   UUID?   │     │ created_at   TIMESTZ │
│ content     TEXT    │     └──────────────────────┘
│ status      VARCHAR │
│ created_at  TIMESTZ │     ┌──────────────────────┐
│ updated_at  TIMESTZ │     │     questions        │
│ deleted_at  TIMESTZ?│     ├──────────────────────┤
└─────────────────────┘     │ id           UUID PK │
                            │ user_id      UUID    │
                            │ title        VARCHAR │
                            │ content      TEXT    │
                            │ type         VARCHAR │
                            │ status       VARCHAR │
                            │ is_public    BOOL    │
                            │ created_at   TIMESTZ │
                            │ updated_at   TIMESTZ │
                            └──────────────────────┘
                                    │
                                    │ 1:N
                                    ▼
                            ┌──────────────────────┐
                            │      answers         │
                            ├──────────────────────┤
                            │ id           UUID PK │
                            │ question_id  UUID FK │
                            │ user_id      UUID    │
                            │ content      TEXT    │
                            │ is_accepted  BOOL    │
                            │ status       VARCHAR │
                            │ created_at   TIMESTZ │
                            │ updated_at   TIMESTZ │
                            │ deleted_at   TIMESTZ?│
                            └──────────────────────┘
```

## API Design

### 评论
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/comments?article_id=&page=&size=` | 顶级评论列表（含子回复） |
| GET | `/api/v1/comments/{id}/replies` | 获取某评论的子回复 |
| POST | `/api/v1/comments` | 创建评论/回复（需认证） |
| DELETE | `/api/v1/comments/{id}` | 删除评论（作者/博主） |

### 点赞
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/articles/{id}/reaction` | Toggle 点赞（需认证） |
| GET | `/api/v1/articles/{id}/reaction` | 获取当前用户点赞状态 |

### 提问
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/questions?status=&type=&page=&size=` | 提问列表 |
| POST | `/api/v1/questions` | 创建提问（需认证） |
| GET | `/api/v1/questions/{id}` | 提问详情（含回答） |
| POST | `/api/v1/questions/{id}/answers` | 回答提问（需博主） |
| PUT | `/api/v1/questions/{id}/answers/{answerId}/accept` | 采纳答案（需博主） |

### article-service 内部端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/articles/{id}/stats` | 同步评论/点赞计数（内部调用） |

## Risks / Trade-offs

- **[计数不一致]** HTTP 回调失败时评论数不准确 → 前端单独查询 interaction-service 获取真实计数，article 表数据仅作展示
- **[无引用完整性]** 直接引用 user_id/article_id 无 FK 约束 → 软删除 + 应用层处理，评论展示时查询用户信息（user-service 正常返回或标记"已删除用户"）
- **[一层嵌套限制]** 不支持回复的回复 → 产品层面已确认够用，如未来需要，只需允许 parent_id 指向非顶级评论即可
