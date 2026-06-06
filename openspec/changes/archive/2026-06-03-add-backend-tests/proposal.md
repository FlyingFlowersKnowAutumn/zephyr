## Why

项目 4 个微服务模块仅有 1 个单元测试（SensitiveWordFilterTest），user-service 甚至未引入 `spring-boot-starter-test` 依赖。所有业务逻辑无测试覆盖，重构和迭代风险极高。需补齐核心 Service 层的单元测试。

## What Changes

- `user-service/pom.xml` 添加 `spring-boot-starter-test` + H2 依赖
- 新增 `UserServiceTest`：注册、登录、getUserById、convertToDTO
- 新增 `UserProfileServiceTest`：getProfile（含空 profile）、updateProfile（upsert）
- 新增 `AboutServiceTest`：getAbout（默认值/已配置）、saveAbout（博主/非博主校验）
- 新增 `JwtServiceTest`：token 生成、解析、过期校验
- 新增 `ArticleServiceTest`：createArticle、slug 生成、searchByTsVector
- 新增 `CommentServiceTest`：创建评论、敏感词过滤
- 新增 `QuestionServiceTest`：创建问题、回答管理

## Capabilities

### New Capabilities

- `backend-unit-tests`: user-service 和 article-service 核心 Service 层单元测试，interaction-service 补充 Service 测试

### Modified Capabilities

_（无现有 specs）_

## Impact

- **user-service/pom.xml**: 新增 test 依赖（H2 + spring-boot-starter-test）
- **user-service/src/test/**: 新增 4 个测试类
- **article-service/src/test/**: 新增 1 个测试类
- **interaction-service/src/test/**: 新增 2 个测试类
