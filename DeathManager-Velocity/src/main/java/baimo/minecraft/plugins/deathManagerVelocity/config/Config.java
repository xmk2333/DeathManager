package baimo.minecraft.plugins.deathManagerVelocity.config;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private final Path configPath;
    private final Logger logger;
    private ConfigurationNode config;

    @Inject
    public Config(@DataDirectory Path dataDirectory, Logger logger) {
        this.logger = logger;
        this.configPath = dataDirectory.resolve("config.yml");
        loadConfig();
    }

    private void loadConfig() {
        try {
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());
                Files.copy(getClass().getResourceAsStream("/config.yml"), configPath);
            }

            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(configPath.toFile())
                    .build();
            config = loader.load();
        } catch (IOException e) {
            logger.error("无法加载配置文件", e);
        }
    }

    public void saveConfig() {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(configPath.toFile())
                    .build();
            loader.save(config);
        } catch (IOException e) {
            logger.error("无法保存配置文件", e);
        }
    }

    public ConfigurationNode getConfig() {
        return config;
    }
} 