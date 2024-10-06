# 基于Android平台的中国象棋对弈应用

## 简介
一个基于 Android 平台的中国象棋对弈应用，提供人机对弈模式，能够与 AI 进行对战。

## 主要功能
- 提供人机对弈模式，能够与 AI 进行象棋对战。
- 多种难度级别，适应不同水平的用户需求，AI 共有五个难度级别（入门、业余、专业、大师、特级大师）。
- 简洁易用的用户界面，便于用户上手操作。
- 落子音效提示，提醒用户专注棋盘，增加趣味性。
- 高效的 AI 算法，能够在一到两秒内给出合理的走棋方案，特别是在高级难度下表现出较强的计算能力和策略水平。
- 用户可以自定义棋子的外观，提供卡通款式和传统木纹款式选择，并可调整音效等设置。

## 技术栈
- **开发语言**：Java
- **前端平台**：Android
- **开发环境**：Android Studio
- **主要组件**：
  - Activity：管理应用的生命周期和用户交互，例如 `MainActivityWph`、`SettingsActivityWph`、`WebViewActivityWph`。
  - 布局文件：采用 XML 定义应用的 UI 布局。
- **后端**：
  - AI 算法：集成在应用内部，使用 Java 编写，实现五个难度级别的 AI 对弈功能。
  - 数据存储：使用 `SharedPreferences` 保存用户配置和游戏状态。
  - 音效管理：通过 `SoundPool` 管理游戏音效。

## 引用
- [项目链接 1](https://github.imc.re/kongxiangchx/ChineseChess)
- [项目链接 2](https://github.imc.re/TangboPro/ChineseChess)
