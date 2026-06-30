package com.yesithx.corechat;

import com.yesithx.corechat.advertising.AntiAdvertising;
import com.yesithx.corechat.afk.AfkManager;
import com.yesithx.corechat.command.CommandRegistry;
import com.yesithx.corechat.config.ConfigManager;
import com.yesithx.corechat.filter.ChatFilter;
import com.yesithx.corechat.format.FormatProcessor;
import com.yesithx.corechat.games.ChatGamesManager;
import com.yesithx.corechat.joinguit.JoinQuitListener;
import com.yesithx.corechat.lang.LangManager;
import com.yesithx.corechat.listener.ChatListener;
import com.yesithx.corechat.log.ChatLogger;
import com.yesithx.corechat.manager.PlayerDataManager;
import com.yesithx.corechat.manager.PrefixManager;
import com.yesithx.corechat.spam.AntiSpam;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class CoreChat extends JavaPlugin {

    private ConfigManager configManager;
    private LangManager langManager;
    private PlayerDataManager playerDataManager;
    private PrefixManager prefixManager;
    private FormatProcessor formatProcessor;
    private ChatFilter chatFilter;
    private AntiSpam antiSpam;
    private AntiAdvertising antiAdvertising;
    private AfkManager afkManager;
    private ChatGamesManager chatGamesManager;
    private ChatLogger chatLogger;
    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        langManager = new LangManager(this);
        langManager.load();

        printStartupBanner();
        checkForDuplicateInstances();

        setupEconomy();

        playerDataManager = new PlayerDataManager(this);
        prefixManager = new PrefixManager(this);
        formatProcessor = new FormatProcessor(this);
        chatFilter = new ChatFilter(this);
        antiSpam = new AntiSpam(this);
        antiAdvertising = new AntiAdvertising(this);
        chatLogger = new ChatLogger(this);
        chatLogger.init();

        afkManager = new AfkManager(this);
        afkManager.start();

        chatGamesManager = new ChatGamesManager(this);
        chatGamesManager.start();

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(afkManager, this);

        new CommandRegistry(this).register();

        getLogger().info("CoreChat v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) playerDataManager.saveData();
        if (afkManager != null) afkManager.stop();
        if (chatGamesManager != null) chatGamesManager.stop();
        getLogger().info("CoreChat disabled.");
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
            getLogger().info("Economy hooked: " + economy.getName());
        }
    }

    private void printStartupBanner() {
        String version = getDescription().getVersion();
        getLogger().info("==========================================");
        getLogger().info(" CoreChat v" + version);
        getLogger().info(" Author: YesithX");
        getLogger().info(" Language: " + configManager.getLanguage());
        getLogger().info("==========================================");
    }

    private void checkForDuplicateInstances() {
        long count = getServer().getPluginManager().getPlugins().length == 0 ? 0 :
                java.util.Arrays.stream(getServer().getPluginManager().getPlugins())
                        .filter(p -> p.getName().equalsIgnoreCase("CoreChat"))
                        .count();

        if (count > 1) {
            getLogger().warning("==========================================");
            getLogger().warning(" WARNING: Multiple CoreChat jars detected!");
            getLogger().warning(" Found " + count + " instances of CoreChat in /plugins.");
            getLogger().warning(" This can cause conflicts, duplicated chat,");
            getLogger().warning(" or commands not working correctly.");
            getLogger().warning(" Please remove old/duplicate CoreChat jars");
            getLogger().warning(" and keep only the latest version.");
            getLogger().warning("==========================================");
        }
    }

    public ConfigManager getConfigManager() { return configManager; }
    public LangManager getLangManager() { return langManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public PrefixManager getPrefixManager() { return prefixManager; }
    public FormatProcessor getFormatProcessor() { return formatProcessor; }
    public ChatFilter getChatFilter() { return chatFilter; }
    public AntiSpam getAntiSpam() { return antiSpam; }
    public AntiAdvertising getAntiAdvertising() { return antiAdvertising; }
    public AfkManager getAfkManager() { return afkManager; }
    public ChatGamesManager getChatGamesManager() { return chatGamesManager; }
    public ChatLogger getChatLogger() { return chatLogger; }
    public Economy getEconomy() { return economy; }
}
