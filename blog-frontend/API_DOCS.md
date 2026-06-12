
# 博客前端 API 接口文档

## 概述

本文档描述了博客前端项目的所有 API 接口，包括接口路径、HTTP方法、请求参数、响应结构等信息。

---

## 基础配置

### 基础路径
```
/api/v1/
```

### 请求头
| Header | 值 | 说明 |
|--------|-----|------|
| Authorization | `Bearer {token}` | 认证令牌（登录后必填） |
| Content-Type | `application/json` | 请求体类型 |

### 统一响应格式
```typescript
interface Result<T> {
  code: number        // 状态码：0-成功，其他-失败
  message: string     // 提示信息
  data: T            // 数据
  traceId: string    // 追踪ID
}
```

---

## 认证模块

### 1. 用户登录

**路径**: `POST /v1/auth/login`

**请求体**:
```json
{
  "username": "string",  // 用户名
  "password": "string"   // 密码
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "string",           // JWT令牌
    "refreshToken": "string",    // 刷新令牌
    "user": {
      "id": "string",
      "username": "string",
      "displayName": "string",
      "email": "string",
      "avatarUrl": "string",
      "role": "string",
      "isGuest": false,
      "createdAt": "2024-01-01T00:00:00"
    }
  },
  "traceId": "string"
}
```

---

### 2. 用户注册

**路径**: `POST /v1/auth/register`

**请求体**:
```json
{
  "username": "string",  // 用户名
  "email": "string",     // 邮箱
  "password": "string"   // 密码
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "string",
    "username": "string",
    "email": "string",
    "avatarUrl": "string",
    "role": "USER",
    "isGuest": false,
    "createdAt": "2024-01-01T00:00:00"
  },
  "traceId": "string"
}
```

---

### 3. 获取当前用户

**路径**: `GET /v1/auth/me`

**认证**: 需要登录

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "string",
    "username": "string",
    "displayName": "string",
    "email": "string",
    "avatarUrl": "string",
    "role": "string",
    "isGuest": false,
    "createdAt": "2024-01-01T00:00:00"
  },
  "traceId": "string"
}
```

---

### 4. 用户登出

**路径**: `POST /v1/auth/logout`

**请求体**:
```json
{
  "refreshToken": "string"  // 刷新令牌
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "traceId": "string"
}
```

---

### 5. 访客登录

**路径**: `POST /v1/auth/guest-login`

**说明**: 无需登录，用于匿名访客访问受限资源

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "string",       // 临时令牌
    "userId": "string",      // 用户ID
    "displayName": "string", // 显示名称
    "role": "GUEST",         // 角色
    "expiresAt": "2024-01-01T00:00:00"  // 过期时间
  },
  "traceId": "string"
}
```

---

### 6. 删除访客

**路径**: `DELETE /v1/auth/guest`

**认证**: 需要登录（访客身份）

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "traceId": "string"
}
```

---

## 文章模块

### 1. 创建文章

**路径**: `POST /v1/articles`

**认证**: 需要登录

**请求体**:
```json
{
  "title": "string",        // 文章标题（必填）
  "content": "string",      // 文章内容（必填）
  "slug": "string",         // 文章别名（可选）
  "excerpt": "string",      // 文章摘要（可选）
  "coverImage": "string",   // 封面图片URL（可选）
  "status": "DRAFT|PUBLISHED",  // 状态（必填）
  "categoryId": "string",   // 分类ID（可选）
  "tags": ["string"]        // 标签列表（可选）
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "string",
    "slug": "string",
    "title": "string",
    "content": "string",
    "excerpt": "string",
    "coverImage": "string",
    "status": "DRAFT|PUBLISHED",
    "viewCount": 0,
    "likeCount": 0,
    "commentCount": 0,
    "authorId": "string",
    "authorName": "string",
    "categoryId": "string",
    "categoryName": "string",
    "tags": ["string"],
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00",
    "publishedAt": "2024-01-01T00:00:00"
  },
  "traceId": "string"
}
```

---

### 2. 更新文章

**路径**: `PUT /v1/articles/{id}`

**认证**: 需要登录

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 文章ID |

**请求体**:
```json
{
  "title": "string",        // 文章标题（可选）
  "content": "string",      // 文章内容（可选）
  "excerpt": "string",      // 文章摘要（可选）
  "coverImage": "string",   // 封面图片URL（可选）
  "status": "DRAFT|PUBLISHED|ARCHIVED",  // 状态（可选）
  "categoryId": "string",   // 分类ID（可选）
  "tags": ["string"]        // 标签列表（可选）
}
```

**成功响应**: 同创建文章

---

### 3. 删除文章

**路径**: `DELETE /v1/articles/{id}`

**认证**: 需要登录

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 文章ID |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "traceId": "string"
}
```

---

### 4. 获取文章详情（按ID）

**路径**: `GET /v1/articles/{id}`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 文章ID |

**成功响应**: 同创建文章响应的 data 字段

---

### 5. 获取文章详情（按Slug）

**路径**: `GET /v1/articles/slug/{slug}`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| slug | string | 文章别名 |

**成功响应**: 同创建文章响应的 data 字段

---

### 6. 获取文章列表

**路径**: `GET /v1/articles`

**查询参数**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | number | 0 | 页码（从0开始） |
| size | number | 10 | 每页数量 |
| categoryId | string | - | 分类ID（可选） |
| tag | string | - | 标签名称（可选） |
| keyword | string | - | 搜索关键词（可选） |
| status | string | - | 状态筛选（可选） |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "string",
        "slug": "string",
        "title": "string",
        "excerpt": "string",
        "coverImage": "string",
        "status": "PUBLISHED",
        "viewCount": 100,
        "likeCount": 10,
        "commentCount": 5,
        "authorName": "string",
        "categoryName": "string",
        "tags": ["string"],
        "publishedAt": "2024-01-01T00:00:00"
      }
    ],
    "totalElements": 100,   // 总记录数
    "totalPages": 10,       // 总页数
    "number": 0,            // 当前页码
    "size": 10              // 每页数量
  },
  "traceId": "string"
}
```

---

### 7. 获取分类列表

**路径**: `GET /v1/articles/categories`

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": ["技术", "生活", "思考"],
  "traceId": "string"
}
```

---

### 8. 创建分类

**路径**: `POST /v1/categories`

**认证**: 需要登录

**请求体**:
```json
{
  "name": "string"  // 分类名称
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": "技术",
  "traceId": "string"
}
```

---

### 9. 获取标签列表

**路径**: `GET /v1/tags`

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": ["Vue", "React", "TypeScript"],
  "traceId": "string"
}
```

---

### 10. 创建标签

**路径**: `POST /v1/tags`

**认证**: 需要登录

**请求体**:
```json
{
  "name": "string"  // 标签名称
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": "Vue",
  "traceId": "string"
}
```

---

## 评论模块

### 1. 创建评论

**路径**: `POST /v1/comments`

**认证**: 需要登录

**请求体**:
```json
{
  "articleId": "string",  // 文章ID（必填）
  "parentId": "string",   // 父评论ID（可选，用于回复）
  "content": "string"     // 评论内容（必填）
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "string",
    "articleId": "string",
    "userId": "string",
    "userName": "string",
    "parentId": "string",
    "content": "string",
    "status": "APPROVED",
    "replies": [],
    "createdAt": "2024-01-01T00:00:00"
  },
  "traceId": "string"
}
```

---

### 2. 获取文章评论列表

**路径**: `GET /v1/comments`

**查询参数**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| articleId | string | - | 文章ID（必填） |
| page | number | 0 | 页码 |
| size | number | 20 | 每页数量 |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "string",
        "articleId": "string",
        "userId": "string",
        "userName": "string",
        "parentId": null,
        "content": "string",
        "status": "APPROVED",
        "replies": [...],
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "totalPages": 5,
    "totalElements": 100
  },
  "traceId": "string"
}
```

---

### 3. 获取评论回复

**路径**: `GET /v1/comments/{commentId}/replies`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| commentId | string | 评论ID |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": "string",
      "articleId": "string",
      "userId": "string",
      "userName": "string",
      "parentId": "string",
      "content": "string",
      "status": "APPROVED",
      "replies": [],
      "createdAt": "2024-01-01T00:00:00"
    }
  ],
  "traceId": "string"
}
```

---

### 4. 删除评论

**路径**: `DELETE /v1/comments/{id}`

**认证**: 需要登录

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 评论ID |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "traceId": "string"
}
```

---

## 点赞模块

### 1. 切换点赞状态

**路径**: `POST /v1/articles/{articleId}/reaction`

**认证**: 需要登录

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| articleId | string | 文章ID |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "liked": true,     // 当前是否已点赞
    "likeCount": 10    // 点赞总数
  },
  "traceId": "string"
}
```

---

### 2. 获取点赞状态

**路径**: `GET /v1/articles/{articleId}/reaction`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| articleId | string | 文章ID |

**成功响应**: 同切换点赞状态

---

## 问答模块

### 1. 创建问题

**路径**: `POST /v1/questions`

**认证**: 需要登录

**请求体**:
```json
{
  "title": "string",   // 问题标题（必填）
  "content": "string", // 问题内容（必填）
  "isPublic": true     // 是否公开（默认true）
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "string",
    "userId": "string",
    "userName": "string",
    "title": "string",
    "content": "string",
    "status": "OPEN",
    "isPublic": true,
    "answers": [],
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "traceId": "string"
}
```

---

### 2. 获取问题列表

**路径**: `GET /v1/questions`

**查询参数**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| status | string | - | 状态筛选（OPEN/CLOSED） |
| isPublic | boolean | - | 是否公开 |
| page | number | 0 | 页码 |
| size | number | 10 | 每页数量 |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [...],
    "totalPages": 10,
    "totalElements": 100
  },
  "traceId": "string"
}
```

---

### 3. 获取问题详情

**路径**: `GET /v1/questions/{id}`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 问题ID |

**成功响应**: 同创建问题响应的 data 字段

---

### 4. 关闭问题

**路径**: `PUT /v1/questions/{id}/close`

**认证**: 需要登录（问题作者）

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 问题ID |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "traceId": "string"
}
```

---

### 5. 创建回答

**路径**: `POST /v1/questions/{questionId}/answers`

**认证**: 需要登录

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| questionId | string | 问题ID |

**请求体**:
```json
{
  "content": "string"  // 回答内容（必填）
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "string",
    "questionId": "string",
    "userId": "string",
    "userName": "string",
    "content": "string",
    "isAccepted": false,
    "status": "APPROVED",
    "createdAt": "2024-01-01T00:00:00"
  },
  "traceId": "string"
}
```

---

### 6. 采纳回答

**路径**: `PUT /v1/questions/{questionId}/answers/{answerId}/accept`

**认证**: 需要登录（问题作者）

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| questionId | string | 问题ID |
| answerId | string | 回答ID |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "traceId": "string"
}
```

---

## 用户资料模块

### 1. 获取用户资料

**路径**: `GET /v1/users/{userId}/profile`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| userId | string | 用户ID |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": "string",
    "displayName": "string",
    "bio": "string",
    "avatarUrl": "string",
    "website": "string",
    "github": "string",
    "twitter": "string",
    "about": "string"
  },
  "traceId": "string"
}
```

---

### 2. 更新用户资料

**路径**: `PUT /v1/users/{userId}/profile`

**认证**: 需要登录（本人）

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| userId | string | 用户ID |

**请求体**:
```json
{
  "displayName": "string",  // 显示名称（可选）
  "bio": "string",          // 简介（可选）
  "avatarUrl": "string",    // 头像URL（可选）
  "website": "string",      // 个人网站（可选）
  "github": "string",       // GitHub地址（可选）
  "twitter": "string",      // Twitter地址（可选）
  "about": "string"         // 关于（可选）
}
```

**成功响应**: 同获取用户资料

---

## 关于模块

### 1. 获取关于页面内容

**路径**: `GET /v1/about`

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": "string",       // 关于页面内容
    "skills": ["string"],      // 技能列表
    "socialLinks": "string"    // 社交链接（JSON字符串）
  },
  "traceId": "string"
}
```

---

### 2. 更新关于页面内容

**路径**: `PUT /v1/about`

**认证**: 需要登录（管理员）

**请求体**:
```json
{
  "content": "string",       // 关于页面内容（可选）
  "skills": ["string"],      // 技能列表（可选）
  "socialLinks": "string"    // 社交链接（可选）
}
```

**成功响应**: 同获取关于页面内容

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证（需要登录） |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 接口文件清单

| 文件路径 | 模块 | 接口数量 |
|----------|------|----------|
| `src/api/auth.ts` | 认证 | 4 |
| `src/api/article.ts` | 文章 | 10 |
| `src/api/comment.ts` | 评论 | 4 |
| `src/api/reaction.ts` | 点赞 | 2 |
| `src/api/question.ts` | 问答 | 6 |
| `src/api/profile.ts` | 用户资料 | 2 |
| `src/api/about.ts` | 关于页面 | 2 |
| `src/api/guest.ts` | 访客 | 2 |

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2024-01-01 | 初始版本 |
