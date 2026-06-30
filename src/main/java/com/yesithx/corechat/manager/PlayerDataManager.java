package com.yesithx.corechat.manager;

import com.yesithx.corechat.CoreChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataManager {

    private final CoreChat plugin;
    private final Map<UUID, String> playerChannels = new HashMap<>();
    private final Map<UUID, Long> mutedUntil = new HashMap<>();
    private final Map<UUID, String> muteReasons = new HashMap<>();
    private final Map<UUID, UUID> lastMessageTarget = new HashMap<>();
    private final Set<UUID> socialSpyEnabled = new HashSet<>();
    private final Set<UUID> staffChatEnabled = new HashSet<>();
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();
    private final Map<UUID, String> lastMessage = new HashMap<>();
    private final Map<UUID, Set<UUID>> ignoredPlayers = new HashMap<>();
    private boolean globalMuted = false;
    private File dataFile;
    private FileConfiguration dataConfig;

    public PlayerDataManager(CoreChat plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long until = dataConfig.getLong(key + ".muted-until", 0);
                if (until > System.currentTimeMillis()) {
                    mutedUntil.put(uuid, until);
                    muteReasons.put(uuid, dataConfig.getString(key + ".mute-reason", "N/A"));
                }
                List<String> ignored = dataConfig.getStringList(key + ".ignored");
                if (!ignored.isEmpty()) {
                    Set<UUID> set = new HashSet<>();
                    for (String s : ignored) {
                        try { set.add(UUID.fromString(s)); } catch (Exception ignored2) {}
                    }
                    ignoredPlayers.put(uuid, set);
                }
            } catch (Exception ignored) {}
        }
    }

    public void saveData() {
        for (var entry : mutedUntil.entrySet()) {
            String key = entry.getKey().toString();
            dataConfig.set(key + ".muted-until", entry.getValue());
            dataConfig.set(key + ".mute-reason", muteReasons.getOrDefault(entry.getKey(), "N/A"));
        }
        for (var entry : ignoredPlayers.entrySet()) {
            List<String> list = entry.getValue().stream().map(UUID::toString).toList();
            dataConfig.set(entry.getKey().toString() + ".ignored", list);
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void setChannel(UUID uuid, String channel) { playerChannels.put(uuid, channel); }
    public String getChannel(UUID uuid) { return playerChannels.getOrDefault(uuid, plugin.getConfigManager().getDefaultChannel()); }

    public void mutePlayer(UUID uuid, int seconds, String reason) {
        mutedUntil.put(uuid, System.currentTimeMillis() + seconds * 1000L);
        muteReasons.put(uuid, reason);
        saveData();
    }

    public void unmutePlayer(UUID uuid) {
        mutedUntil.remove(uuid);
        muteReasons.remove(uuid);
        saveData();
    }

    public boolean isMuted(UUID uuid) {
        if (!mutedUntil.containsKey(uuid)) return false;
        if (mutedUntil.get(uuid) > System.currentTimeMillis()) return true;
        mutedUntil.remove(uuid);
        return false;
    }

    public long getMuteTimeRemaining(UUID uuid) {
        if (!isMuted(uuid)) return 0;
        return (mutedUntil.get(uuid) - System.currentTimeMillis()) / 1000;
    }

    public String getMuteReason(UUID uuid) { return muteReasons.getOrDefault(uuid, "N/A"); }
    public void setLastTarget(UUID sender, UUID target) { lastMessageTarget.put(sender, target); }
    public UUID getLastTarget(UUID sender) { return lastMessageTarget.get(sender); }
    public boolean isSocialSpyEnabled(UUID uuid) { return socialSpyEnabled.contains(uuid); }
    public void toggleSocialSpy(UUID uuid) {
        if (socialSpyEnabled.contains(uuid)) socialSpyEnabled.remove(uuid);
        else socialSpyEnabled.add(uuid);
    }
    public Set<UUID> getSocialSpyPlayers() { return Collections.unmodifiableSet(socialSpyEnabled); }
    public boolean isStaffChatEnabled(UUID uuid) { return staffChatEnabled.contains(uuid); }
    public void toggleStaffChat(UUID uuid) {
        if (staffChatEnabled.contains(uuid)) staffChatEnabled.remove(uuid);
        else staffChatEnabled.add(uuid);
    }
    public boolean isGlobalMuted() { return globalMuted; }
    public void toggleGlobalMute() { globalMuted = !globalMuted; }

    public boolean checkCooldown(UUID uuid) {
        if (!plugin.getConfigManager().isCooldownEnabled()) return false;
        long now = System.currentTimeMillis();
        long last = lastMessageTime.getOrDefault(uuid, 0L);
        long diff = (now - last) / 1000;
        return diff < plugin.getConfigManager().getCooldownSeconds();
    }

    public long getCooldownRemaining(UUID uuid) {
        long now = System.currentTimeMillis();
        long last = lastMessageTime.getOrDefault(uuid, 0L);
        return plugin.getConfigManager().getCooldownSeconds() - (now - last) / 1000;
    }

    public void updateCooldown(UUID uuid) { lastMessageTime.put(uuid, System.currentTimeMillis()); }

    public boolean checkFlood(UUID uuid, String message) {
        if (!plugin.getConfigManager().isFloodEnabled()) return false;
        String last = lastMessage.getOrDefault(uuid, "");
        return similarity(last, message) >= plugin.getConfigManager().getFloodMinSimilarity();
    }

    public void updateLastMessage(UUID uuid, String message) { lastMessage.put(uuid, message); }

    private int similarity(String s1, String s2) {
        if (s1.isEmpty() || s2.isEmpty()) return 0;
        int matches = 0;
        String shorter = s1.length() <= s2.length() ? s1 : s2;
        String longer = s1.length() > s2.length() ? s1 : s2;
        for (char c : shorter.toCharArray()) {
            if (longer.indexOf(c) >= 0) matches++;
        }
        return (int) ((matches * 2.0 / (s1.length() + s2.length())) * 100);
    }

    public boolean isIgnoring(UUID uuid, UUID target) {
        return ignoredPlayers.getOrDefault(uuid, Set.of()).contains(target);
    }

    public void addIgnore(UUID uuid, UUID target) {
        ignoredPlayers.computeIfAbsent(uuid, k -> new HashSet<>()).add(target);
        saveData();
    }

    public void removeIgnore(UUID uuid, UUID target) {
        ignoredPlayers.getOrDefault(uuid, new HashSet<>()).remove(target);
        saveData();
    }

    public Set<UUID> getIgnored(UUID uuid) {
        return Collections.unmodifiableSet(ignoredPlayers.getOrDefault(uuid, Set.of()));
    }
}
