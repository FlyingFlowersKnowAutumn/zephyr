## Why

当前博客无法被搜索引擎收录（无 meta 标签、无 sitemap、路由使用 UUID），访客无法订阅更新（无 RSS），站内检索只能按标题简单匹配（无全文搜索）。这直接限制了博客的传播力和用户体验。按开发流程规划，SEO/RSS 属于阶段四，全文搜索属于 P1 功能。

## What Changes

- **Slug 化路由**：新增 `slug` 字段到 Article 实体和 ArticleDTO，前端路由从 `/articles/:id` 改为 `/articles/:slug`（**BREAKING** 旧 UUID 路由）
- **SEO Meta 管理**：引入 `@unhead/vue`，创建 SeoHead 组件，每页自动注入 title/description/og:image/twitter:card
- **Sitemap 生成**：article-service 新增 `GET /api/v1/sitemap.xml` 端点，自动生成包含首页和所有已发布文章的 sitemap
- **RSS 订阅**：article-service 新增 `GET /api/v1/rss.xml` 端点，引入 Rome 库生成 RSS 2.0 格式 Feed
- **全文搜索**：articles 表新增 `search_vector TSVECTOR` 列 + GIN 索引，修改 ArticleRepository 增加 tsvector 查询，ArticleController 已有 keyword 参数改为全文检索
- **前端搜索 UI**：导航栏增加搜索入口，搜索结果页展示匹配文章列表
- **暗色模式**：引入 `@vueuse/core` 的 `useDark`，全局暗色/亮色切换

## Capabilities

### New Capabilities

- `seo-optimization`: SEO meta 标签管理、slug 化 URL、sitemap.xml 自动生成
- `rss-feed`: RSS 2.0 订阅源，包含最近发布文章的标题/链接/摘要
- `fulltext-search`: PostgreSQL tsvector 全文检索，支持中文分词（开发环境用 simple，生产用 zhparser）
- `dark-mode`: 全局暗色模式切换，使用 CSS Variables + `@vueuse/core` useDark

### Modified Capabilities

<!-- 无现有 specs -->

## Impact

- **article-service**: `Article.java` 新增 slug/searchVector 字段；`ArticleDTO.java` 新增 slug；新增 Flyway 迁移脚本；新增 `SitemapController`、`RssController`；修改 `ArticleRepository` 搜索逻辑
- **blog-common**: `ArticleDTO.java` 和 `ArticleCreateDTO.java` 新增 slug 字段
- **前端依赖**: 新增 `@unhead/vue`、`@vueuse/core`（或利用现有 GSAP 生态）
- **前端路由**: `router/index.ts` — `/articles/:id` → `/articles/:slug`，新增 `/search` 路由
- **前端组件**: 新增 `SeoHead.vue`、`SearchView.vue`、`DarkModeToggle.vue`
- **前端修改**: 所有链接到文章详情的路由参数从 id 改为 slug
