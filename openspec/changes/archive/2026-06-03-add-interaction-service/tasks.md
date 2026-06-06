## 1. 基础设施搭建

- [x] 1.1 创建 interaction-service 模块骨架（pom.xml + Application 类 + application.yml）
- [x] 1.2 在 blog-parent/pom.xml 中添加 `<module>interaction-service</module>`
- [x] 1.3 配置 interaction_db 数据源和 Flyway 迁移
- [x] 1.4 创建 Flyway 迁移脚本 V1__init.sql（comments / reactions / questions / answers 四张表）
- [x] 1.5 在 api-gateway/application.yml 中新增 interaction-service 路由（comments/questions/reactions）

## 2. blog-common 共享 DTO

- [x] 2.1 创建 CommentDTO 和 CommentCreateDTO
- [x] 2.2 创建 ReactionDTO 和 ReactionResponseDTO
- [x] 2.3 创建 QuestionDTO、QuestionCreateDTO、AnswerDTO
- [x] 2.4 创建 CommentStatusUpdateDTO（博主修改评论状态）
- [x] 2.5 创建 ArticleStatsUpdateDTO（内部调用同步计数）

## 3. interaction-service 实体与 Repository

- [x] 3.1 创建 Comment 实体（JPA，含 status 枚举和软删除逻辑）
- [x] 3.2 创建 Reaction 实体（含 user_id + article_id 联合唯一约束）
- [x] 3.3 创建 Question 实体（含 status / is_public 字段）
- [x] 3.4 创建 Answer 实体（含 is_accepted 字段）
- [x] 3.5 创建 CommentRepository（按 article_id 查顶级评论、按 parent_id 查子回复）
- [x] 3.6 创建 ReactionRepository（findByUserAndArticle / countByArticle / existsByUserAndArticle）
- [x] 3.7 创建 QuestionRepository（分页查询，按可见性过滤）
- [x] 3.8 创建 AnswerRepository（按 question_id 查询）

## 4. 敏感词过滤

- [x] 4.1 创建 sensitive-words.txt 字典文件（classpath 资源）
- [x] 4.2 实现 SensitiveWordFilter（DFA 算法，启动时加载字典）
- [x] 4.3 为 SensitiveWordFilter 编写单元测试

## 5. interaction-service 业务层

- [x] 5.1 实现 CommentService（创建/删除/列表/回复，含敏感词过滤和嵌套限制）
- [x] 5.2 实现 ReactionService（toggle 点赞 / 查询状态 / 计数）
- [x] 5.3 实现 QuestionService（创建/列表/详情/关闭，含可见性过滤）
- [x] 5.4 实现 AnswerService（创建/采纳）
- [x] 5.5 实现 ArticleStatsClient（RestTemplate 调用 article-service 同步计数）

## 6. interaction-service 控制器

- [x] 6.1 创建 CommentController
- [x] 6.2 创建 ReactionController
- [x] 6.3 创建 QuestionController
- [x] 6.4 创建内部 WebConfig（CORS + OPTIONS）
- [x] 6.5 集成 blog-common 的 GlobalExceptionHandler（scanBasePackages）

## 7. article-service 适配

- [x] 7.1 移除 ArticleController 中的 `POST /{id}/like` 端点
- [x] 7.2 新增 `POST /api/v1/articles/{id}/stats` 内部端点（同步 commentCount / likeCount）
- [x] 7.3 新增 ArticleService.updateStats 方法

## 8. 前端 API 与 Store

- [x] 8.1 创建 `src/api/comment.ts`（评论/回复 API）
- [x] 8.2 创建 `src/api/reaction.ts`（点赞 API）
- [x] 8.3 创建 `src/api/question.ts`（提问 API）
- [x] 8.4 创建 `src/stores/comment.ts`（评论状态管理）
- [x] 8.5 在 `src/types/index.ts` 中添加互动相关 TypeScript 类型

## 9. 前端组件

- [x] 9.1 创建 `CommentSection.vue`（评论列表 + 创建评论 + 嵌套回复展示）
- [x] 9.2 创建 `CommentItem.vue`（单条评论展示，含回复按钮和删除）
- [x] 9.3 创建 `LikeButton.vue`（点赞 toggle 按钮 + 动画）
- [x] 9.4 在 `ArticleDetailView.vue` 中集成 CommentSection 和 LikeButton

## 10. 前端提问页

- [x] 10.1 创建 `QuestionsView.vue`（提问列表 + 筛选）
- [x] 10.2 创建 `QuestionDetailView.vue`（提问详情 + 回答列表）
- [x] 10.3 创建 `AskQuestionView.vue`（创建提问表单）
- [x] 10.4 在路由 `router/index.ts` 中添加提问相关路由

## 11. 网关与集成

- [x] 11.1 更新 api-gateway 路由配置，添加 interaction-service 限流规则
- [x] 11.2 验证前端 `vite.config.ts` 代理配置（已配置 `/api` → `localhost:8080`，无需修改）
- [x] 11.3 端到端验证：评论创建 → 文章详情展示评论 → 计数同步（需手动创建 interaction_db 后验证）
