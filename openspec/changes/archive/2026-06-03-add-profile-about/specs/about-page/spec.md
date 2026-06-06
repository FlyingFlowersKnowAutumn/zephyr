## ADDED Requirements

### Requirement: 查看关于页
系统 SHALL 提供公开 API 返回博主关于页配置，包含自我介绍内容、技能列表、社交链接。

#### Scenario: 博主已配置关于页
- **WHEN** 客户端请求 `GET /api/v1/about`
- **THEN** 系统返回 AboutDTO（content, skills[], socialLinks{}），code=0

#### Scenario: 博主未配置关于页
- **WHEN** 客户端请求 `GET /api/v1/about` 且无博主配置
- **THEN** 系统返回默认空 AboutDTO（content=""，skills=[]，socialLinks={}），code=0

### Requirement: 编辑关于页
系统 SHALL 允许博主（role=blogger）编辑关于页配置。首次编辑自动创建记录。

#### Scenario: 博主编辑关于页
- **WHEN** 博主 PUT `/api/v1/about` 携带有效 token
- **THEN** 系统保存 content/skills/socialLinks，返回更新后的 AboutDTO

#### Scenario: 非博主编辑
- **WHEN** 非博主角色用户请求编辑
- **THEN** 系统返回 code=403，message="仅博主可编辑"

#### Scenario: 未登录访问
- **WHEN** 未携带有效 token 请求编辑
- **THEN** 系统返回 code=401
