package baimo.minecraft.plugins.deathManagerVelocity.sync;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import baimo.minecraft.plugins.deathManagerVelocity.DeathManagerVelocity;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

public class EventSyncManager {
    private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("deathmanager", "sync");
    private final ProxyServer server;
    private final Logger logger;
    private final ConfigurationNode config;
    private final long syncDelay;
    private final DeathManagerVelocity plugin;

    public EventSyncManager(DeathManagerVelocity plugin, ProxyServer server, Logger logger, ConfigurationNode config) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
        this.config = config;
        this.syncDelay = config.node("sync", "delay").getLong(50);
        logger.info("[DeathManager] 事件同步管理器已初始化");
    }

    public void broadcastDeath(String playerName, String sourceServer, String deathMessage) {
        if (!config.node("sync", "enabled").getBoolean(true) ||
                !config.node("sync", "events", "death").getBoolean(true)) {
            return;
        }

        logger.info("[DeathManager] 正在同步死亡事件: player={}, server={}", playerName, sourceServer);

        for (RegisteredServer targetServer : server.getAllServers()) {
            String targetServerName = targetServer.getServerInfo().getName();
            if (!targetServerName.equals(sourceServer)) {
                server.getScheduler()
                        .buildTask(plugin, () -> {
                            targetServer.sendPluginMessage(
                                    CHANNEL,
                                    String.format("death\n%s\n%s\n%s", playerName, sourceServer, deathMessage)
                                            .getBytes()
                            );
                        })
                        .delay(syncDelay, TimeUnit.MILLISECONDS)
                        .schedule();
            }
        }
    }

    public void broadcastEffect(String type, String sourceServer, String data) {
        if (!config.node("sync", "enabled").getBoolean(true) ||
                !config.node("sync", "events", "effects").getBoolean(true)) {
            return;
        }

        logger.info("[DeathManager] 正在同步特效: type={}, server={}", type, sourceServer);

        for (RegisteredServer targetServer : server.getAllServers()) {
            String targetServerName = targetServer.getServerInfo().getName();
            if (!targetServerName.equals(sourceServer)) {
                server.getScheduler()
                        .buildTask(plugin, () -> {
                            targetServer.sendPluginMessage(
                                    CHANNEL,
                                    String.format("effect\n%s\n%s\n%s", type, sourceServer, data)
                                            .getBytes()
                            );
                        })
                        .delay(syncDelay, TimeUnit.MILLISECONDS)
                        .schedule();
            }
        }
    }

    public void broadcastSound(String soundName, String sourceServer, String data) {
        if (!config.node("sync", "enabled").getBoolean(true) ||
                !config.node("sync", "events", "sounds").getBoolean(true)) {
            return;
        }

        logger.info("[DeathManager] 正在同步音效: sound={}, server={}", soundName, sourceServer);

        for (RegisteredServer targetServer : server.getAllServers()) {
            String targetServerName = targetServer.getServerInfo().getName();
            if (!targetServerName.equals(sourceServer)) {
                server.getScheduler()
                        .buildTask(plugin, () -> {
                            targetServer.sendPluginMessage(
                                    CHANNEL,
                                    String.format("sound\n%s\n%s\n%s", soundName, sourceServer, data)
                                            .getBytes()
                            );
                        })
                        .delay(syncDelay, TimeUnit.MILLISECONDS)
                        .schedule();
            }
        }
    }

    public void broadcastCommand(String command, String sourceServer) {
        if (!config.node("sync", "enabled").getBoolean(true) ||
                !config.node("sync", "events", "commands").getBoolean(true)) {
            return;
        }

        logger.info("[DeathManager] 正在同步命令: command={}, server={}", command, sourceServer);

        for (RegisteredServer targetServer : server.getAllServers()) {
            String targetServerName = targetServer.getServerInfo().getName();
            if (!targetServerName.equals(sourceServer)) {
                server.getScheduler()
                        .buildTask(plugin, () -> {
                            targetServer.sendPluginMessage(
                                    CHANNEL,
                                    String.format("command\n%s\n%s", sourceServer, command)
                                            .getBytes()
                            );
                        })
                        .delay(syncDelay, TimeUnit.MILLISECONDS)
                        .schedule();
            }
        }
    }

    public void broadcastSpawnUpdate() {
        try {
            ConfigurationNode spawnNode = config.node("spawn");
            String serverName = spawnNode.node("server").getString();
            String worldName = spawnNode.node("world").getString();
            double x = spawnNode.node("x").getDouble();
            double y = spawnNode.node("y").getDouble();
            double z = spawnNode.node("z").getDouble();
            float yaw = (float) spawnNode.node("yaw").getDouble();
            float pitch = (float) spawnNode.node("pitch").getDouble();

            // 构建更新消息
            String message = String.format("spawn_update\n%s\n%s\n%.2f,%.2f,%.2f,%.2f,%.2f",
                serverName, worldName, x, y, z, yaw, pitch);

            // 发送到所有服务器
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            for (RegisteredServer targetServer : server.getAllServers()) {
                targetServer.sendPluginMessage(
                    MinecraftChannelIdentifier.create("deathmanager", "sync"),
                    messageBytes
                );
            }

            logger.info("[DeathManager] 已广播重生点更新到所有服务器");
        } catch (Exception e) {
            logger.error("[DeathManager] 广播重生点更新时出错", e);
        }
    }
} 