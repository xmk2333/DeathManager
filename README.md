# DeathManager - 跨服务器死亡管理插件

![Version](https://img.shields.io/badge/Minecraft-1.20.x-blue)
![License](https://img.shields.io/badge/License-MIT-green)

简体中文 | [English](README_EN.md)
![69a53941ac25dc4b7989a9d8d328887f](https://github.com/user-attachments/assets/34b31725-270b-4d21-9da4-778ec7d1ad09)

## 📝 介绍

DeathManager 是一个跨服务器的死亡管理插件系统，包含 Velocity 代理端和 Spigot 服务端插件。它提供了完整的死亡处理机制，让玩家的死亡体验更加流畅和个性化。

## ✨ 功能特性

- 💀 自定义死亡处理
  - 自定义死亡标题和副标题
  - 自定义死亡音效
  - 5秒倒计时重生
  - 观察者模式过渡
- 🌐 跨服务器支持
  - 跨服务器传送系统
  - 服务器间通信同步
  - 多服务器重生点管理
- 🎮 游戏体验优化
  - 平滑的重生过渡
  - 生存模式自动切换
  - 自定义重生命令
- 🌍 多语言支持
  - 可自定义所有提示信息
  - 支持多种语言配置

## 📥 安装步骤

### Velocity 端
1. 下载 `DeathManager-Velocity.jar`
2. 放入 Velocity 的 `plugins` 文件夹
3. 重启服务器生成配置文件

### Spigot 端
1. 下载 `DeathManager-Spigot.jar`
2. 放入各子服务器的 `plugins` 文件夹
3. 重启服务器生成配置文件

## ⚙️ 配置文件

### Spigot 配置
```yaml
# DeathManager-Spigot 配置文件

# 是否跳过死亡传送系统（如果为true则使用本地重生点）
bypass: false

# 死亡相关设置
death:
  # 死亡音效设置
  play_sound: true
  sound: ENTITY_PLAYER_DEATH
  volume: 1.0
  pitch: 1.0
  
  # 标题设置
  title:
    # 死亡标题文本列表，每次死亡随机显示其中一条
    texts:
      - "&c你死了！"
      - "&c死亡如风，常伴吾身"
      - "&c这是一次糟糕的冒险..."
      - "&c勇士，请重新振作！"
      - "&c这只是一个小挫折..."
    # 副标题文本，支持变量：
    # {time} - 复活倒计时（秒）
    subtitle: "&7{time}秒后复活..."
    # 标题淡入时间（秒）
    fade_in: 0.5
    # 标题停留时间（秒）
    stay: 5
    # 标题淡出时间（秒）
    fade_out: 0.5

# 复活后执行的操作
# 可用变量：
# {player} - 玩家名称
# {world} - 玩家所在世界
# {x} - X坐标
# {y} - Y坐标
# {z} - Z坐标
#
# 支持的操作类型：
# [op] - 以OP权限执行命令/可能触发提权，不推荐使用
# [console] - 以控制台身份执行命令
# [player] - 以玩家身份执行命令
# [message] - 发送消息给玩家
# [title] - 发送标题
# [subtitle] - 发送副标题（需要和title一起使用）
# 
# 对于[title]和[subtitle]，可以在后面添加显示时间（tick）：
# [title:20:100:20] - 分别表示淡入:停留:淡出时间
respawn_commands:
  enabled: true
  actions:
    - "[message]&a欢迎回来，{player}！"
    - "[title:10:40:10]&6欢迎回来"
    - "[subtitle:10:40:10]&7亲爱的&f{player}"

# 调试设置
debug: false

# 本地重生点设置（仅在bypass=true时使用）
spawn:
  world: world
  x: 0.0
  y: 64.0
  z: 0.0
  yaw: 0.0
  pitch: 0.0 

debug: false
```

## 📌 命令系统

### Velocity 命令
| 命令 | 描述 |
|------|------|
| `/dmreload` | 重载插件配置 |
| `/deathmanagerreload` | 重载插件配置（别名） |

### Spigot 命令
| 命令 | 描述 |
|------|------|
| `/setserverspawn` | 设置服务器重生点 |
| `/deathmanager` | 插件管理命令 |

## 🔧 依赖要求

- ☕ Java 17+
- 🎮 Minecraft 1.20.x
- 🛠️ Velocity/Spigot 最新版

## ⚠️ 注意事项

1. 确保 Velocity 和 Spigot 服务器都已正确安装对应插件
2. 修改配置后需要重载插件或重启服务器
3. 请正确设置重生点，避免玩家重生位置错误

## 💬 技术支持

遇到问题？我们随时为您提供帮助！

[![加入我们的QQ群](https://img.shields.io/badge/QQ群-528651839-blue)](https://jq.qq.com/?_wv=1027&k=528651839)

## 📜 开源协议

本项目采用 [MIT](LICENSE) 许可证开源。 
