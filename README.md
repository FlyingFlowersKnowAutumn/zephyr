# Personal Tech Blog

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/java-21-blue.svg)](https://www.oracle.com/java/technologies/downloads/#java21)
[![Vue Version](https://img.shields.io/badge/vue-3.4-green.svg)](https://vuejs.org/)
[![Docker Ready](https://img.shields.io/badge/docker-ready-blue.svg)](https://docker.com/)

一个现代化的个人技术博客系统，采用微服务架构，支持文章发布、评论互动、分类标签等功能。

---

## 📋 目录

- [项目简介](#项目简介)
- [技术框架](#技术框架)
- [项目结构](#项目结构)
- [安装与配置](#安装与配置)
- [使用说明](#使用说明)
- [API接口](#api接口)
- [开发指南](#开发指南)
- [部署方式](#部署方式)
- [许可证](#许可证)

---

## 🌟 项目简介

### 项目背景

本项目是一个基于微服务架构的个人技术博客系统，旨在为开发者提供一个现代化、高性能的博客平台。

### 主要功能

| 模块 | 功能 |
|------|------|
| 用户系统 | 注册、登录、免密登录、密码重置、个人资料管理 |
| 文章管理 | 文章发布、编辑、删除、分类、标签、搜索 |
| 互动系统 | 评论、点赞、收藏、留言 |
| 站点功能 | 站点地图、关于页面、统计数据 |

### 目标用户

- 技术博主和开发者
- 希望搭建个人博客的用户
- 学习微服务架构的开发者

---

## 🛠️ 技术框架

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 前端框架 |
| TypeScript | 5.4+ | 类型安全 |
| Vite | 5.2+ | 构建工具 |
| Element Plus | 2.14+ | UI组件库 |
| Pinia | 2.1+ | 状态管理 |
| Vue Router | 4.3+ | 路由管理 |
| Axios | 1.6+ | HTTP客户端 |
| Marked | 18.0+ | Markdown解析 |
| Highlight.js | 11.11+ | 代码高亮 |

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 后端框架 |
| Spring Cloud | 2023.0.2 | 微服务治理 |
| Spring Cloud Alibaba | 2023.0.1.0 | 服务发现 |
| Java | 21 | 编程语言 |
| PostgreSQL | 14+ | 关系型数据库 |
| Redis | 7.2+ | 缓存与会话存储 |
| Nacos | 2.3.1 | 服务发现与配置中心 |
| Flyway | 9.22+ | 数据库迁移 |
| JJWT | 0.12.5 | JWT认证 |

### 基础设施

| 工具 | 说明 |
|------|------|
| Docker | 容器化部署 |
| Docker Compose | 编排工具 |
| Nginx | 反向代理 |

---

## 📁 项目结构

```
personal-blog/
├── blog-frontend/          # 前端应用
│   ├── public/             # 静态资源
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── components/     # 组件
│   │   ├── composables/    # 组合式函数
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # 状态管理
│   │   ├── views/          # 页面视图
│   │   └── utils/          # 工具函数
│   └── package.json
├── blog-parent/            # 后端微服务
│   ├── api-gateway/        # API网关
│   ├── article-service/    # 文章服务
│   ├── interaction-service/# 互动服务
│   ├── user-service/       # 用户服务
│   ├── blog-common/        # 公共模块
│   └── pom.xml
├── nacos/                  # Nacos配置
├── docker-compose.yml      # Docker Compose配置
└── .gitignore
```

---

## 🚀 安装与配置

### 环境要求

- **Java**: 21+
- **Node.js**: 18+
- **Docker**: 20+
- **Maven**: 3.8+

### 快速开始（Docker Compose）

1. **克隆项目**

```bash
git clone https://github.com/FlyingFlowersKnowAutumn/zephyr.git
cd personal-blog
```

2. **配置环境变量**

修改 `docker-compose.yml` 中的数据库密码：

```yaml
services:
  postgres:
    environment:
      POSTGRES_PASSWORD: your_password
```

修改各服务的 `application.yml` 中的数据库密码和邮箱配置：

```yaml
spring:
  datasource:
    password: your_password
  mail:
    password: your_email_token
```

3. **启动服务**

```bash
docker-compose up -d
```

4. **访问应用**

- 前端页面: `http://localhost`
- API网关: `http://localhost:8080`
- Nacos控制台: `http://localhost:8848/nacos`

### 本地开发

#### 后端启动

```bash
cd blog-parent
mvn clean install -DskipTests
cd api-gateway
mvn spring-boot:run
```

依次启动其他服务：

```bash
cd user-service && mvn spring-boot:run
cd article-service && mvn spring-boot:run
cd interaction-service && mvn spring-boot:run
```

#### 前端启动

```bash
cd blog-frontend
npm install
npm run dev
```

---

## 📖 使用说明

### 用户注册与登录

1. 访问首页，点击"登录"按钮
2. 选择"注册"，输入邮箱、用户名和密码
3. 使用邮箱验证码完成注册
4. 注册成功后自动登录

### 发布文章

1. 登录后点击"写文章"按钮
2. 填写文章标题、内容、分类和标签
3. 点击"发布"按钮

### 评论与互动

1. 在文章详情页查看评论
2. 登录用户可以发表评论、点赞、收藏文章
3. 支持访客留言功能

### 文章管理

1. 登录后点击"管理"按钮
2. 可以查看、编辑、删除自己的文章
3. 支持按分类、标签筛选文章

---

## 🔌 API接口

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/auth/register` | 用户注册 |
| POST | `/api/v1/auth/login` | 用户登录 |
| POST | `/api/v1/auth/guest-login` | 免密登录 |
| POST | `/api/v1/auth/refresh` | 刷新Token |
| POST | `/api/v1/auth/logout` | 退出登录 |

### 文章接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/articles` | 获取文章列表 |
| GET | `/api/v1/articles/{id}` | 获取文章详情 |
| POST | `/api/v1/articles` | 创建文章 |
| PUT | `/api/v1/articles/{id}` | 更新文章 |
| DELETE | `/api/v1/articles/{id}` | 删除文章 |

### 评论接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/comments` | 获取评论列表 |
| POST | `/api/v1/comments` | 创建评论 |
| PUT | `/api/v1/comments/{id}` | 更新评论 |
| DELETE | `/api/v1/comments/{id}` | 删除评论 |

---

## 💡 开发指南

### 代码规范

- **前端**: 遵循 Vue 官方代码风格指南
- **后端**: 遵循 Spring Boot 代码风格指南
- **Git提交**: 使用 Conventional Commits 规范

### 提交信息格式

```
feat: 添加文章搜索功能
fix: 修复评论列表分页问题
docs: 更新API文档
refactor: 重构用户服务
```

### 分支管理

- `main`: 主分支，稳定版本
- `develop`: 开发分支
- `feature/*`: 功能分支
- `fix/*`: 修复分支

---

## 📦 部署方式

### Docker Compose（推荐）

```bash
docker-compose up -d
```

### Kubernetes（生产环境）

```bash
kubectl apply -f k8s/
```

---

## 📜 许可证与知识产权声明

### 知识产权

**版权所有 (c) 2024 FlyingFlowersKnowAutumn**

本项目的所有源代码、文档、配置文件及相关资源均受中华人民共和国著作权法及国际版权公约保护。未经授权，禁止商用。

**作者**: FlyingFlowersKnowAutumn（飞花知秋）  
**邮箱**: 1822134823@qq.com  
**GitHub**: [FlyingFlowersKnowAutumn](https://github.com/FlyingFlowersKnowAutumn)

---

### 开源许可证

本项目采用 **MIT 许可证** 开源，详见 [LICENSE](LICENSE) 文件。

根据 MIT 许可证，你可以：
- 自由使用、复制、修改和分发本项目的代码
- 将本项目的代码用于非商业目的
- 在修改后的代码中保留原始版权声明

但必须遵守以下条件：
- 在所有副本或实质性部分中保留上述版权声明和本许可声明
- 本软件按"原样"提供，不提供任何明示或暗示的担保

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- GitHub Issues: [提交问题](https://github.com/your-username/personal-blog/issues)

---

*Made with ❤️ by [https://github.com/FlyingFlowersKnowAutumn/zephyr.git]

