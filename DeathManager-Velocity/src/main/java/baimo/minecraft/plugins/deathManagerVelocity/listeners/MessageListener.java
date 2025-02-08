package baimo.minecraft.plugins.deathManagerVelocity.listeners;

import baimo.minecraft.plugins.deathManagerVelocity.DeathManagerVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class MessageListener {
    private final DeathManagerVelocity plugin;
    private final Logger logger;

    public MessageListener(DeathManagerVelocity plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        logger.info("[DeathManager] 消息监听器已初始化");
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        logger.info("[DeathManager] 收到插件消息");
        
        if (!event.getIdentifier().getId().equals("deathmanager:message")) {
            logger.debug("[DeathManager] 忽略非目标频道消息: " + event.getIdentifier().getId());
            return;
        }

        // 防止事件被处理两次
        if (!event.getResult().isAllowed()) {
            logger.info("[DeathManager] 消息已被处理，跳过");
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        String messageStr = new String(event.getData(), StandardCharsets.UTF_8);
        String[] parts = messageStr.split("\n");
        if (parts.length < 2) {
            logger.warn("[DeathManager] 收到无效的消息格式: " + messageStr);
            return;
        }

        String type = parts[0];
        String playerName = parts[1];

        // 获取源服务器
        Optional<ServerConnection> sourceServer = event.getSource() instanceof ServerConnection
                ? Optional.of((ServerConnection) event.getSource())
                : Optional.empty();

        if (!sourceServer.isPresent()) {
            logger.warn("[DeathManager] 无法处理消息：未知的源服务器");
            return;
        }

        logger.info("[DeathManager] 处理消息: type={}, player={}, server={}, raw_message={}", 
            type, playerName, sourceServer.get().getServerInfo().getName(), messageStr);

        switch (type) {
            case "death":
                if (parts.length >= 3 && "request_spawn".equals(parts[2])) {
                    logger.info("[DeathManager] 处理死亡请求: player={}", playerName);
                    handleDeathRequest(playerName, sourceServer.get());
                } else {
                    logger.warn("[DeathManager] 无效的死亡请求格式: {}", messageStr);
                }
                break;
            case "get_server":
                String serverName = sourceServer.get().getServerInfo().getName();
                logger.debug("[DeathManager] 处理服务器名称请求: player={}, server={}", playerName, serverName);
                String response = String.format("server_name\n%s\n%s", playerName, serverName);
                sourceServer.get().sendPluginMessage(
                    MinecraftChannelIdentifier.create("deathmanager", "sync"),
                    response.getBytes(StandardCharsets.UTF_8)
                );
                break;
            case "setspawn":
                if (parts.length >= 3) {
                    logger.info("[DeathManager] 处理设置重生点请求");
                    handleSetSpawn(playerName, sourceServer.get().getServerInfo().getName(), parts[2], parts[3]);
                } else {
                    logger.warn("[DeathManager] 无效的设置重生点请求格式: {}", messageStr);
                }
                break;
            default:
                logger.warn("[DeathManager] 收到未知类型的消息: {}", type);
        }
    }

    private void handleDeathRequest(String playerName, ServerConnection sourceServer) {
        logger.info("[DeathManager] 开始处理死亡请求: player={}, source_server={}", 
            playerName, sourceServer.getServerInfo().getName());

        Optional<Player> playerOpt = plugin.getServer().getPlayer(playerName);
        if (!playerOpt.isPresent()) {
            logger.warn("[DeathManager] 无法处理死亡请求：玩家不在线: {}", playerName);
            return;
        }

        Player player = playerOpt.get();
        ConfigurationNode spawnNode = plugin.getConfig().node("spawn");
        String targetServerName = spawnNode.node("server").getString();

        logger.info("[DeathManager] 配置信息: target_server={}, current_server={}", 
            targetServerName, sourceServer.getServerInfo().getName());

        if (targetServerName == null) {
            logger.warn("[DeathManager] 无法处理死亡请求：未设置重生点");
            return;
        }

        Optional<RegisteredServer> targetServer = plugin.getServer().getServer(targetServerName);
        if (!targetServer.isPresent()) {
            logger.warn("[DeathManager] 无法处理死亡请求：目标服务器不存在: {}", targetServerName);
            return;
        }

        // 如果玩家在目标服务器，直接发送重生点信息
        if (sourceServer.getServerInfo().getName().equals(targetServerName)) {
            logger.info("[DeathManager] 玩家在目标服务器，发送本地重生点信息: {} -> {}", 
                playerName, targetServerName);
            sendSpawnInfo(targetServer.get(), playerName);
            return;
        }

        // 如果玩家不在目标服务器，先传送到目标服务器
        logger.info("[DeathManager] 跨服传送玩家: {} -> {}", playerName, targetServerName);
        player.createConnectionRequest(targetServer.get()).connectWithIndication().thenAccept(success -> {
            if (success) {
                logger.info("[DeathManager] 玩家传送成功，发送重生点信息: {}", playerName);
                sendSpawnInfo(targetServer.get(), playerName);
            } else {
                logger.error("[DeathManager] 无法将玩家传送到目标服务器: player={}, server={}", 
                    playerName, targetServerName);
            }
        });
    }

    private void sendSpawnInfo(RegisteredServer server, String playerName) {
        logger.info("[DeathManager] 准备发送重生点信息: player={}, server={}", 
            playerName, server.getServerInfo().getName());

        ConfigurationNode spawnNode = plugin.getConfig().node("spawn");
        String world = spawnNode.node("world").getString("world");
        double x = spawnNode.node("x").getDouble(0.0);
        double y = spawnNode.node("y").getDouble(100.0);
        double z = spawnNode.node("z").getDouble(0.0);
        float yaw = (float) spawnNode.node("yaw").getDouble(0.0);
        float pitch = (float) spawnNode.node("pitch").getDouble(0.0);

        // 构建消息，发送 local_respawn 消息
        String message = String.format("local_respawn\n%s\n%s\n%.2f,%.2f,%.2f,%.2f,%.2f",
            playerName,
            world,
            x, y, z,
            yaw, pitch
        );

        logger.info("[DeathManager] 发送重生点信息: player={}, server={}, world={}, location={},{},{}, message={}", 
            playerName, 
            server.getServerInfo().getName(),
            world,
            x, y, z,
            message
        );

        // 发送消息
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        server.sendPluginMessage(
            MinecraftChannelIdentifier.create("deathmanager", "sync"),
            messageBytes
        );
    }

    private void handleSetSpawn(String playerName, String server, String worldName, String coordsStr) {
        logger.info("[DeathManager] 处理设置出生点: player={}, server={}, world={}, coords={}", 
            playerName, server, worldName, coordsStr);

        try {
            String[] coords = coordsStr.split(",");
            if (coords.length < 5) {
                logger.warn("[DeathManager] 无效的坐标格式，期望5个值但收到 {} 个: {}", coords.length, coordsStr);
                return;
            }

            // 更新配置
            ConfigurationNode spawnNode = plugin.getConfig().node("spawn");
            spawnNode.node("server").set(server);
            spawnNode.node("world").set(worldName);
            spawnNode.node("x").set(Double.parseDouble(coords[0]));
            spawnNode.node("y").set(Double.parseDouble(coords[1]));
            spawnNode.node("z").set(Double.parseDouble(coords[2]));
            spawnNode.node("yaw").set(Float.parseFloat(coords[3]));
            spawnNode.node("pitch").set(Float.parseFloat(coords[4]));

            logger.info("[DeathManager] 更新配置: server={}, world={}, coords={}", 
                server, worldName, coordsStr);

            // 立即保存配置
            plugin.saveConfig();
            
            // 重新加载配置
            try {
                plugin.loadConfig();
                logger.info("[DeathManager] 配置已重新加载");
            } catch (Exception e) {
                logger.error("[DeathManager] 重新加载配置时出错", e);
            }
            
            // 通知所有服务器重生点已更新
            String updateMessage = String.format("spawn_update\n%s\n%s\n%.2f,%.2f,%.2f,%.2f,%.2f",
                server, worldName,
                Double.parseDouble(coords[0]),
                Double.parseDouble(coords[1]),
                Double.parseDouble(coords[2]),
                Float.parseFloat(coords[3]),
                Float.parseFloat(coords[4])
            );
            
            logger.info("[DeathManager] 广播重生点更新消息: {}", updateMessage);
            
            byte[] messageBytes = updateMessage.getBytes(StandardCharsets.UTF_8);
            for (RegisteredServer targetServer : plugin.getServer().getAllServers()) {
                logger.info("[DeathManager] 发送重生点更新到服务器: {}", targetServer.getServerInfo().getName());
                targetServer.sendPluginMessage(
                    MinecraftChannelIdentifier.create("deathmanager", "sync"),
                    messageBytes
                );
            }
            
            // 通知设置成功
            Optional<Player> setterOpt = plugin.getServer().getPlayer(playerName);
            if (setterOpt.isPresent()) {
                Player setter = setterOpt.get();
                setter.sendMessage(net.kyori.adventure.text.Component.text("§a[DeathManager] 全局重生点已更新并同步到所有服务器"));
                logger.info("[DeathManager] 已通知玩家设置成功: {}", playerName);
            }
            
            logger.info("[DeathManager] 已完成全局死亡重生点更新: server={}, world={}, location={}", 
                server, worldName, String.join(",", coords));
        } catch (Exception e) {
            logger.error("[DeathManager] 处理出生点设置时出错", e);
            // 通知设置失败
            Optional<Player> setterOpt = plugin.getServer().getPlayer(playerName);
            if (setterOpt.isPresent()) {
                Player setter = setterOpt.get();
                setter.sendMessage(net.kyori.adventure.text.Component.text("§c[DeathManager] 设置全局重生点失败: " + e.getMessage()));
                logger.info("[DeathManager] 已通知玩家设置失败: {}", playerName);
            }
        }
    }
} 