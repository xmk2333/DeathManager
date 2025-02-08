package baimo.minecraft.plugins.deathManagerVelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.command.CommandMeta;
import baimo.minecraft.plugins.deathManagerVelocity.listeners.MessageListener;
import baimo.minecraft.plugins.deathManagerVelocity.sync.EventSyncManager;
import baimo.minecraft.plugins.deathManagerVelocity.commands.ReloadCommand;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "deathmanager-velocity", name = "DeathManager-Velocity", version = BuildConstants.VERSION, description = "死亡管理插件", url = "baimowl.cn", authors = {"Baimo"})
public class DeathManagerVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private ConfigurationNode config;
    private EventSyncManager eventSyncManager;
    private final Path dataDirectory;
    private YamlConfigurationLoader configLoader;

    @Inject
    public DeathManagerVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // 加载配置文件
        try {
            loadConfig();
        } catch (IOException e) {
            logger.error("[DeathManager] 无法加载配置文件", e);
            return;
        }

        // 初始化事件同步管理器
        eventSyncManager = new EventSyncManager(this, server, logger, config);

        // 注册消息监听器
        server.getChannelRegistrar().register(
            MinecraftChannelIdentifier.create("deathmanager", "message"),
            MinecraftChannelIdentifier.create("deathmanager", "sync")
        );
        server.getEventManager().register(this, new MessageListener(this));

        // 注册命令
        CommandMeta meta = server.getCommandManager().metaBuilder("dmreload")
            .aliases("deathmanagerreload")
            .plugin(this)
            .build();
        server.getCommandManager().register(meta, new ReloadCommand(this));

        logger.info("[DeathManager] 插件已启用");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        // 保存配置
        try {
            saveConfig();
        } catch (IOException e) {
            logger.error("[DeathManager] 保存配置文件时出错", e);
        }

        // 注销消息通道
        server.getChannelRegistrar().unregister(
            MinecraftChannelIdentifier.create("deathmanager", "message"),
            MinecraftChannelIdentifier.create("deathmanager", "sync")
        );
        logger.info("[DeathManager] 插件已禁用");
    }

    public EventSyncManager getEventSyncManager() {
        return eventSyncManager;
    }

    public void loadConfig() throws IOException {
        Path configPath = dataDirectory.resolve("config.yml");
        
        // 如果配置文件不存在，创建默认配置
        if (!Files.exists(configPath)) {
            Files.createDirectories(dataDirectory);
            try (InputStream is = getClass().getResourceAsStream("/config.yml")) {
                if (is != null) {
                    Files.copy(is, configPath);
                }
            }
        }

        // 加载配置文件
        configLoader = YamlConfigurationLoader.builder()
            .path(configPath)
            .build();
        config = configLoader.load();
        logger.info("[DeathManager] 配置文件已加载");
    }

    public void saveConfig() throws IOException {
        if (configLoader != null && config != null) {
            configLoader.save(config);
            logger.info("[DeathManager] 配置文件已保存");
        }
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public ConfigurationNode getConfig() {
        return config;
    }
}
