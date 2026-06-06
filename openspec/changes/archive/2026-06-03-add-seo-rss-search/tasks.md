## 1. Slug 化路由（后端）

- [x] 1.1 Article 实体新增 `slug` 字段（unique, length=500）
- [x] 1.2 ArticleDTO / ArticleCreateDTO 新增 slug 字段
- [x] 1.3 创建 Flyway 迁移脚本 V2__add_slug.sql（ALTER TABLE articles ADD slug）
- [x] 1.4 ArticleRepository 新增 `findBySlug` 方法
- [x] 1.5 ArticleService 新增 slug 生成逻辑（标题→拼音/英文slug，重复追加后缀）
- [x] 1.6 ArticleController 新增 `GET /api/v1/articles/slug/:slug` 端点

## 2. 前端 SEO 基础设施

- [x] 2.1 安装 `@unhead/vue` 依赖
- [x] 2.2 创建 `SeoHead.vue` 组件（title/description/og/twitter meta）
- [x] 2.3 在 `main.ts` 注册 @unhead/vue 插件
- [x] 2.4 在 HomeView/ArticlesView/ArticleDetailView 中集成 SeoHead

## 3. Sitemap 生成（后端）

- [x] 3.1 创建 `SitemapController.java`（GET /api/v1/sitemap.xml，拼接 XML）
- [x] 3.2 ArticleRepository 新增 `findPublishedArticles(limit)` 查询
- [x] 3.3 在网关白名单中添加 sitemap.xml 路径

## 4. RSS 订阅（后端）

- [x] 4.1 article-service pom.xml 添加 Rome 依赖（com.rometools:rome:2.1.0）
- [x] 4.2 创建 `RssController.java`（GET /api/v1/rss.xml，生成 RSS 2.0）
- [x] 4.3 在网关白名单中添加 rss.xml 路径
- [x] 4.4 前端首页 `<head>` 添加 RSS `<link>` 发现标签

## 5. 全文搜索（后端）

- [x] 5.1 创建 Flyway 迁移脚本 V3__add_search_vector.sql（新增 tsvector 列 + GIN 索引）
- [x] 5.2 Article 实体新增 `searchVector` 字段（insertable=false, updatable=false）
- [x] 5.3 ArticleRepository 新增 `searchByTsVector` 查询方法
- [x] 5.4 修改 ArticleService.getArticles 中的 keyword 搜索使用 tsvector
- [x] 5.5 ArticleController 中 keyword 参数搜索改为全文检索

## 6. 暗色模式（前端）

- [x] 6.1 安装 `@vueuse/core` 依赖（或手动实现 useDark composable）
- [x] 6.2 创建 `composables/useDarkMode.ts`
- [x] 6.3 定义亮色/暗色 CSS Variables 双套主题
- [x] 6.4 创建 `DarkModeToggle.vue` 组件
- [x] 6.5 在 `App.vue` 中集成暗色模式切换

## 7. 前端搜索 UI

- [x] 7.1 在导航栏/Header 中添加搜索输入框
- [x] 7.2 创建 `SearchView.vue`（搜索结果页，分页列表）
- [x] 7.3 在 `router/index.ts` 中添加 `/search` 路由
- [x] 7.4 修改 `src/api/article.ts` 的 listArticles 支持 keyword 参数

## 8. 路由迁移与兼容

- [x] 8.1 修改 `router/index.ts` — `/articles/:id` 改为 `/articles/:slug`
- [x] 8.2 修改 ArticleDetailView — 使用 slug 获取文章
- [x] 8.3 修改 ArticlesView — 文章卡片链接使用 slug
- [x] 8.4 修改 WriteView — 编辑后导航使用 slug
- [x] 8.5 后端添加 UUID→slug 重定向端点（兼容旧链接）

## 9. 集成与验证

- [x] 9.1 验证：创建文章 → slug 自动生成 → 文章页可访问
- [x] 9.2 验证：`GET /api/v1/sitemap.xml` 返回正确 XML
- [x] 9.3 验证：`GET /api/v1/rss.xml` 返回正确 RSS 2.0
- [x] 9.4 验证：全文搜索返回匹配结果
- [x] 9.5 验证：暗色模式切换正常，刷新保持偏好
- [x] 9.6 验证：旧 UUID 链接可正确重定向到新 slug URL
