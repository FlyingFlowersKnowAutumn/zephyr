## 1. 环境准备

- [x] 1.1 `user-service/pom.xml` 添加 `spring-boot-starter-test` 依赖

## 2. user-service 测试

- [x] 2.1 创建 `JwtServiceTest`：token 生成/解析/过期
- [x] 2.2 创建 `UserServiceTest`：注册/登录/冲突/密码错误/convertToDTO
- [x] 2.3 创建 `UserProfileServiceTest`：查询/不存在/upsert 创建/更新
- [x] 2.4 创建 `AboutServiceTest`：默认值返回/保存/非博主拒绝

## 3. article-service 测试

- [x] 3.1 创建 `ArticleServiceTest`：创建文章/slug 自动生成/全文搜索分支

## 4. interaction-service 测试

- [x] 4.1 创建 `CommentServiceTest`：创建评论/回复/敏感词过滤
- [x] 4.2 创建 `QuestionServiceTest`：创建问题/添加回答
