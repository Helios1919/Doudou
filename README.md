# Doudou AI - 信息检索聚合AI智能体

Doudou AI 是一个信息检索聚合 AI 智能体。它先通过搜索引擎实时检索互联网信息，再将检索结果聚合整理后交由大模型进行理解与归纳，最终输出结构化的回答。整个过程完全在 Android 设备本地完成，无需后台服务器。

## 工作流程

```
用户提问 → DuckDuckGo 实时检索 → 提取摘要与相关条目 → 豆包大模型理解归纳 → 结构化回答
```

在检索增强模式下，系统会将搜索结果作为上下文注入 AI 提示词，让回答兼具实时性与准确性。

## 功能特性

- **AI 对话** — 基于豆包大模型，支持多轮上下文记忆（最近 10 轮对话）
- **联网检索** — 一键开启，实时搜索互联网信息，由 AI 聚合后回答
- **检索降级** — 搜索失败时自动回退到普通对话模式，不影响使用
- **灵活配置** — 可自定义 API 密钥和模型端点
- **连接测试** — 设置面板内置连接诊断，快速验证 API 可用性
- **移动优化** — 响应式设计，适配手机屏幕，支持安全区域

## 技术架构

```
┌─────────────────────────────────┐
│         Android WebView         │
│  ┌───────────────────────────┐  │
│  │    index.html (前端界面)    │  │
│  │    HTML + CSS + JS        │  │
│  └──────────┬────────────────┘  │
│             │ JavascriptInterface│
│  ┌──────────▼────────────────┐  │
│  │    ApiBridge.java          │  │
│  │  ┌──────┐ ┌────────────┐  │  │
│  │  │ 豆包  │ │ DuckDuckGo │  │  │
│  │  │ API  │ │  搜索 API  │  │  │
│  │  └──────┘ └────────────┘  │  │
│  └───────────────────────────┘  │
│         Android 原生层           │
└─────────────────────────────────┘
```

- **前端**: HTML5 + CSS3 + JavaScript，内嵌于 WebView
- **JS 桥接**: Java `@JavascriptInterface` — JS 调用原生层发起 HTTP 请求（绕过 CORS）
- **AI 引擎**: 火山引擎豆包大模型 (OpenAI 兼容 Chat Completions API)
- **搜索引擎**: DuckDuckGo Instant Answer API
- **数据持久化**: Android SharedPreferences

## 项目结构

```
doudou/
├── app/src/main/
│   ├── assets/
│   │   └── index.html              # 前端界面与交互逻辑
│   ├── java/com/example/doudou/
│   │   ├── bridge/
│   │   │   └── ApiBridge.java      # JS 桥接层（API调用 / 搜索 / 设置）
│   │   └── MainActivity.java       # WebView 容器
│   └── res/                        # Android 资源文件
├── gradle/
├── release/                        # 发布产物目录
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- Android SDK 34
- Gradle 8.3
- JDK 21

### 构建

```bash
git clone https://github.com/<your-username>/doudou.git
cd doudou
./gradlew assembleRelease
```

构建产物位于 `app/build/outputs/apk/release/`。

### 安装

将 APK 安装到 Android 手机即可使用。首次使用需在设置中配置 API 密钥。

## 配置说明

1. 在[火山引擎控制台](https://console.volcengine.com/ark)创建推理接入点（在线推理类型）
2. 获取 API Key
3. 打开 APP → 点击 ⚙ 设置 → 填入 API Key → 点击"测试连接"验证 → 保存

默认模型为 `doubao-pro-32k`，可根据需要修改为其他兼容 OpenAI Chat Completions 格式的模型。

## 许可证

MIT License