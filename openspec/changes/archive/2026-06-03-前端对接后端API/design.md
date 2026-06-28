## Context

当前前端 3 个 Store 中，`comment.ts` 已使用真实 API，`auth.ts` 和 `article.ts` 仍使用 mock 数据。后端 API 全部就绪，网关路由已配置。前端 API 层（`src/api/`）封装完整，Store 只需调用即可。

约束：保持 Store 对外接口签名不变，视图层零改动。

## Goals / Non-Goals

**Goals:**
- `auth.ts`：`login()`/`register()`/`fetchCurrentUser()` 调用真实 API，token 和 user 由服务端返回
- `article.ts`：全部 CRUD 操作调用真实 API，移除硬编码数据
- 出错时 `error` 字段有可用信息，Loading 状态由 API 调用驱动

**Non-Goals:**
- 不修改 API 层（`src/api/`）——已正确封装
- 不修改视图层——Store 接口签名不变
- 不处理离线/断网重试——后续迭代

## Decisions

### 1. Store 返回值保持现有结构

auth store 的 `login()` 仍返回 `LoginResponseDTO`，article store 的 `fetchArticles()` 仍返回 `{ articles, total, page, size }`。视图层无感知。

### 2. 错误处理策略

API 调用失败时设置 `error.value = result.message`，不抛出异常。视图层通过 `store.error` 显示错误信息。

### 3. 移除 mock 数据

article store 的 3 条硬编码文章、6 个硬编码标签、3 个硬编码分类全部删除。首次渲染时 `articles` 为空数组，`onMounted` 异步拉取。

### 4. auth token 持久化

登录成功后 token 写入 `localStorage`（已有逻辑保留）。`fetchCurrentUser()` 改为调用 `GET /v1/auth/me`，仅在 token 存在时调用。

## Risks / Trade-offs

- [首次加载空白] 移除 mock 后首页无预填充数据 → `loading` 状态驱动骨架屏/加载提示
- [API 不可用] 后端未启动时页面报错 → error 字段展示友好提示，不影响页面渲染
- [token 过期] 无自动刷新机制 → 后续在 axios interceptor 中添加
