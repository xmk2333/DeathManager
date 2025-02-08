package baimo.minecraft.plugins.deathmanagerSpigot.listeners;

import baimo.minecraft.plugins.deathmanagerSpigot.DeathmanagerSpigot;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.attribute.Attribute;
import java.nio.charset.StandardCharsets;

public class SyncMessageListener implements PluginMessageListener {
    private final DeathmanagerSpigot plugin;

    public SyncMessageListener(DeathmanagerSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("deathmanager:sync")) {
            return;
        }

        String messageStr = new String(message, StandardCharsets.UTF_8);
        plugin.getLogger().fine(String.format("[DeathManager] 收到同步消息: channel=%s, player=%s, message=%s", 
            channel, player.getName(), messageStr));

        String[] parts = messageStr.split("\n");
        if (parts.length < 2) {
            plugin.getLogger().warning(String.format("[DeathManager] 无效的消息格式: %s", messageStr));
            return;
        }

        String type = parts[0];
        String playerName = parts[1];
        Player targetPlayer = plugin.getServer().getPlayer(playerName);

        if (targetPlayer == null) {
            plugin.getLogger().warning(String.format("[DeathManager] 找不到目标玩家: %s", playerName));
            return;
        }

        plugin.getLogger().fine(String.format("[DeathManager] 处理同步消息: type=%s, player=%s", type, playerName));

        switch (type) {
            case "local_respawn":
            case "spawn":
                handleRespawnMessage(targetPlayer, parts);
                break;
            case "server_name":
                if (parts.length >= 3) {
                    plugin.setServerName(parts[2]);
                    plugin.getLogger().fine(String.format("[DeathManager] 设置服务器名称: %s", parts[2]));
                }
                break;
            default:
                plugin.getLogger().warning(String.format("[DeathManager] 未知的消息类型: %s", type));
                break;
        }
    }

    private void handleRespawnMessage(Player targetPlayer, String[] parts) {
        plugin.getLogger().info(String.format("[DeathManager] 开始处理重生消息: player=%s", targetPlayer.getName()));

        if (parts.length < 4) {
            plugin.getLogger().warning(String.format("[DeathManager] 重生消息格式无效: parts.length=%d", parts.length));
            return;
        }

        String worldName = parts[2];
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning(String.format("[DeathManager] 找不到目标世界: %s", worldName));
            return;
        }

        String[] coords = parts[3].split(",");
        if (coords.length < 5) {
            plugin.getLogger().warning(String.format("[DeathManager] 坐标格式无效: %s", parts[3]));
            return;
        }

        try {
            double x = Double.parseDouble(coords[0]);
            double y = Double.parseDouble(coords[1]);
            double z = Double.parseDouble(coords[2]);
            float yaw = Float.parseFloat(coords[3]);
            float pitch = Float.parseFloat(coords[4]);

            Location spawnLocation = new Location(world, x, y, z, yaw, pitch);
            plugin.getLogger().info(String.format("[DeathManager] 重生位置: world=%s, x=%.2f, y=%.2f, z=%.2f, yaw=%.2f, pitch=%.2f",
                worldName, x, y, z, yaw, pitch));

            // 恢复玩家状态
            targetPlayer.setGameMode(GameMode.SURVIVAL);
            targetPlayer.setHealth(targetPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            targetPlayer.setFoodLevel(20);
            plugin.getLogger().info(String.format("[DeathManager] 已恢复玩家状态: player=%s, health=%.1f, food=%d",
                targetPlayer.getName(), targetPlayer.getHealth(), targetPlayer.getFoodLevel()));

            // 传送玩家
            targetPlayer.teleport(spawnLocation);
            plugin.getLogger().info(String.format("[DeathManager] 已传送玩家到重生点: player=%s", targetPlayer.getName()));

            // 执行重生命令
            plugin.getLogger().info(String.format("[DeathManager] 准备执行重生命令: player=%s", targetPlayer.getName()));
            plugin.executeRespawnCommands(targetPlayer, spawnLocation);
            plugin.getLogger().info(String.format("[DeathManager] 重生命令执行完成: player=%s", targetPlayer.getName()));

        } catch (NumberFormatException e) {
            plugin.getLogger().severe(String.format("[DeathManager] 解析坐标时出错: %s", e.getMessage()));
            e.printStackTrace();
        }
    }
} 