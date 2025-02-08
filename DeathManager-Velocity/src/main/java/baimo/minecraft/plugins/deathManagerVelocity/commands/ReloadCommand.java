package baimo.minecraft.plugins.deathManagerVelocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import baimo.minecraft.plugins.deathManagerVelocity.DeathManagerVelocity;


public class ReloadCommand implements SimpleCommand {
    private final DeathManagerVelocity plugin;

    public ReloadCommand(DeathManagerVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        if (!source.hasPermission("deathmanager.admin")) {
            source.sendMessage(Component.text("§c你没有权限执行此命令"));
            return;
        }

        try {
            // 重载配置
            plugin.loadConfig();
            
            // 通知所有服务器重新加载配置
            plugin.getEventSyncManager().broadcastSpawnUpdate();
            
            source.sendMessage(Component.text("§a[DeathManager] 配置已重载并同步到所有服务器"));
        } catch (Exception e) {
            plugin.getLogger().error("重载配置时出错", e);
            source.sendMessage(Component.text("§c[DeathManager] 重载配置时出错，请查看控制台"));
        }
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("deathmanager.admin");
    }
} 
