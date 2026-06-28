## ADDED Requirements

### Requirement: user-service 业务逻辑测试覆盖
系统 SHALL 为 user-service 的 UserService、UserProfileService、AboutService、JwtService 提供单元测试，覆盖核心正常路径和边界情况。

#### Scenario: UserService 注册成功
- **WHEN** 使用未占用的 username 和 email 调用 register
- **THEN** 返回 UserDTO 且 userRepository.save 被调用

#### Scenario: UserService 注册用户名冲突
- **WHEN** 使用已存在的 username 调用 register
- **THEN** 抛出 BusinessException（conflict）

#### Scenario: UserService 登录成功
- **WHEN** 提供正确的 username 和 password
- **THEN** 返回 LoginResponseDTO 包含 token、refreshToken、user

#### Scenario: UserService 登录密码错误
- **WHEN** 提供错误的 password
- **THEN** 抛出 BusinessException（unauthorized）

#### Scenario: UserProfileService 查看不存在的用户资料
- **WHEN** 查询不存在的 userId
- **THEN** 抛出 BusinessException（notFound）

#### Scenario: UserProfileService upsert 更新资料
- **WHEN** 首次更新某用户的 profile
- **THEN** 自动创建 UserProfile 记录并返回合并后的 UserProfileDTO

#### Scenario: AboutService 博主未配置时返回默认值
- **WHEN** 查询关于页但无博主或配置不存在
- **THEN** 返回 content=""、skills=[]、socialLinks="{}"

#### Scenario: AboutService 非博主编辑被拒绝
- **WHEN** 非 blogger 角色的用户尝试保存关于页
- **THEN** 抛出 BusinessException（forbidden）

#### Scenario: JwtService 生成和解析 token
- **WHEN** 使用固定 secret 生成 token 后解析
- **THEN** 解析出的 userId 与原始一致

#### Scenario: JwtService 过期 token
- **WHEN** 解析已过期的 token
- **THEN** 返回 null

### Requirement: article-service 业务逻辑测试覆盖
系统 SHALL 为 article-service 的 ArticleService 提供单元测试。

#### Scenario: 创建文章自动生成 slug
- **WHEN** 创建文章未提供 slug
- **THEN** 根据 title 自动生成唯一 slug 并保存

#### Scenario: 全文搜索委托 Repository
- **WHEN** keyword 参数不为空
- **THEN** 调用 articleRepository.searchByTsVector 而非 findAll

### Requirement: interaction-service 业务逻辑测试补充
系统 SHALL 为 interaction-service 的 CommentService 和 QuestionService 提供单元测试。

#### Scenario: 评论内容敏感词过滤
- **WHEN** 创建包含敏感词的评论
- **THEN** 敏感词被替换为 ** 后保存

#### Scenario: 创建回复评论
- **WHEN** 创建带 parentId 的评论
- **THEN** 保存为子评论并关联父评论
