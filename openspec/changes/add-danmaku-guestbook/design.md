## Context

现有 `GuestbookView.vue` 将留言数据库中的消息作为弹幕显示，但实现简单：随机颜色文字、随机顶部分布（可能重叠）、随机速度。后端 `Guestbook` 实体无头像字段。

架构约束：前端 Vue 3 + GSAP（已引入），后端 Spring Boot + JPA，gateway 传递 X-User-Id/X-User-Name 头。

## Goals / Non-Goals

**Goals:**
- 弹幕携带用户头像（创建时固化）+ 文字内容，透明黑底白字 pill 样式
- 轨道调度：纵向分固定轨道，弹幕不重叠
- Wave 分批释放：历史弹幕作为 Wave 1，新发射弹幕在当前 Wave 结束后作为下一波
- 速度公式：文本越长速率越快
- hover 悬停暂停动画，移开继续
- 博主和普通用户可发射，游客不可
- 移除硬编码初始弹幕和定时随机弹幕

**Non-Goals:**
- 不改造个人中心"留言与评论"左右分栏（后续 change）
- 不支持弹幕颜色自定义（统一白字黑底）
- 不引入 WebSocket 实时同步（刷新页面才看到新弹幕）

## Decisions

### 1. 头像获取：创建时前端直传

**选择**: `GuestbookCreateDTO` 新增可选 `avatarUrl` 字段，前端从 `authStore.user.avatarUrl` 获取，随请求发送，后端直接存入。

**备选方案**: interaction-service 跨服务调用 user-service 获取头像 → 对个人博客过度设计。

**理由**: 请求已通过 gateway JWT 认证，用户只能传自己的头像。实现最简。

### 2. 轨道调度：Lane + occupiedUntil + animationend 事件

**选择**: 纵向划分 16 条固定轨道（百分比定位），每条轨道记录 `occupiedUntil` 时间戳。弹幕分配空闲轨道，CSS animation 驱动位移。`animationend` 事件触发时释放轨道并检查队列。

**理由**: 
- `animationend` 天然匹配 CSS animation 生命周期，hover 暂停时不触发
- 百分比定位自适应不同屏幕高度
- 无需 JS 定时器轮询，性能好

### 3. 速度公式

```
duration = baseDuration - textLength * speedFactor
           (clamped: minDuration ≤ duration ≤ baseDuration)

  例: baseDuration=10s, speedFactor=0.3, minDuration=4s
  "你好" (2字)  → 9.4s  慢
  "欢迎留言" (4字) → 8.8s
  "这是一条很长很长很长很长的弹幕" (14字) → 5.8s  快
```

**理由**: 长文本占用更宽空间，需要更快速度保证视觉流畅。

### 4. Wave 分批释放

```
Wave 1: 页面加载 → 历史 guestbook 消息 → 依次分配轨道
        全部轨道空闲 → 检查队列 → 有则 Wave 2
Wave 2: 队列中所有待发射弹幕 → 一并释放
        （队列在 Wave 期间收集新发射的弹幕）
        全部空闲 → 检查队列 → Wave 3 → ...
```

**理由**: 连续发射的弹幕成批出现、成批消失，视觉上比零散弹幕更有节奏感。

### 5. hover 暂停

CSS: `.danmaku-item:hover { animation-play-state: paused; }`

暂停期间 `animationend` 不触发，轨道保持占用。移开后动画继续，结束后正常释放。无需额外 JS 逻辑。

### 6. 弹幕视觉结构

```
┌──────────────────────────────────┐
│ ┌────┐                           │  ← rgba(0,0,0,0.6) 背景
│ │    │  欢迎来到留言板！           │  ← border-radius: 20px
│ │ 👤 │                           │  ← padding: 6px 16px 6px 6px
│ └────┘                           │
└──────────────────────────────────┘
  ↑                                ↑
  28px 圆形头像                   白色字体 14px
```

## Risks / Trade-offs

- **头像变更不同步**: 创建时固化的 avatarUrl 不会随用户换头像更新。这是预期的——历史留言保留当时头像
- **轨道数量**: 16 条轨道在小屏（<768px）可能显密，可响应式减少到 10 条
- **动画性能**: 大量弹幕 + `animation-play-state` + 事件监听，需注意。16 条轨道上限限制同时弹幕数量
