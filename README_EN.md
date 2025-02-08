# DeathManager - Cross-Server Death Management Plugin

![Version](https://img.shields.io/badge/Minecraft-1.20.x-blue)
![License](https://img.shields.io/badge/License-MIT-green)

[简体中文](README.md) | English

## 📝 Introduction

DeathManager is a cross-server death management plugin system that includes both Velocity proxy and Spigot server plugins. It provides a complete death handling mechanism, making the player's death experience more smooth and personalized.

## ✨ Features

- 💀 Custom Death Handling
  - Custom death titles and subtitles
  - Custom death sound effects
  - 5-second respawn countdown
  - Spectator mode transition
- 🌐 Cross-Server Support
  - Cross-server teleportation system
  - Server communication synchronization
  - Multi-server spawn point management
- 🎮 Gameplay Optimization
  - Smooth respawn transition
  - Automatic survival mode switching
  - Custom respawn commands
- 🌍 Multi-language Support
  - Customizable messages
  - Multiple language configurations

## 📥 Installation

### Velocity Side
1. Download `DeathManager-Velocity.jar`
2. Place it in the Velocity `plugins` folder
3. Restart the server to generate configuration files

### Spigot Side
1. Download `DeathManager-Spigot.jar`
2. Place it in each sub-server's `plugins` folder
3. Restart the server to generate configuration files

## ⚙️ Configuration

### Spigot Configuration
```yaml
# DeathManager-Spigot Configuration

# Skip death teleportation system (if true, use local spawn point)
bypass: false

# Death-related settings
death:
  # Death sound settings
  play_sound: true
  sound: ENTITY_PLAYER_DEATH
  volume: 1.0
  pitch: 1.0
  
  # Title settings
  title:
    # Death title text list, randomly displays one on death
    texts:
      - "&cYou died!"
      - "&cDeath is like the wind, always by my side"
      - "&cThat was a bad adventure..."
      - "&cStand up, warrior!"
      - "&cJust a minor setback..."
    # Subtitle text, supports variables:
    # {time} - Respawn countdown (seconds)
    subtitle: "&7Respawning in {time} seconds..."
    # Title fade in time (seconds)
    fade_in: 0.5
    # Title stay time (seconds)
    stay: 5
    # Title fade out time (seconds)
    fade_out: 0.5

# Actions executed after respawn
# Available variables:
# {player} - Player name
# {world} - Player's world
# {x} - X coordinate
# {y} - Y coordinate
# {z} - Z coordinate
#
# Supported action types:
# [op] - Execute command with OP permissions/may trigger elevation, not recommended
# [console] - Execute command as console
# [player] - Execute command as player
# [message] - Send message to player
# [title] - Send title
# [subtitle] - Send subtitle (must be used with title)
# 
# For [title] and [subtitle], you can add display time (ticks):
# [title:20:100:20] - Represents fade_in:stay:fade_out time
respawn_commands:
  enabled: true
  actions:
    - "[message]&aWelcome back, {player}!"
    - "[title:10:40:10]&6Welcome Back"
    - "[subtitle:10:40:10]&7Dear &f{player}"

# Debug settings
debug: false

# Local spawn point settings (only used when bypass=true)
spawn:
  world: world
  x: 0.0
  y: 64.0
  z: 0.0
  yaw: 0.0
  pitch: 0.0
```

## 📌 Commands

### Velocity Commands
| Command | Description |
|---------|-------------|
| `/dmreload` | Reload plugin configuration |
| `/deathmanagerreload` | Reload plugin configuration (alias) |

### Spigot Commands
| Command | Description |
|---------|-------------|
| `/setserverspawn` | Set server spawn point |
| `/deathmanager` | Plugin management command |

## 🔧 Requirements

- ☕ Java 17+
- 🎮 Minecraft 1.20.x
- 🛠️ Latest Velocity/Spigot version

## ⚠️ Notes

1. Ensure both Velocity and Spigot servers have the corresponding plugins installed
2. Configuration changes require plugin reload or server restart
3. Please set spawn points correctly to avoid incorrect player respawn locations

## 💬 Technical Support

Need help? We're here for you!

[![Join our QQ Group](https://img.shields.io/badge/QQ%20Group-528651839-blue)](https://jq.qq.com/?_wv=1027&k=528651839)

## 📜 License

This project is licensed under the [MIT](LICENSE) License. 