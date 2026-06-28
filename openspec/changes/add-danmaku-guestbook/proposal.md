## Why

现有留言板（GuestbookView）仅有基础弹幕效果——彩色文字随机飘过、可能重叠、无头像、速度随机、无背景样式。需要将其改造为完整的弹幕系统：携带用户头像、透明黑底白字、轨道调度不重叠、速率随文本长度变化、hover 暂停、Wave 分批释放。

## What Changes

- Guestbook 实体新增 `avatarUrl` 字段，创建时固化头像 URL
- 前端弹幕系统重写：轨道调度器、Wave 分批释放、animation 生命周期管理
- 弹幕视觉：24px 圆形头像 + 内容文字，rgba(0,0,0,0.6) 圆角背景 pill，白色字体
- 速度公式：文本越长速率越快
- 交互：hover 暂停、聚焦显示发射按钮、未登录跳转登录
- 移除初始硬编码弹幕和定时随机弹幕

## Capabilities

### New Capabilities

- `danmaku-system`: 基于轨道调度的弹幕系统，包含头像携带、Wave 分批释放、hover 暂停、速度自适应、输入发射交互
- `guestbook-avatar`: Guestbook 消息创建时固化用户头像 URL，返回数据包含 avatarUrl

### Modified Capabilities

（无现有 capability 需要修改）

## Impact

| 组件 | 文件 | 影响 |
|------|------|------|
| 后端 Entity | `Guestbook.java` | 新增 avatarUrl 字段 |
| 后端 DTO | `GuestbookDTO.java`, `GuestbookCreateDTO.java` | 新增 avatarUrl |
| 后端 Service | `GuestbookService.java` | 创建时存入 avatarUrl |
| 后端 DB | 新 migration 文件 | ALTER TABLE guestbook ADD avatar_url |
| 前端页面 | `GuestbookView.vue` | 重写弹幕系统 |
| 前端类型 | `types/index.ts` | GuestbookDTO 新增 avatarUrl |
