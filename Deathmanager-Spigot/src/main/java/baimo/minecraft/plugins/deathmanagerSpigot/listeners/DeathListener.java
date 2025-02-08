package baimo.minecraft.plugins.deathmanagerSpigot.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.attribute.Attribute;
import baimo.minecraft.plugins.deathmanagerSpigot.DeathmanagerSpigot;
import org.bukkit.event.EventPriority;

public class DeathListener implements Listener {
    private final DeathmanagerSpigot plugin;

    public DeathListener(DeathmanagerSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        double finalDamage = event.getFinalDamage();
        double currentHealth = player.getHealth();

        plugin.getLogger().info(String.format("[DeathManager] 玩家受到伤害: player=%s, damage=%.2f, health=%.2f", 
            player.getName(), finalDamage, currentHealth));

        if (currentHealth - finalDamage > 0) {
            return;
        }

        plugin.getLogger().info(String.format("[DeathManager] 玩家将死亡，取消伤害事件: player=%s", player.getName()));
        event.setCancelled(true);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        // 播放死亡音效
        if (plugin.getConfig().getBoolean("death.play_sound", true)) {
            String soundName = plugin.getConfig().getString("death.sound", "ENTITY_PLAYER_DEATH");
            float volume = (float) plugin.getConfig().getDouble("death.volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("death.pitch", 1.0);

            try {
                Sound sound = Sound.valueOf(soundName);
                player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
                plugin.getLogger().info(String.format("[DeathManager] 播放死亡音效: player=%s, sound=%s, volume=%.1f, pitch=%.1f",
                    player.getName(), soundName, volume, pitch));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe(String.format("[DeathManager] 无效的音效名称: %s", soundName));
            }
        }

        // 设置为旁观者模式
        player.setGameMode(GameMode.SPECTATOR);
        plugin.getLogger().info(String.format("[DeathManager] 设置玩家为旁观者模式: player=%s", player.getName()));

        // 5秒后请求重生点
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info(String.format("[DeathManager] 请求重生点: player=%s", player.getName()));
                plugin.requestSpawnLocation(player);
            }
        }.runTaskLater(plugin, 100L);
    }
} 