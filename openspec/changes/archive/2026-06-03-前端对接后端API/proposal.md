## Why

前端 Store 层全部使用硬编码 mock 数据——登录返回假 token、文章列表是 3 条固定数据、发布文章只在本地数组 unshift。后端 API 已全部就绪，但前端从未调用。博客看起来能跑，实际端到端不通。

## What Changes

- `stores/auth.ts`：`login()`/`register()`/`fetchCurrentUser()` 从本地 mock 改为调用 auth API
- `stores/article.ts`：全部 CRUD 操作从本地数组操作改为调用 article API，移除硬编码示例数据
- `composables/useAuth.ts`：无需改动（透传 store 方法，接口不变）

## Capabilities

### New Capabilities

无。本次是前端 Store 层的实现细节变更，不引入新能力。

### Modified Capabilities

无。用户可见行为不变——仍可登录、查看文章、发布文章，只是数据来源从 mock 变为真实 API。

## Impact

- `stores/auth.ts` — 移除 mock token/user，改为调用 `@/api/auth`
- `stores/article.ts` — 移除 3 条硬编码文章和 mock 分类/标签，改为调用 `@/api/article`
- 所有引用 Store 的视图层（HomeView/ArticlesView/ArticleDetailView/WriteView 等）无需改动——Store 接口签名不变
