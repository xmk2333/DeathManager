package baimo.minecraft.plugins.deathmanagerSpigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import baimo.minecraft.plugins.deathmanagerSpigot.DeathmanagerSpigot;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {
    private final DeathmanagerSpigot plugin;

    public ReloadCommand(DeathmanagerSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("deathmanager.admin")) {
            sender.sendMessage(plugin.getLangMessage("messages.plugin.no_permission"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            try {
                plugin.loadAllConfigs();
                sender.sendMessage(plugin.getLangMessage("messages.plugin.reload"));
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("重载配置时发生错误: " + e.getMessage());
                sender.sendMessage("§c[DeathManager] 重载配置时发生错误，请查看控制台");
                return false;
            }
        }

        sender.sendMessage("§c用法: /deathmanager reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
        }
        return completions;
    }
} 