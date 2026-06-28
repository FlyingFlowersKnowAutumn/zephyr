## 1. 后端 — Guestbook 头像字段

- [x] 1.1 新增 Flyway migration：`ALTER TABLE guestbook ADD COLUMN avatar_url VARCHAR(500)`
- [x] 1.2 `Guestbook` entity 新增 `avatarUrl` 字段（`@Column(name = "avatar_url")`）
- [x] 1.3 `GuestbookCreateDTO` 新增 `avatarUrl` 字段（可选）
- [x] 1.4 `GuestbookDTO` 新增 `avatarUrl` 字段
- [x] 1.5 `GuestbookService.createGuestbook()` 创建时存入 avatarUrl，`convertToDTO()` 返回 avatarUrl

## 2. 前端 — 弹幕组件视觉

- [x] 2.1 `DanmakuItem` 接口新增 `avatarUrl` 字段，移除 `color` 字段
- [x] 2.2 弹幕模板改为：28px 圆形头像 + 文字，背景 rgba(0,0,0,0.6)，圆角 pill，白色字体
- [x] 2.3 CSS animation：从右到左线性滚动，`animation-play-state: paused` on hover

## 3. 前端 — 轨道调度器

- [x] 3.1 实现 16 条固定轨道（百分比 `top`），每条轨道记录 `occupiedUntil`
- [x] 3.2 发射弹幕时找空闲轨道（`occupiedUntil < now`），分配并标记占用
- [x] 3.3 无空闲轨道时弹幕进入等待队列
- [x] 3.4 `animationend` 事件触发时释放轨道，检查队列 → 非空则下一波放出

## 4. 前端 — 速度与生命周期

- [x] 4.1 实现速度公式：`duration = baseDuration - textLength * speedFactor`（区间限制）
- [x] 4.2 移除硬编码 `initialDanmakus` 数组和定时随机弹幕逻辑
- [x] 4.3 页面加载时 GET /guestbook 历史数据，作为 Wave 1 发射

## 5. 前端 — 输入交互

- [x] 5.1 输入框聚焦时显示"发射"按钮，点击或回车发射
- [x] 5.2 已登录用户发射：POST /guestbook（携带 avatarUrl）→ 成功加入调度器
- [x] 5.3 未登录用户发射：跳转到 /login

## 6. 前端 — 类型更新

- [x] 6.1 `types/index.ts` 中 `GuestbookDTO` 新增 `avatarUrl?: string`
- [x] 6.2 `GuestbookCreateDTO` 新增 `avatarUrl?: string`

## 7. 验证

- [ ] 7.1 历史弹幕加载：进入页面后历史留言以 Wave 形式依次出现
- [ ] 7.2 发射弹幕：输入内容后点击发射，弹幕出现在屏幕上
- [ ] 7.3 不重叠：多条弹幕同时出现时分布在不同轨道
- [ ] 7.4 速度差异：长弹幕移动速度明显快于短弹幕
- [ ] 7.5 hover 暂停：鼠标悬停弹幕停止，移开继续
- [ ] 7.6 Wave 机制：连续发射多条弹幕，在历史波结束后一起出现
- [ ] 7.7 游客拦截：未登录点击发射跳转登录页
