## ADDED Requirements

### Requirement: 全局暗色模式切换
系统 SHALL 支持亮色/暗色双主题，用户可手动切换，默认跟随系统偏好。

#### Scenario: 首次访问跟随系统偏好
- **WHEN** 用户首次访问且系统设置为暗色模式
- **THEN** 网站自动使用暗色主题

#### Scenario: 手动切换暗色模式
- **WHEN** 用户点击暗色模式切换按钮
- **THEN** 网站立即切换为亮色主题，偏好保存到 localStorage

#### Scenario: 刷新后保持偏好
- **WHEN** 用户已手动选择亮色模式并刷新页面
- **THEN** 网站保持亮色模式，不跟随系统偏好

### Requirement: CSS Variables 主题系统
系统 SHALL 使用 CSS Variables 定义主题色，切换暗色模式时仅修改 `:root` 的 CSS 类。

#### Scenario: 主题色切换
- **WHEN** 暗色模式切换
- **THEN** `<html>` 元素的 `dark` class 被切换，所有组件的颜色通过 CSS Variables 自动响应

### Requirement: 暗色模式切换按钮
系统 SHALL 在页面固定位置提供暗色模式切换按钮。

#### Scenario: 切换按钮图标
- **WHEN** 当前为暗色模式
- **THEN** 按钮显示太阳图标（☀️），提示切换到亮色

#### Scenario: 切换按钮位置
- **WHEN** 页面加载
- **THEN** 切换按钮固定在页面右下角或导航栏内
