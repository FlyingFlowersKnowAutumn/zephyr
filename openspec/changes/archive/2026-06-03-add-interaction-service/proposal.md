## Why

个人博客缺少读者互动能力——访客无法评论文章、点赞内容或向博主提问。这些是博客的基础互动功能，按照开发流程规划属于阶段三，在核心服务（用户/文章）已完成的前提下，互动系统是自然的下一步。

## What Changes

- **新增 interaction-service 微服务**（端口 8003），独立的 interaction_db 数据库
- **评论系统**：支持顶级评论 + 嵌套回复，敏感词过滤，软删除
- **点赞系统**：基于用户的 toggle 点赞（同用户再次操作即取消），替代 article-service 现有的简单自增逻辑
- **提问系统**：访客可提交公开/私密提问，博主可回复并采纳答案
- **网关路由**：新增 `/api/v1/comments/**`、`/api/v1/questions/**`、`/api/v1/reactions/**` 路由
- **blog-common**：新增 CommentDTO、QuestionDTO、AnswerDTO 及相关 Request DTO
- **article-service**：删除 `POST /{id}/like` 端点（**BREAKING**），commentCount 通过 interaction-service 同步更新
- **前端**：文章详情页增加评论组件和点赞按钮，新增提问页面

## Capabilities

### New Capabilities

- `comment-system`: 文章评论功能，支持嵌套回复、敏感词过滤、软删除、状态管理（approved/pending/rejected）
- `reaction-system`: 文章点赞 toggle 功能，同一用户对同一文章只能有一条点赞记录，再次操作取消点赞
- `question-system`: 提问与建议功能，支持公开/私密切换，博主回复与采纳答案

### Modified Capabilities

<!-- 无现有 specs，不涉及 -->

## Impact

- **新增服务**: `blog-parent/interaction-service/` 模块
- **父 POM**: `blog-parent/pom.xml` 新增 `<module>interaction-service</module>`
- **网关**: `api-gateway/application.yml` 新增 interaction-service 路由
- **blog-common**: 新增 6 个 DTO 类（CommentDTO, CommentCreateDTO, ReactionDTO, QuestionDTO, QuestionCreateDTO, AnswerDTO）
- **article-service**: 删除 `POST /api/v1/articles/{id}/like` 端点，新增 `POST /api/v1/articles/{id}/comment-count` 内部端点（供 interaction-service 回调）
- **前端新增**: `src/views/QuestionsView.vue`（提问页）、`src/components/interaction/CommentSection.vue`、`src/api/comment.ts`、`src/stores/comment.ts`
- **前端修改**: `ArticleDetailView.vue` 集成评论和点赞组件
