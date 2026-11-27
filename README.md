# Zéphyr · 个人技术博客

一个前后端分离的个人技术博客系统：**Vue 3 前端 + Spring Cloud 微服务后端**，自带 Markdown 写作、评论互动、标签/分类聚合、全文搜索，整站通过 Cloudflare Tunnel 对外提供服务。

- 线上站点：<https://zephyrlog.dpdns.org>
- 前端：`blog-frontend/`
- 后端：`blog-parent/`

## 功能

**阅读**

- 首页：Hero 视觉、公告、最新文章、侧边栏（个人信息 / 标签云 / 分类 / 归档 / 站点统计）
- 文章列表与详情：Markdown 渲染、代码高亮、目录 TOC、图片灯箱、上下篇导航
- 归档（按年月）、分类、标签三个聚合视图，支持 URL 深链接
- 全站搜索（`Ctrl / ⌘ + K` 唤起，匹配标题、摘要与标签）
- 留言板、关于页、404 页
- 暗色模式、响应式布局、Canvas 流星背景（尊重 `prefers-reduced-motion`）

**写作与互动**

- Markdown 编辑器：封面上传、正文插图、标签与分类选择、草稿 / 发布
- 文章管理：列表、编辑、删除、状态切换
- 评论（支持一级回复）、点赞、收藏
- 注册 / 登录 / 邮箱验证码免密登录 / 修改密码 / 个人中心（昵称、简介、头像）

**工程**

- 匿名可读、写操作需登录；网关统一 JWT 鉴权，客户端伪造的身份头会被剥离
- 网关按 IP 限流 + 登录失败锁定（IP 5 次/5 分钟、账号 10 次/15 分钟）
- `sitemap.xml` 与 `rss.xml` 动态生成
- 访问统计（文章浏览量、站点访问量、访客数）

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 前端 | Vue 3.4 · TypeScript 5 · Vite 5 · Pinia · Vue Router 4 · Element Plus · marked · DOMPurify · highlight.js · GSAP · SCSS |
| 后端 | Java 21 · Spring Boot 3.2.5 · Spring Cloud 2023.0.2 · Spring Cloud Gateway（WebFlux）· Spring Data JPA · Flyway · JJWT 0.12.5 |
| 注册中心 | Nacos 2.3.1 |
| 存储 | PostgreSQL 14 · Redis 7.2 |
| 部署 | Docker Compose · Nginx · Cloudflare Tunnel |

## 架构

```
浏览器
  │ HTTPS
  ▼
Cloudflare 边缘（Universal SSL）
  │ Cloudflare Tunnel
  ▼
cloudflared 容器
  │ http://nginx:80
  ▼
nginx ── 托管前端静态资源 + /api/** 与 /uploads/** 反向代理
  │
  ▼
api-gateway :8080 ── JWT 鉴权 / 限流 / 路由
  ├── user-service        :8002   认证、用户、文件上传
  ├── article-service     :8082   文章、分类、标签、sitemap / RSS
  └── interaction-service :8003   评论、点赞、收藏、留言
        │
        ├── PostgreSQL :5432   user_db / article_db / interaction_db
        └── Redis      :6379   缓存、验证码、限流计数、登录失败计数
```

端口全部只绑定 `127.0.0.1`，不对公网暴露：

| 容器 | 端口 |
| --- | --- |
| blog-nginx | 80 |
| blog-gateway | 8080 |
| blog-user / blog-article / blog-interaction | 内部 8002 / 8082 / 8003（不发布） |
| blog-postgres | 5432 |
| blog-redis | 6379 |
| blog-nacos | 8848 / 9848 |
| blog-cloudflared | 无（出站连接） |

## 目录结构

```
.
├── blog-parent/                后端多模块 Maven 工程
│   ├── blog-common/            公共 DTO、异常处理、分页工具、TraceId 过滤器
│   ├── api-gateway/            Spring Cloud Gateway：鉴权、限流、路由
│   ├── user-service/           用户、认证、验证码、文件上传
│   ├── article-service/        文章、分类、标签、sitemap / RSS
│   └── interaction-service/    评论、点赞、收藏、留言
├── blog-frontend/              Vue 3 前端（含 nginx.conf 与 Dockerfile）
├── docker/                     PostgreSQL 初始化脚本
├── scripts/                    构建 / 发布 / 备份 / 计数对账
├── .github/workflows/          CI：后端测试 + 前端构建
├── docker-compose.yml
└── .env.example
```

## 快速开始（Docker）

前置：Docker Desktop、JDK 21、Maven 3.9+、Node.js 18+

```bash
# 1. 配置环境变量
cp .env.example .env
#    编辑 .env，至少填写 POSTGRES_PASSWORD 与 JWT_SECRET

# 2. 构建后端（产出各模块 target/*.jar）
cd blog-parent && mvn clean install -DskipTests && cd ..

# 3. 构建前端（产出 blog-frontend/dist）
cd blog-frontend && npm ci && npm run build && cd ..

# 4. 构建镜像并启动
docker compose up -d --build

# 5. 访问
#    http://127.0.0.1
```

> 三个 Java 镜像的 Dockerfile 用的是 `COPY target/*.jar`，nginx 镜像用 `COPY dist/`。
> 所以**必须先本地构建出产物**，`docker compose build` 才有东西可复制。

## 环境变量

| 变量 | 必填 | 说明 |
| --- | :---: | --- |
| `POSTGRES_PASSWORD` | ✅ | 数据库密码 |
| `JWT_SECRET` | ✅ | JWT 签名密钥，建议 64 位随机串 |
| `BLOG_OWNER_EMAIL` | | 站点所有者邮箱：用该邮箱注册的账号自动获得 `blogger`（管理员）角色；留空则一律普通用户 |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | | SMTP 发信（注册、免密登录验证码）；QQ 邮箱需使用**授权码**而非登录密码 |
| `BLOG_BASE_URL` | | 站点对外地址，用于 sitemap / RSS 中的绝对链接 |
| `CORS_ALLOWED_ORIGINS` | | 允许跨域的来源，逗号分隔；前后端同源部署时留空即可 |
| `CLOUDFLARE_TUNNEL_TOKEN` | | 启用 Cloudflare Tunnel 时需要 |
| `LOGIN_LOCK_IP_MAX_FAILURES` 等 | | 登录锁定阈值，默认 IP 5 次 / 5 分钟、账号 10 次 / 15 分钟 |

**本仓库不含任何真实凭据**，所有敏感配置均通过环境变量注入；`.env` 已被 `.gitignore` 忽略。

## 本地开发（不走 Docker）

```bash
# 只启动依赖组件
docker compose up -d postgres redis nacos

# 后端：逐个服务启动（或在 IDE 中运行各 *Application）
cd blog-parent
mvn -pl api-gateway        spring-boot:run
mvn -pl user-service       spring-boot:run
mvn -pl article-service    spring-boot:run
mvn -pl interaction-service spring-boot:run

# 前端：5175 端口，已配置 /api 与 /uploads 代理到 8080
cd blog-frontend && npm run dev
```

## 测试与检查

```bash
cd blog-parent    && mvn test         # 35 个单元测试
cd blog-frontend  && npm run build    # vue-tsc 类型检查 + 生产打包
```

CI 配置见 `.github/workflows/ci.yml`。

## 运维脚本

| 脚本 | 用途 |
| --- | --- |
| `scripts/build.sh` | 一键构建后端 jar、前端 dist 与 Docker 镜像 |
| `scripts/release.sh` | 带质量门禁的发布：测试 → 类型检查 → 构建 → 备份 → 启动 → 健康检查 |
| `scripts/backup.sh` | 备份三个业务库与上传目录，并清理过期备份 |
| `scripts/reconcile-counts.ps1` | 校验并修复文章的评论数 / 点赞数（支持 `-DryRun` 只检查） |

## 说明

- 这是个人自建博客，代码公开供学习参考，未附带开源许可证。
- 生产部署建议：使用强随机的 `JWT_SECRET`、为数据库设置独立强密码、为站点开启 Cloudflare 的 Universal SSL 与「始终使用 HTTPS」。
