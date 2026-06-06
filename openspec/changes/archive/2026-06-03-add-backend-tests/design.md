## Context

项目基于 Spring Boot 3.2.5 + JUnit 5（spring-boot-starter-test 内含 Mockito、AssertJ）。user-service 缺少 test 依赖需补齐。数据库为 PostgreSQL，测试中使用 H2 内存库避免外部依赖。现有 1 个测试（SensitiveWordFilterTest）展示了 JUnit 5 + @Test 的纯单元测试模式。

## Goals / Non-Goals

**Goals:**
- 覆盖 user-service 核心 Service：UserService、UserProfileService、AboutService、JwtService
- 覆盖 article-service：ArticleService
- 覆盖 interaction-service：CommentService、QuestionService（补充）
- 使用 @ExtendWith(MockitoExtension.class) 纯 Mock 模式，不启动 Spring 容器

**Non-Goals:**
- Controller 层 @WebMvcTest 集成测试
- Repository 层 @DataJpaTest
- 端到端集成测试
- 前端测试

## Decisions

### 1. 纯 Mockito 单元测试，不启动 Spring 容器

**决策**: 使用 `@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks`，不加载 ApplicationContext。

**理由**: Service 层逻辑不依赖 Spring 容器，纯 Mock 测试速度快（毫秒级），适合 CI 快速反馈。Controller/Repository 测试留待后续阶段。

**替代方案**: @SpringBootTest + @MockBean — 启动完整容器，速度慢、维护成本高，对于 Service 单元测试是过度工程。

### 2. JwtService 使用固定密钥测试

**决策**: 测试中通过反射或构造函数注入固定 secret，验证 token 生成/解析/过期逻辑。

**理由**: JwtService 依赖 application.yml 中的 `jwt.secret`，纯 Mock 测试环境无配置。通过 setter 注入 secret 绕过。

### 3. PasswordEncoder 直接 mock

**决策**: 测试 UserService 时 mock PasswordEncoder，不验证真实加密。

**理由**: BCrypt 是外部库行为，不属于业务逻辑测试范围。mock encode/matches 返回值即可验证业务分支。

### 4. 分类组织测试文件

**决策**: 测试类放在对应 Service 包的同级 `src/test/java` 目录下，命名 `{ClassName}Test`。

## Risks / Trade-offs

- **Mock 参数不匹配风险**: 如果 Service 重构改变了 Repository 方法签名，测试编译期就会发现，风险可控
- **未覆盖 SQL 查询**: Repository 层和真实 SQL 未测试。复杂查询（如 tsvector 全文搜索）的 SQL 错误需集成测试捕获，留待后续
