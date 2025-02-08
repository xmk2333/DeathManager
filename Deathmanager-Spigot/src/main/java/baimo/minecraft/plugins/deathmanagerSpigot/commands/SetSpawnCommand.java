package baimo.minecraft.plugins.deathmanagerSpigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import baimo.minecraft.plugins.deathmanagerSpigot.DeathmanagerSpigot;
import java.nio.charset.StandardCharsets;

public class SetSpawnCommand implements CommandExecutor {
    private final DeathmanagerSpigot plugin;

    public SetSpawnCommand(DeathmanagerSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("messages.plugin.player_only"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("deathmanager.admin")) {
            player.sendMessage(plugin.getLangMessage("messages.plugin.no_permission"));
            return true;
        }

        try {
            Location location = player.getLocation();
            
            // 构建消息字符串，格式：setspawn\n玩家名\n世界名\n坐标数据
            String message = String.format("setspawn\n%s\n%s\n%.2f,%.2f,%.2f,%.2f,%.2f",
                player.getName(),
                location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch()
            );

            plugin.getLogger().info("发送设置重生点消息: " + message);
            
            // 发送消息到Velocity
            player.getServer().sendPluginMessage(plugin, "deathmanager:message", message.getBytes(StandardCharsets.UTF_8));
            
            // 发送成功消息给玩家
            player.sendMessage(plugin.getLangMessage("messages.spawn.set_success"));
            
            // 记录日志
            plugin.getLogger().info(String.format(
                "已发送重生点设置请求: world=%s, location=%.2f,%.2f,%.2f,%.2f,%.2f",
                location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch()
            ));
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error setting spawn point: " + e.getMessage());
            player.sendMessage(plugin.getLangMessage("messages.spawn.set_error"));
            return false;
        }
    }
} 