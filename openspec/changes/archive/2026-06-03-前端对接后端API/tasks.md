## 1. Auth Store 对接真实 API

- [x] 1.1 `login()` 改为调用 `authApi.login()`，移除 mock token 生成
- [x] 1.2 `register()` 改为调用 `authApi.register()`，移除 mock user 生成
- [x] 1.3 `fetchCurrentUser()` 改为调用 `authApi.getCurrentUser()`
- [x] 1.4 `logout()` 改为调用 `authApi.logout()`，携带 refreshToken

## 2. Article Store 对接真实 API

- [x] 2.1 `fetchArticles()` 改为调用 `articleApi.listArticles()`，移除本地数组过滤
- [x] 2.2 `fetchArticle()` 改为调用 `articleApi.getArticle()`
- [x] 2.3 `saveArticle()` 改为调用 `articleApi.createArticle()`
- [x] 2.4 `modifyArticle()` 改为调用 `articleApi.updateArticle()`
- [x] 2.5 `removeArticle()` 改为调用 `articleApi.deleteArticle()`
- [x] 2.6 移除 3 条硬编码文章、6 个硬编码标签、3 个硬编码分类
- [x] 2.7 `fetchCategories()` 改为调用 `articleApi.listCategories()`
- [x] 2.8 `fetchTags()` 改为调用 `articleApi.listTags()`

## 3. Store 返回值适配

- [x] 3.1 article store `fetchArticles` 返回结构对齐 `ArticleListDTO`（content/totalElements）
- [x] 3.2 适配 ArticlesView 中使用 `result.total` 的地方改用新字段

## 4. 类型检查验证

- [x] 4.1 运行 `vue-tsc --noEmit` 确保无类型错误
- [x] 4.2 运行后端编译确保无破坏性变更
