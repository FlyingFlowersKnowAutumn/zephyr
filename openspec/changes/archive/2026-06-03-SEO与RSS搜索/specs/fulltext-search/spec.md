## ADDED Requirements

### Requirement: 文章全文检索
系统 SHALL 支持对已发布文章进行全文检索，使用 PostgreSQL tsvector 匹配标题和正文内容。

#### Scenario: 按关键词搜索
- **WHEN** 用户请求 `GET /api/v1/articles?keyword=响应式&page=0&size=10`
- **THEN** 系统返回标题或正文包含"响应式"的已发布文章，按 ts_rank 相关性排序

#### Scenario: 搜索结果为空
- **WHEN** 关键词未匹配任何文章
- **THEN** 系统返回空分页列表

#### Scenario: 搜索仅限已发布文章
- **WHEN** 存在 status=DRAFT 的文章包含关键词
- **THEN** 搜索结果中不包含该草稿文章

#### Scenario: 中文分词基本支持
- **WHEN** 用户搜索"Vue3 原理"
- **THEN** 系统使用 `simple` 分词配置匹配包含"Vue3"和"原理"的文章

### Requirement: 搜索向量自动更新
系统 SHALL 在创建或更新文章时自动更新 search_vector 列。

#### Scenario: 创建文章时生成搜索向量
- **WHEN** 新文章保存到数据库
- **THEN** search_vector 列自动根据 title 和 content 生成（GENERATED ALWAYS AS）

#### Scenario: 更新文章时刷新搜索向量
- **WHEN** 文章 title 或 content 被修改
- **THEN** search_vector 自动重新生成

### Requirement: 前端搜索入口
系统 SHALL 在前端提供搜索入口，支持关键词输入并跳转搜索结果页。

#### Scenario: 搜索输入
- **WHEN** 用户在导航栏搜索框输入关键词并提交
- **THEN** 跳转到 `/search?q=关键词`，展示搜索结果列表

#### Scenario: 搜索结果分页
- **WHEN** 搜索结果超过 10 条
- **THEN** 搜索结果页底部显示分页控件
