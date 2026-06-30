package com.yesithx.corechat.lang;

import com.yesithx.corechat.CoreChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.List;
import java.util.Map;

public class LangManager {

    private final CoreChat plugin;
    private FileConfiguration lang;

    public LangManager(CoreChat plugin) {
        this.plugin = plugin;
    }

    public void load() {
        String code = plugin.getConfig().getString("language", "es");
        String path = "lang/" + code + ".yml";
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }
        lang = YamlConfiguration.loadConfiguration(file);
        InputStream def = plugin.getResource(path);
        if (def != null) {
            lang.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(def)));
        }
    }

    public String get(String key) {
        return get(key, Map.of());
    }

    public String get(String key, Map<String, String> placeholders) {
        String prefix = color(lang.getString("prefix", "&8[CoreChat] &r"));
        String value = lang.getString(key, "&cMissing: " + key);
        for (var entry : placeholders.entrySet()) {
            value = value.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return color(prefix + value);
    }

    public String getRaw(String key) {
        return color(lang.getString(key, "&cMissing: " + key));
    }

    public String getRaw(String key, Map<String, String> placeholders) {
        String value = lang.getString(key, "&cMissing: " + key);
        for (var entry : placeholders.entrySet()) {
            value = value.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return color(value);
    }

    public List<String> getList(String key) {
        return lang.getStringList(key).stream().map(this::color).toList();
    }

    public String color(String text) {
        if (text == null) return "";
        return text.replace("&", "\u00a7");
    }
}
