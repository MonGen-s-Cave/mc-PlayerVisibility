package hu.kxtsoo.playervisibility.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import hu.kxtsoo.playervisibility.PlayerVisibility;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ConfigUtil {

    private final PlayerVisibility plugin;
    public static ConfigUtil configUtil;
    private YamlDocument config;
    private YamlDocument messages;

    public ConfigUtil(PlayerVisibility plugin) {
        this.plugin = plugin;
        setupConfig();
        setupMessages();
    }

    public void setupConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                plugin.saveResource("config.yml", false);
            }

            config = YamlDocument.create(configFile,
                    Objects.requireNonNull(plugin.getResource("config.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setKeepAll(true)
                            .setVersioning(new BasicVersioning("config-version")).build());

            config.update();
        } catch (IOException ex) {
            plugin.getLogger().severe("Error loading or creating config.yml: " + ex.getMessage());
        }
    }

    public void setupMessages() {
        generateDefaultLocales();

        String locale = config.getString("locale", "en");
        File messagesFile = new File(plugin.getDataFolder() + File.separator + "locale", locale + ".yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("locale" + File.separator + locale + ".yml", false);
        }

        try {
            messages = YamlDocument.create(messagesFile,
                    Objects.requireNonNull(plugin.getResource("locale/" + locale + ".yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("messages-version"))
                            .setKeepAll(true)
                            .build());

            messages.update();
        } catch (IOException ex) {
            plugin.getLogger().severe("Error loading or creating message files " + ex.getMessage());
        }
    }

    private void generateDefaultLocales() {
        String[] locales = {"en", "hu"};
        for (String locale : locales) {
            File localeFile = new File(plugin.getDataFolder(), "locale" + File.separator + locale + ".yml");
            if (!localeFile.exists()) {
                plugin.saveResource("locale" + File.separator + locale + ".yml", false);
            }
        }
    }

    public String getMessage(String key) {
        Object messageObj = messages.get(key, "Message not found");

        if (messageObj instanceof String) {
            String message = ChatUtil.colorizeHex((String) messageObj);
            String prefix = ChatUtil.colorizeHex(config.getString("prefix", ""));
            if (message.contains("%prefix%")) {
                return message.replace("%prefix%", prefix);
            }
            return message;
        } else if (messageObj instanceof List) {
            List<String> messageList = (List<String>) messageObj;
            String prefix = ChatUtil.colorizeHex(config.getString("prefix", ""));
            messageList = messageList.stream()
                    .map(ChatUtil::colorizeHex)
                    .map(msg -> msg.contains("%prefix%") ? msg.replace("%prefix%", prefix) : msg)
                    .toList();
            return String.join("\n", messageList);
        }

        return "Invalid message format";
    }

    public YamlDocument getConfig() {
        return config;
    }

    public void reloadConfig() {
        setupConfig();
        setupMessages();
    }
}