## ADDED Requirements

### Requirement: 弹幕轨道调度
系统 SHALL 使用固定轨道（lane）机制管理弹幕位置，确保弹幕不重叠。每条轨道记录占用截止时间，弹幕结束后自动释放。

#### Scenario: 空闲轨道分配
- **WHEN** 有弹幕需要发射且存在空闲轨道
- **THEN** 系统将弹幕分配到该轨道，标记轨道占用

#### Scenario: 无空闲轨道时进入队列
- **WHEN** 有弹幕需要发射但所有轨道均被占用
- **THEN** 系统将该弹幕放入等待队列

#### Scenario: 全部轨道释放后放出队列
- **WHEN** 屏幕上所有弹幕动画结束且队列非空
- **THEN** 系统将队列中所有弹幕作为下一波（Wave）一并发射

### Requirement: 弹幕视觉样式
系统 SHALL 将每条弹幕渲染为透明黑底白字的圆角 pill 组件，前面携带 28px 圆形用户头像。

#### Scenario: 有头像的弹幕
- **WHEN** 弹幕数据包含有效 avatarUrl
- **THEN** 系统在弹幕左侧显示 28px 圆形头像，右侧显示文字，整体包裹在 rgba(0,0,0,0.6) 背景的圆角容器中

#### Scenario: 无头像的弹幕
- **WHEN** 弹幕数据的 avatarUrl 为空
- **THEN** 系统显示默认占位头像（用户名首字母或通用图标）

### Requirement: 速度自适应
系统 SHALL 根据弹幕文本长度调整动画速度，长文本速率更快。

#### Scenario: 短文本弹幕
- **WHEN** 弹幕文本少于 5 个字符
- **THEN** 动画时长接近 baseDuration（较慢）

#### Scenario: 长文本弹幕
- **WHEN** 弹幕文本超过 15 个字符
- **THEN** 动画时长接近 minDuration（较快）

### Requirement: hover 暂停
系统 SHALL 在鼠标悬停弹幕时暂停该弹幕的动画，移开后继续。

#### Scenario: 悬停暂停
- **WHEN** 用户将鼠标移到某条弹幕上
- **THEN** 该弹幕动画暂停（animation-play-state: paused），轨道保持占用

#### Scenario: 移开继续
- **WHEN** 用户将鼠标从弹幕上移开
- **THEN** 该弹幕动画继续（animation-play-state: running），结束后轨道正常释放

### Requirement: 弹幕发射交互
系统 SHALL 提供输入框和发射按钮，博主和普通用户可发射弹幕，游客不可。

#### Scenario: 输入聚焦显示发射按钮
- **WHEN** 用户点击输入框获得焦点
- **THEN** 输入框宽度收缩，右侧出现"发射"按钮

#### Scenario: 已登录用户发射弹幕
- **WHEN** 已登录用户输入内容后点击"发射"（或回车）
- **THEN** 系统调用 POST /guestbook 创建留言，成功后弹幕进入轨道或队列

#### Scenario: 未登录用户点击发射
- **WHEN** 未登录用户在输入框中点击"发射"
- **THEN** 系统跳转到登录页面

### Requirement: 历史弹幕加载
系统 SHALL 在页面加载时获取历史 guestbook 消息作为初始弹幕 Wave，不再使用硬编码数据。

#### Scenario: 页面加载显示历史弹幕
- **WHEN** 用户进入留言板页面
- **THEN** 系统请求 GET /guestbook 获取历史留言，转换为弹幕数据，作为 Wave 1 发射

#### Scenario: 无历史留言
- **WHEN** 历史留言为空
- **THEN** 屏幕空白，等待用户发射第一条弹幕
