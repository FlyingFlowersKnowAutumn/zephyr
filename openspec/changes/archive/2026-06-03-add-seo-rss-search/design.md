## Context

当前博客使用 UUID 作为文章路径（`/articles/:id`），无 meta 标签、无 sitemap、无 RSS、无暗色模式。搜索仅通过 `LIKE` 匹配标题。SEO 优化和全文搜索是开发文档中明确的阶段四和 P1 需求。

**约束**：
- 前端 Vue 3 + Vite，已集成 GSAP
- 后端 Spring Boot 3.2 + PostgreSQL + JPA
- 无需 SSR（文档标注"可选"），纯 SPA 也能做基础 SEO

## Goals / Non-Goals

**Goals:**
- 文章路由从 UUID 迁移到 slug（`/articles/my-first-post`）
- 每页注入 SEO meta（title/description/og/twitter:card）
- 生成 sitemap.xml（首页 + 已发布文章）
- 生成 RSS 2.0 Feed（最近 20 篇已发布文章）
- PostgreSQL tsvector 全文检索（支持中文分词）
- 全局暗色模式切换

**Non-Goals:**
- 不引入 SSR/SSG 框架（Nuxt 等）
- 不接入 Google Search Console API（手动提交 sitemap）
- 不实现 zhparser 生产级分词（开发环境用 `simple` 字典 + `plainto_tsquery`）
- 不做图片 SEO（alt 标签以后优化）

## Decisions

### 1. Slug 生成策略

**选择：前端创建文章时自动生成 slug，后端校验唯一性**

```java
// Article.java 新增
@Column(unique = true, length = 500)
private String slug;

// 生成逻辑：标题 → 去特殊字符 → 空格替换为连字符 → 小写
// "Vue3 响应式原理" → "vue3-xiang-ying-shi-yuan-li"
// 实际使用简化的英文/拼音混合
```

生成策略分两步：
1. 前端输入时从标题实时生成 slug（可手动编辑）
2. 后端保存时检查 slug 唯一性，重复则追加数字后缀

理由：中文标题直接转 slug 会变成纯拼音，可读性差。让用户可编辑，默认自动生成。

### 2. 路由迁移：UUID → Slug

**选择：新增 slug 路由，保留旧 UUID 路由做重定向**

```
/articles/:slug          → ArticleDetailView (新)
/articles/:id            → 301 重定向到 slug (兼容)
```

理由：已分享的 UUID 链接仍可访问，不产生死链。后端通过 UUID 查找文章后返回 slug 触发重定向。

### 3. SEO Head 管理

**选择：@unhead/vue + SeoHead 组件**

```
SeoHead.vue — 接收 title/description/image props，自动注入：
  <title>, <meta name="description">,
  <meta property="og:*">,
  <meta name="twitter:*">
```

每个页面直接使用 `<SeoHead title="..." description="..." />`，默认值取全局配置。

### 4. Sitemap 生成

**选择：article-service 内置端点，无外部依赖**

```java
@GetMapping(value = "/api/v1/sitemap.xml", produces = "application/xml")
```

纯 StringBuilder 拼接 XML，5000 条以内足够，无需引入额外库。

### 5. RSS 生成

**选择：Rome (com.rometools:rome) 库**

```xml
<dependency>
    <groupId>com.rometools</groupId>
    <artifactId>rome</artifactId>
    <version>2.1.0</version>
</dependency>
```

理由：Rome 是事实标准的 Java RSS 库，自动生成标准 RSS 2.0 XML，避免手动拼接 XML 出错。备选：手动 StringBuilder（省依赖但易出错）。

### 6. 全文搜索：tsvector + 中文分词

**选择：PostgreSQL 原生 tsvector，开发环境用 `simple` 配置**

```sql
ALTER TABLE articles ADD COLUMN search_vector TSVECTOR
    GENERATED ALWAYS AS (
        to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, ''))
    ) STORED;

CREATE INDEX idx_articles_search ON articles USING GIN(search_vector);
```

查询：
```java
@Query("SELECT a FROM Article a WHERE a.searchVector @@ plainto_tsquery('simple', :query)")
```

理由：
- `simple` 配置不做语言分析，直接按词匹配，中文也能用（PostgreSQL 默认按空格/标点分词）
- 生产环境切换到 `zhparser` 只需改配置文本，不改变 API
- 避免 `zhparser` 的编译安装复杂度（需要 scws 字典 + PostgreSQL 扩展编译）

### 7. 暗色模式

**选择：CSS Variables + `@vueuse/core` useDark**

```scss
:root {
  --color-bg-primary: #0a0e1a;
  --color-text-primary: #ffffff;
}
.dark {
  --color-bg-primary: #ffffff;
}
```

理由：项目已使用 CSS Variables，暗色模式只需切换 `dark` class 即可。`@vueuse/core` 提供 `useDark`/`useToggle`，自动持久化到 localStorage 并响应系统偏好。

## Risks / Trade-offs

- **[slug 唯一性冲突]** 两篇文章标题相同时 slug 重复 → 后端检测重复自动追加 `-2`, `-3`
- **[搜索性能]** tsvector GIN 索引增量更新，写入时略有开销 → 已发布文章远多于写入，可接受
- **[RSS 缓存]** 每次请求重查数据库 → 后续可用 Redis 缓存 1 小时
- **[SPA SEO 局限]** 搜索引擎对 SPA 索引不如 SSR → sitemap.xml 可缓解，Google 已支持 JS 渲染
