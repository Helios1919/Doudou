# Doudou AI - 智能AI助手

一个简约风格的Android AI聊天应用，基于豆包大模型，支持联网搜索。纯前端+WebView架构，无需后台服务器。

## 功能特性

- 🤖 **AI对话** - 基于豆包大模型的智能聊天，支持上下文记忆
- 🌐 **联网搜索** - 一键开启联网搜索，结合实时信息回答问题
- ⚙️ **灵活配置** - 可自定义API密钥和模型端点
- 📱 **移动优化** - 响应式设计，适配手机屏幕
- 🎨 **简约UI** - 橙色主题，圆角气泡，流畅动画

## 技术栈

- **前端**: HTML5 + CSS3 + JavaScript（内嵌于WebView）
- **容器**: Android WebView
- **桥接**: Java JavascriptInterface（JS调用原生HTTP请求）
- **API**: 火山引擎豆包大模型 + DuckDuckGo搜索

## 快速开始

### 前置要求

- Android Studio Hedgehog+
- Android SDK 34
- Gradle 8.3
- JDK 21

### 构建

```bash
git clone <repository-url>
cd doudou
gradle assembleRelease
```

### 安装

```bash
release/app-release.apk
```

直接安装到Android手机即可使用。

## 使用说明

1. **对话** - 在底部输入框输入问题，点击发送
2. **联网搜索** - 点击顶栏 🌐 按钮开启/关闭
3. **设置** - 点击 ⚙ 配置API密钥和模型
4. **清空** - 点击 ↻ 清空聊天记录

## API配置

应用内置默认API密钥，开箱即用。如需自定义：

1. 在火山引擎控制台创建推理接入点（在线推理类型）
2. 获取API Key和Endpoint ID
3. 在APP设置中填入

## 项目结构

```
doudou/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   └── index.html          # 前端界面
│   │   ├── java/com/example/doudou/
│   │   │   ├── bridge/
│   │   │   │   └── ApiBridge.java  # JS桥接（API调用/搜索/设置）
│   │   │   └── MainActivity.java   # WebView容器
│   │   └── res/                    # 资源文件
│   └── build.gradle
├── release/                        # 发布APK
├── gradle/
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 许可证

MIT License