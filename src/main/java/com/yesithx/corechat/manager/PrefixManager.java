package com.yesithx.corechat.manager;

import com.yesithx.corechat.CoreChat;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PrefixManager {

    private final CoreChat plugin;
    private LuckPerms luckPerms;
    private Chat vaultChat;

    public PrefixManager(CoreChat plugin) {
        this.plugin = plugin;
        setupLuckPerms();
        setupVault();
    }

    private void setupLuckPerms() {
        try {
            RegisteredServiceProvider<LuckPerms> rsp =
                    plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (rsp != null) {
                luckPerms = rsp.getProvider();
                plugin.getLogger().info("LuckPerms hooked successfully.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("LuckPerms not found.");
        }
    }

    private void setupVault() {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return;
            RegisteredServiceProvider<Chat> rsp =
                    plugin.getServer().getServicesManager().getRegistration(Chat.class);
            if (rsp != null) {
                vaultChat = rsp.getProvider();
                plugin.getLogger().info("Vault Chat hooked successfully.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Vault Chat not found.");
        }
    }

    public String getPrefix(Player player) {
        if (luckPerms != null && plugin.getConfigManager().isShowLuckPermsPrefix()) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix != null && !prefix.isEmpty()) return prefix;
            }
        }
        if (vaultChat != null) {
            String prefix = vaultChat.getPlayerPrefix(player);
            if (prefix != null && !prefix.isEmpty()) return prefix;
        }
        return "";
    }

    public String getSuffix(Player player) {
        if (luckPerms != null && plugin.getConfigManager().isShowLuckPermsSuffix()) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String suffix = user.getCachedData().getMetaData().getSuffix();
                if (suffix != null && !suffix.isEmpty()) return suffix;
            }
        }
        if (vaultChat != null) {
            String suffix = vaultChat.getPlayerSuffix(player);
            if (suffix != null && !suffix.isEmpty()) return suffix;
        }
        return "";
    }

    public String getGroup(Player player) {
        if (luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) return user.getPrimaryGroup();
        }
        if (vaultChat != null) return vaultChat.getPrimaryGroup(player);
        return "default";
    }
}
