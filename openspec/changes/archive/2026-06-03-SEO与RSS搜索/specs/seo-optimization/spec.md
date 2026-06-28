## ADDED Requirements

### Requirement: 文章使用 slug 化 URL
系统 SHALL 为每篇文章生成唯一的 slug，前端路由使用 `/articles/:slug` 访问文章详情。

#### Scenario: 创建文章时自动生成 slug
- **WHEN** 用户创建文章，标题为 "Vue3 响应式原理探析"
- **THEN** 系统自动生成 slug "vue3-reactive-principles"，可手动修改

#### Scenario: slug 唯一性保证
- **WHEN** 两篇文章生成相同 slug
- **THEN** 后端自动追加数字后缀，如 "my-post" → "my-post-2"

#### Scenario: 旧 UUID 链接重定向
- **WHEN** 用户访问旧链接 `/articles/abc123-def456`
- **THEN** 系统 301 重定向到对应的 slug URL

### Requirement: 每页注入 SEO Meta 标签
系统 SHALL 在每页动态注入 `<title>` 和 `<meta>` 标签（description、og:*、twitter:*）。

#### Scenario: 首页 SEO
- **WHEN** 用户访问首页 `/`
- **THEN** 页面 meta 包含 title="个人技术博客"、description="分享技术心得与学习笔记"

#### Scenario: 文章详情页 SEO
- **WHEN** 用户访问文章详情页 `/articles/some-slug`
- **THEN** 页面 meta 包含 title=文章标题、description=文章摘要、og:image=文章封面

#### Scenario: 默认值回退
- **WHEN** 某页面未提供特定 meta
- **THEN** SeoHead 组件使用全局默认值

### Requirement: 自动生成 Sitemap
系统 SHALL 提供 `/api/v1/sitemap.xml` 端点，生成包含首页和所有已发布文章的 sitemap。

#### Scenario: Sitemap 包含首页和已发布文章
- **WHEN** 搜索引擎抓取 `GET /api/v1/sitemap.xml`
- **THEN** 返回 XML 包含首页（priority=1.0）和所有 status=PUBLISHED 的文章（priority=0.8）

#### Scenario: Sitemap 不包含草稿
- **WHEN** 存在 status=DRAFT 的文章
- **THEN** sitemap 中不包含该文章的 URL
