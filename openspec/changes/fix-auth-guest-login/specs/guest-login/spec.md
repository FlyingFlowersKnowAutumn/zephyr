## ADDED Requirements

### Requirement: 免密登录

系统 SHALL 提供免密登录端点，用户通过邮箱和验证码即可登录。

#### Scenario: 已注册用户免密登录成功

- **WHEN** 用户提交已注册的邮箱和正确验证码
- **THEN** 系统校验验证码通过，生成 JWT，返回 LoginResponseDTO（token + 用户信息）

#### Scenario: 未注册用户免密登录自动注册

- **WHEN** 用户提交未注册的邮箱和正确验证码
- **THEN** 系统自动创建账号（默认用户名 "Vent" + 4 位随机数，密码随机 UUID，头像 "/Note/mm.jpg"，角色 "user"），生成 JWT，返回 LoginResponseDTO

#### Scenario: 验证码错误

- **WHEN** 用户提交的验证码与 Redis 中存储的不匹配或已过期
- **THEN** 系统返回错误 "验证码错误或已过期"

#### Scenario: 修改密码端点可访问

- **WHEN** 用户提交 `PUT /api/v1/auth/password` 请求（无需认证）
- **THEN** 网关放行，系统处理密码重置请求
