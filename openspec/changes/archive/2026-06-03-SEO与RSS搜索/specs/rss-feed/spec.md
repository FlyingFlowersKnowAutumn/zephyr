## ADDED Requirements

### Requirement: 生成 RSS 2.0 Feed
系统 SHALL 提供 `/api/v1/rss.xml` 端点，返回 RSS 2.0 格式的最近发布文章订阅源。

#### Scenario: RSS Feed 包含最近文章
- **WHEN** 用户请求 `GET /api/v1/rss.xml`
- **THEN** 返回 RSS 2.0 XML，title="个人技术博客"，包含最近 20 篇已发布文章

#### Scenario: RSS Feed 文章条目包含必要字段
- **WHEN** RSS Feed 生成
- **THEN** 每个 `<item>` 包含 title、link（slug URL）、description（文章摘要）、pubDate

#### Scenario: 草稿不出现
- **WHEN** 存在 status=DRAFT 的文章
- **THEN** RSS Feed 中不包含该文章

### Requirement: 首页暴露 RSS 链接
系统 SHALL 在首页 HTML 中添加 RSS `<link>` 标签，便于浏览器和 RSS 阅读器自动发现。

#### Scenario: 首页 RSS 发现标签
- **WHEN** 用户访问首页
- **THEN** `<head>` 中包含 `<link rel="alternate" type="application/rss+xml" title="RSS" href="/api/v1/rss.xml">`
