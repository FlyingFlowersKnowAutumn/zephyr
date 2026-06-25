## 后端核心技术亮点

### 一、JWT 认证系统（核心亮点）

**技术实现：**
- **非对称加密**：采用 RS256 算法，私钥签名、公钥验签，杜绝 Token 伪造风险
- **双 Token 机制**：
  - **Access Token**：15 分钟有效期，用于日常接口认证
  - **Refresh Token**：7 天有效期，存储于 Redis，支持安全刷新

**安全设计：**
- Token 携带用户角色信息，支持细粒度权限控制
- 集成网关过滤器，实现统一认证与白名单管理

---

### 二、Redis 实战应用

**核心场景：**
- **限流防护**：基于 Redis 实现分布式限流，登录接口 5 次/分钟/IP，防暴力破解
- **游客 Session**：临时用户数据存储，支持 24 小时过期自动清理
- **缓存优化**：Session 续期策略，提升用户体验

**技术细节：**
- 使用 RedisTemplate 进行高效数据操作
- 配合 Gateway 过滤器实现请求计数与拦截

---

### 三、RBAC 权限管理系统

**设计架构：**
- **角色分级**：guest（游客）→ user（普通用户）→ blogger（博主）
- **动态权限**：基于角色的访问控制，不同角色拥有不同操作权限

**实现效果：**
- 路由守卫配合后端鉴权，确保接口安全
- 游客可浏览但无法评论/点赞，博主拥有完整管理权限

---

### 四、Flyway 数据迁移

**版本管理：**
- **V1__init_user_db.sql**：初始化用户核心表（users、user_profiles、about_config）
- **V2__add_rbac_tables.sql**：扩展 RBAC 权限表结构

**优势：**
- 自动化迁移，支持多环境同步
- 回滚机制完善，确保数据安全

---

### 五、微服务架构设计

**服务拆分：**
- **api-gateway**：统一入口、路由转发、全局过滤器
- **user-service**：用户认证、权限管理、Profile 服务
- **article-service**：文章 CRUD、全文搜索
- **interaction-service**：评论、点赞、访客留言
- **blog-common**：公共 DTO、异常处理、工具类

**服务治理：**
- **Nacos**：服务注册发现 + 配置中心
- **Spring Cloud Gateway**：限流、认证、跨域处理

---

### 六、数据库设计与优化

**表结构设计：**
```sql
-- 用户表核心设计
users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    role VARCHAR(20) DEFAULT 'user',
    is_guest BOOLEAN DEFAULT false,
    guest_expires_at TIMESTAMPTZ
)
```

**索引策略：**
- idx_users_username：加速用户名查询
- idx_users_email：加速邮箱登录
- idx_users_guest_expires：优化游客过期清理

---

### 七、安全防护体系

| 防护层 | 实现方式 | 效果 |
|--------|----------|------|
| **认证** | JWT + 网关过滤器 | 统一鉴权，白名单放行 |
| **限流** | Redis + 滑动窗口 | 防暴力破解，5次/分钟 |
| **密码** | bcrypt 10轮哈希 | 不可逆加密，防彩虹表 |
| **权限** | RBAC 角色控制 | 细粒度访问控制 |

---

### 八、全文搜索系统

**技术难点：**
- **PostgreSQL 全文搜索**：使用 tsvector/tsquery 实现高效文本检索
- **搜索向量预计算**：通过触发器自动更新文章搜索向量
- **模糊匹配**：支持前缀匹配、同义词扩展

**核心实现：**
```sql
-- 搜索向量字段
ALTER TABLE articles ADD COLUMN search_vector tsvector;
-- 创建索引
CREATE INDEX idx_articles_search ON articles USING gin(search_vector);
```

**亮点：**
- 支持标题、内容、标签多字段联合检索
- 性能优化：避免实时计算，采用预计算策略

---

### 九、分页查询优化

**技术难点：**
- **大数据量分页**：解决 OFFSET 深分页性能问题
- **游标分页**：基于 ID 范围的高效分页方案
- **分页元数据**：总数、页码、每页数量的精准计算

**核心实现：**
```java
// 游标分页查询
@Query("SELECT a FROM Article a WHERE a.id < :lastId ORDER BY a.id DESC LIMIT :size")
List<Article> findByCursor(Long lastId, int size);
```

**亮点：**
- 支持传统分页与游标分页两种模式
- 优化 COUNT 查询，避免全表扫描

---

### 十、文章发布系统

**技术难点：**
- **Markdown 解析**：支持代码高亮、表格、公式渲染
- **富文本存储**：HTML 转义与安全过滤
- **Slug 生成**：SEO 友好的 URL 路径

**核心实现：**
```java
// Slug 生成策略
public String generateSlug(String title) {
    return title.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-");
}
```

**亮点：**
- 支持草稿保存与定时发布
- 文章版本管理与历史记录

---

### 十一、弹幕留言系统

**技术难点：**
- **WebSocket 实时推送**：实现弹幕实时展示
- **消息队列**：异步处理高并发弹幕请求
- **弹幕样式**：支持颜色、速度、位置多样化

**核心架构：**
```
[前端] ──WebSocket──> [Gateway] ──MQ──> [interaction-service] ──广播──> [所有在线用户]
```

**亮点：**
- 支持实时互动，提升用户参与感
- 弹幕去重与频率限制

---

### 十二、评论系统

**技术难点：**
- **多级评论**：支持树形结构展示回复关系
- **敏感词过滤**：基于字典的内容审查
- **评论排序**：时间倒序、点赞数排序

**数据库设计：**
```sql
CREATE TABLE comments (
    id UUID PRIMARY KEY,
    article_id UUID REFERENCES articles(id),
    parent_id UUID REFERENCES comments(id),
    content TEXT NOT NULL,
    likes INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_comments_article ON comments(article_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);
```

**亮点：**
- 支持@用户提醒功能
- 评论审核与回复通知

---

### 十三、点赞功能

**技术难点：**
- **Redis 计数**：高性能点赞统计
- **去重机制**：防止重复点赞
- **数据一致性**：Redis 与数据库同步策略

**核心实现：**
```java
// 点赞操作
public void like(String userId, String targetId) {
    String key = "like:" + targetId;
    Boolean isNew = redisTemplate.opsForSet().add(key, userId);
    if (Boolean.TRUE.equals(isNew)) {
        redisTemplate.opsForValue().increment("like_count:" + targetId);
    }
}
```

**亮点：**
- O(1) 时间复杂度的点赞操作
- 支持点赞状态实时查询

---

### 十四、归档与分类系统

**技术难点：**
- **时间归档**：按年月聚合文章
- **分类层级**：支持多级分类结构
- **标签云**：动态权重计算

**核心查询：**
```sql
-- 按年月归档
SELECT DATE_TRUNC('month', created_at) as month, COUNT(*) as count
FROM articles GROUP BY month ORDER BY month DESC;
```

**亮点：**
- 分类与标签支持增删改查
- 文章批量分类管理

---

### 十五、信息统计系统

**技术难点：**
- **数据聚合**：文章数、评论数、访问量统计
- **缓存策略**：定期刷新统计数据
- **趋势分析**：按时间维度的数据变化

**核心设计：**
```java
// 统计缓存刷新
@Scheduled(cron = "0 0 * * * ?")
public void refreshStats() {
    long articleCount = articleRepository.count();
    long commentCount = commentRepository.count();
    redisTemplate.opsForValue().set("stats:articles", articleCount);
    redisTemplate.opsForValue().set("stats:comments", commentCount);
}
```

**亮点：**
- 实时统计与历史趋势展示
- 支持多维度数据报表

---

### 技术栈总结

| 分类 | 技术 | 版本 | 亮点 |
|------|------|------|------|
| 语言 | Java | 21 | LTS 版本，性能优化 |
| 框架 | Spring Boot | 3.2.x | 最新稳定版 |
| 微服务 | Spring Cloud | 2023.0.x | 云原生架构 |
| 服务治理 | Nacos | 2.3.x | 配置中心 + 服务发现 |
| 数据库 | PostgreSQL | 14.x | 全文搜索支持 |
| 缓存 | Redis | 7.x | 限流 + Session + 点赞计数 |
| 认证 | JWT | RS256 | 非对称加密 |
| 迁移 | Flyway | 9.x | 版本化管理 |
| 实时通信 | WebSocket | - | 弹幕实时推送 |

---

### 后端技能亮点提炼

1. **认证安全**：JWT 非对称加密 + 双 Token + Redis Session 管理
2. **限流防护**：分布式限流策略，多维度规则配置
3. **权限架构**：RBAC 角色分级，动态权限控制
4. **数据迁移**：Flyway 版本控制，自动化部署
5. **微服务设计**：5 模块拆分，高内聚低耦合
6. **全文搜索**：PostgreSQL 全文索引，搜索向量优化
7. **实时互动**：WebSocket 弹幕推送，消息队列异步处理
8. **数据统计**：定时任务 + Redis 缓存，高效数据聚合