
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
  "username": "string",  // 用户名或邮箱
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
      "role": "ADMIN | USER",
      "bio": "string",           // 个性签名
      "status": "ACTIVE",         // 账户状态
      "lastLoginAt": "2024-01-01T00:00:00", // 最后登录时间
      "createdAt": "2024-01-01T00:00:00"
    }
  },
  "traceId": "string"
}
```

---

### 2. 用户注册

**路径**: `POST /v1/auth/register`

**说明**: 注册前需先调用发送验证码接口获取验证码

**请求体**:
```json
{
  "username": "string",  // 用户名（4-20位字母数字组合，唯一）
  "email": "string",     // 邮箱（有效邮箱格式，唯一）
  "password": "string",   // 密码（6-20位）
  "code": "string"        // 邮箱验证码（必填，需先调用发送验证码接口）
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
    "role": "USER",
    "createdAt": "2024-01-01T00:00:00"
  },
  "traceId": "string"
}
```

---

### 3. 发送验证码

**路径**: `POST /v1/auth/send-code`

**说明**: 发送邮箱验证码，用于注册

**请求体**:
```json
{
  "email": "string",                    // 邮箱地址
  "type": "REGISTER"                    // 验证码类型（仅支持注册）
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "验证码发送成功",
  "data": null,
  "traceId": "string"
}
```

**业务规则**:
- 验证码有效期：5分钟
- 同一邮箱每分钟最多发送1次
- 每天最多发送5次

---

### 4. 获取当前用户

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
    "role": "ADMIN | USER",
    "bio": "string",
    "status": "ACTIVE",
    "lastLoginAt": "2024-01-01T00:00:00",
    "createdAt": "2024-01-01T00:00:00"
  },
  "traceId": "string"
}
```

---

### 5. 用户登出

**路径**: `POST /v1/auth/logout`

**认证**: 需要登录

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
  "message": "退出登录成功",
  "data": null,
  "traceId": "string"
}
```

---

### 6. 刷新Token

**路径**: `POST /v1/auth/refresh`

**说明**: 使用RefreshToken获取新的AccessToken

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
  "message": "Token刷新成功",
  "data": {
    "token": "string",      // 新的AccessToken
    "expiresIn": 86400      // 过期时间（秒）
  },
  "traceId": "string"
}
```

**业务规则**:
- RefreshToken有效期：7天
- AccessToken有效期：24小时
- RefreshToken过期后需重新登录

---

### 7. 修改密码

**路径**: `PUT /v1/auth/password`

**说明**: 修改密码，使用邮箱+验证码验证，无需登录

**请求体**:
```json
{
  "email": "string",      // 邮箱
  "code": "string",       // 验证码（需先调用发送验证码接口）
  "newPassword": "string" // 新密码（6-20位）
}
```

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "密码修改成功",
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

## 留言板模块

### 1. 创建留言

**路径**: `POST /v1/guestbook`

**认证**: 需要登录（支持访客登录）

**请求体**:
```json
{
  "content": "string",  // 留言内容（必填，最大100字符）
  "nickname": "string"  // 昵称（可选）
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
    "nickname": "string",
    "content": "string",
    "status": "APPROVED",
    "createdAt": "2024-01-01T00:00:00"
  },
  "traceId": "string"
}
```

---

### 2. 获取留言列表

**路径**: `GET /v1/guestbook`

**查询参数**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | number | 0 | 页码 |
| size | number | 50 | 每页数量 |

**成功响应** (code=0):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "string",
        "userId": "string",
        "nickname": "string",
        "content": "string",
        "status": "APPROVED",
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "totalPages": 10,
    "totalElements": 500
  },
  "traceId": "string"
}
```

---

### 3. 删除留言

**路径**: `DELETE /v1/guestbook/{id}`

**认证**: 需要登录（管理员或留言作者）

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 留言ID |

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

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 1001 | 参数错误 |
| 1002 | 用户名已存在 |
| 1003 | 邮箱已存在 |
| 1004 | 用户名或密码错误 |
| 1005 | 验证码错误或已过期 |
| 1006 | Token无效或已过期 |
| 1007 | 无权限访问 |
| 1008 | 账户已被禁用 |
| 1009 | 验证码发送过于频繁 |
| 401 | 未认证（需要登录） |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

---

## 接口文件清单

| 文件路径 | 模块 | 接口数量 |
|----------|------|----------|
| `src/api/auth.ts` | 认证 | 7 |
| `src/api/article.ts` | 文章 | 10 |
| `src/api/comment.ts` | 评论 | 4 |
| `src/api/reaction.ts` | 点赞 | 2 |
| `src/api/profile.ts` | 用户资料 | 3 |
| `src/api/about.ts` | 关于页面 | 2 |
| `src/api/guestbook.ts` | 留言板 | 3 |

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.3 | 2026-06-14 | 修改密码接口（邮箱+验证码）、移除访客登录、文章分类标签增删改仅管理员可用、移除问答模块 |
| v1.2 | 2026-06-13 | 新增发送验证码、刷新Token、修改签名接口 |
| v1.1 | 2026-06-12 | 新增留言板模块接口 |
| v1.0 | 2026-06-11 | 初始版本 |
