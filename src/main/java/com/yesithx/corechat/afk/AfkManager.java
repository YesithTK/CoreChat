package com.yesithx.corechat.afk;

import com.yesithx.corechat.CoreChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AfkManager implements Listener {

    private final CoreChat plugin;
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final Set<UUID> afkPlayers = new HashSet<>();
    private BukkitTask checkTask;

    public AfkManager(CoreChat plugin) {
        this.plugin = plugin;
    }

    public void start() {
        checkTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkAfk, 200L, 200L);
    }

    public void stop() {
        if (checkTask != null) checkTask.cancel();
    }

    private void checkAfk() {
        if (!plugin.getConfigManager().isAfkEnabled()) return;
        long timeout = plugin.getConfigManager().getAfkTimeoutSeconds() * 1000L;
        long kickAfter = plugin.getConfigManager().getAfkKickAfterSeconds() * 1000L;
        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("corechat.bypass.afk")) continue;
            UUID uuid = player.getUniqueId();
            long last = lastActivity.getOrDefault(uuid, now);
            long idle = now - last;

            if (!afkPlayers.contains(uuid) && idle >= timeout) {
                setAfk(player, true);
            }

            if (kickAfter > 0 && afkPlayers.contains(uuid) && idle >= kickAfter) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.kickPlayer(plugin.getLangManager().color(
                                plugin.getConfigManager().getAfkKickReason())));
            }
        }
    }

    public void setAfk(Player player, boolean afk) {
        UUID uuid = player.getUniqueId();
        if (afk && !afkPlayers.contains(uuid)) {
            afkPlayers.add(uuid);
            if (plugin.getConfigManager().isAfkBroadcast()) {
                String msg = plugin.getLangManager().color(
                        plugin.getConfigManager().getAfkBroadcastFormat()
                                .replace("{player}", player.getName()));
                Bukkit.broadcast(net.kyori.adventure.text.Component.text(msg));
            }
        } else if (!afk && afkPlayers.contains(uuid)) {
            afkPlayers.remove(uuid);
            lastActivity.put(uuid, System.currentTimeMillis());
            if (plugin.getConfigManager().isAfkBroadcast()) {
                String msg = plugin.getLangManager().color(
                        plugin.getConfigManager().getAfkBroadcastReturnFormat()
                                .replace("{player}", player.getName()));
                Bukkit.broadcast(net.kyori.adventure.text.Component.text(msg));
            }
        }
    }

    public boolean isAfk(UUID uuid) { return afkPlayers.contains(uuid); }

    public void updateActivity(UUID uuid) {
        boolean wasAfk = afkPlayers.contains(uuid);
        lastActivity.put(uuid, System.currentTimeMillis());
        if (wasAfk) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) setAfk(player, false);
        }
    }

    public void removePlayer(UUID uuid) {
        afkPlayers.remove(uuid);
        lastActivity.remove(uuid);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.hasChangedBlock()) return;
        updateActivity(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        updateActivity(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        updateActivity(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        lastActivity.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removePlayer(e.getPlayer().getUniqueId());
    }
}
