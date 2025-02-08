package baimo.minecraft.plugins.deathmanagerSpigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import baimo.minecraft.plugins.deathmanagerSpigot.listeners.DeathListener;
import baimo.minecraft.plugins.deathmanagerSpigot.listeners.SyncMessageListener;
import baimo.minecraft.plugins.deathmanagerSpigot.commands.SetSpawnCommand;
import baimo.minecraft.plugins.deathmanagerSpigot.commands.ReloadCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.Random;
import org.bukkit.Sound;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DeathmanagerSpigot extends JavaPlugin implements Listener {
    private static DeathmanagerSpigot instance;
    private FileConfiguration langConfig;
    private File langFile;
    private String serverName = null;

    @Override
    public void onEnable() {
        instance = this;
        
        // 创建插件目录
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // 加载配置
        loadAllConfigs();
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        
        // 注册命令
        getCommand("setserverspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("deathmanager").setExecutor(new ReloadCommand(this));
        
        // 注册消息通道
        getServer().getMessenger().registerOutgoingPluginChannel(this, "deathmanager:message");
        getServer().getMessenger().registerIncomingPluginChannel(this, "deathmanager:sync", new SyncMessageListener(this));
        
        // 定期获取服务器名称
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!getServer().getOnlinePlayers().isEmpty()) {
                    Player player = getServer().getOnlinePlayers().iterator().next();
                    try {
                        String message = "get_server\n" + player.getName();
                        player.sendPluginMessage(DeathmanagerSpigot.this, "deathmanager:message", message.getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        getLogger().warning("获取服务器名称时出错: " + e.getMessage());
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
        
        getLogger().info(getLangMessage("messages.plugin.enabled"));
    }

    @Override
    public void onDisable() {
        getLogger().info(getLangMessage("messages.plugin.disabled"));
        
        // 注销消息通道
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    public void loadAllConfigs() {
        // 保存默认配置（如果不存在）
        saveDefaultConfig();
        reloadConfig();
        
        // 加载语言文件（如果不存在才创建）
        langFile = new File(getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            saveResource("lang.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public static DeathmanagerSpigot getInstance() {
        return instance;
    }

    public String getLangMessage(String path) {
        return getLangMessage(path, new String[0]);
    }

    public String getLangMessage(String path, String... replacements) {
        if (langConfig == null) {
            return "§c语言文件未加载";
        }

        String message = langConfig.getString(path);
        if (message == null) {
            return "§c找不到语言键: " + path;
        }

        // 替换变量
        if (replacements != null && replacements.length >= 2) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    message = message.replace(replacements[i], replacements[i + 1]);
                }
            }
        }

        return message.replace('&', '§');
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        double finalDamage = event.getFinalDamage();
        double health = player.getHealth();

        if (health - finalDamage <= 0) {
            event.setCancelled(true);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setGameMode(GameMode.SPECTATOR);

            // 播放死亡音效
            if (getConfig().getBoolean("death.play_sound", true)) {
                String soundName = getConfig().getString("death.sound", "entity_player_death");
                float volume = (float) getConfig().getDouble("death.volume", 1.0);
                float pitch = (float) getConfig().getDouble("death.pitch", 1.0);
                try {
                    Sound sound = Sound.valueOf(soundName.toUpperCase());
                    player.playSound(player.getLocation(), sound, volume, pitch);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("无效的声音名称: " + soundName);
                }
            }

            // 显示死亡标题
            List<String> titles = getConfig().getStringList("death.title.texts");
            String deathTitle = "";  // 存储死亡标题
            if (!titles.isEmpty()) {
                deathTitle = titles.get(new Random().nextInt(titles.size())).replace('&', '§');
                String subtitle = getConfig().getString("death.title.subtitle", "").replace('&', '§');
                // 将秒转换为 tick (1秒 = 20 tick)
                int fadeIn = (int)(getConfig().getDouble("death.title.fade_in", 0.5) * 20);
                int stay = (int)(getConfig().getDouble("death.title.stay", 5.0) * 20);
                int fadeOut = (int)(getConfig().getDouble("death.title.fade_out", 0.5) * 20);

                // 替换变量
                subtitle = subtitle.replace("{time}", "5");

                player.sendTitle(deathTitle, subtitle, fadeIn, stay, fadeOut);
            }

            // 5秒后发送重生请求
            final String finalDeathTitle = deathTitle;  // 将标题传入匿名类
            new BukkitRunnable() {
                int countdown = 5;

                @Override
                public void run() {
                    if (countdown <= 0) {
                        if (getConfig().getBoolean("debug", false)) {
                            getLogger().info("发送死亡请求到 Velocity");
                        }
                        // 发送请求到 Velocity
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        try {
                            stream.write(("death\n" + player.getName() + "\nrequest_spawn").getBytes());
                            player.sendPluginMessage(DeathmanagerSpigot.this, "deathmanager:message", stream.toByteArray());
                        } catch (IOException e) {
                            getLogger().warning("发送重生请求时出错: " + e.getMessage());
                        }
                        cancel();
                    } else {
                        // 更新副标题倒计时，但保持主标题不变
                        String subtitle = getConfig().getString("death.title.subtitle", "").replace('&', '§');
                        subtitle = subtitle.replace("{time}", String.valueOf(countdown));
                        player.sendTitle(finalDeathTitle, subtitle, 0, 21, 0);
                        countdown--;
                    }
                }
            }.runTaskTimer(this, 0L, 20L);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            if (getConfig().getBoolean("debug", false)) {
                getLogger().info("检测到玩家 " + player.getName() + " 以观察者模式加入，正在修改为生存模式");
            }
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setFoodLevel(20);
        }
    }

    public void executeRespawnCommands(Player player, Location spawnLocation) {
        if (!getConfig().getBoolean("respawn_commands.enabled", true)) {
            getLogger().info("[DeathManager] 重生命令已禁用，跳过执行");
            return;
        }

        getLogger().info(String.format("[DeathManager] 开始执行重生命令: player=%s, location=%.2f,%.2f,%.2f", 
            player.getName(), 
            spawnLocation.getX(),
            spawnLocation.getY(),
            spawnLocation.getZ()
        ));

        List<String> actions = getConfig().getStringList("respawn_commands.actions");
        getLogger().info(String.format("[DeathManager] 读取到 %d 个动作", actions.size()));

        String currentTitle = null;
        String currentSubtitle = null;
        int fadeIn = 10;
        int stay = 70;
        int fadeOut = 20;

        for (int i = 0; i < actions.size(); i++) {
            try {
                String action = actions.get(i);
                getLogger().info(String.format("[DeathManager] 处理动作 %d/%d: %s", i + 1, actions.size(), action));

                String[] actionParts = action.substring(1).split("]", 2);
                if (actionParts.length != 2) {
                    getLogger().warning(String.format("[DeathManager] 无效的动作格式: %s", action));
                    continue;
                }

                String actionType = actionParts[0];
                String content = actionParts[1];
                
                // 替换变量
                content = content.replace("{player}", player.getName())
                        .replace("{world}", spawnLocation.getWorld().getName())
                        .replace("{x}", String.format("%.2f", spawnLocation.getX()))
                        .replace("{y}", String.format("%.2f", spawnLocation.getY()))
                        .replace("{z}", String.format("%.2f", spawnLocation.getZ()))
                        .replace('&', '§');

                getLogger().info(String.format("[DeathManager] 处理动作: type=%s, content=%s", actionType, content));

                // 检查是否包含时间参数
                String[] actionTypeParts = actionType.split(":");
                actionType = actionTypeParts[0];

                if (actionTypeParts.length > 1) {
                    String[] times = actionTypeParts[1].split(",");
                    if (times.length == 3) {
                        fadeIn = Integer.parseInt(times[0]);
                        stay = Integer.parseInt(times[1]);
                        fadeOut = Integer.parseInt(times[2]);
                        getLogger().info(String.format("[DeathManager] 设置标题时间: fadeIn=%d, stay=%d, fadeOut=%d", 
                            fadeIn, stay, fadeOut));
                    }
                }

                switch (actionType) {
                    case "message":
                        getLogger().info(String.format("[DeathManager] 发送消息: %s", content));
                        player.sendMessage(content);
                        break;
                    case "title":
                        getLogger().info(String.format("[DeathManager] 设置标题: %s", content));
                        currentTitle = content;
                        // 如果下一个动作是副标题，等待处理
                        if (i + 1 < actions.size() && actions.get(i + 1).contains("[subtitle")) {
                            getLogger().info("[DeathManager] 检测到下一个动作是副标题，等待处理");
                            continue;
                        }
                        // 否则直接发送标题
                        getLogger().info(String.format("[DeathManager] 发送标题: title=%s, subtitle=%s, fadeIn=%d, stay=%d, fadeOut=%d", 
                            currentTitle, currentSubtitle != null ? currentSubtitle : "", fadeIn, stay, fadeOut));
                        player.sendTitle(currentTitle, currentSubtitle != null ? currentSubtitle : "", fadeIn, stay, fadeOut);
                        currentTitle = null;
                        currentSubtitle = null;
                        break;
                    case "subtitle":
                        getLogger().info(String.format("[DeathManager] 设置副标题: %s", content));
                        currentSubtitle = content;
                        // 如果已有标题，发送标题和副标题
                        if (currentTitle != null) {
                            getLogger().info(String.format("[DeathManager] 发送标题和副标题: title=%s, subtitle=%s, fadeIn=%d, stay=%d, fadeOut=%d", 
                                currentTitle, currentSubtitle, fadeIn, stay, fadeOut));
                            player.sendTitle(currentTitle, currentSubtitle, fadeIn, stay, fadeOut);
                            currentTitle = null;
                            currentSubtitle = null;
                        }
                        break;
                    case "console":
                        getLogger().info(String.format("[DeathManager] 执行控制台命令: %s", content));
                        getServer().dispatchCommand(getServer().getConsoleSender(), content);
                        break;
                    case "player":
                        getLogger().info(String.format("[DeathManager] 执行玩家命令: %s", content));
                        player.performCommand(content);
                        break;
                }
            } catch (Exception e) {
                getLogger().severe(String.format("[DeathManager] 执行重生命令时出错: %s", e.getMessage()));
                e.printStackTrace();
            }
        }
        getLogger().info(String.format("[DeathManager] 重生命令执行完成: %s", player.getName()));
    }

    public void setServerName(String name) {
        if (serverName == null || !serverName.equals(name)) {
            serverName = name;
            if (getConfig().getBoolean("debug", false)) {
                getLogger().fine("[DeathManager] 服务器名称已设置为: " + name);
            }
        }
    }

    public String getServerName() {
        return serverName;
    }

    public void requestSpawnLocation(Player player) {
        getLogger().info(String.format("[DeathManager] 发送重生点请求: player=%s", player.getName()));
        
        // 构造消息
        String message = String.format("death\n%s", player.getName());
        
        // 发送消息到 Velocity
        player.sendPluginMessage(this, "deathmanager:message", message.getBytes(StandardCharsets.UTF_8));
        getLogger().info(String.format("[DeathManager] 重生点请求已发送: player=%s", player.getName()));
    }
} 