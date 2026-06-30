package com.yesithx.corechat.joinguit;

import com.yesithx.corechat.CoreChat;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    private final CoreChat plugin;

    public JoinQuitListener(CoreChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfigManager().isJoinQuitEnabled()) return;
        Player player = event.getPlayer();
        event.setJoinMessage(null);

        if (!plugin.getConfigManager().isJoinEnabled()) return;

        String prefix = plugin.getPrefixManager().getPrefix(player);
        boolean firstJoin = !player.hasPlayedBefore();

        String format = firstJoin
                ? plugin.getConfigManager().getFirstJoinFormat()
                : plugin.getConfigManager().getJoinFormat();

        String msg = plugin.getLangManager().color(format
                .replace("{player}", player.getName())
                .replace("{prefix}", prefix)
                .replace("{displayname}", player.getDisplayName()));

        if (plugin.getConfigManager().isJoinBroadcast()) {
            plugin.getServer().broadcastMessage(msg);
        }

        if (plugin.getConfigManager().isJoinSoundEnabled()) {
            try {
                Sound sound = Sound.valueOf(plugin.getConfigManager().getJoinSound());
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
                }
            } catch (Exception ignored) {}
        }

        if (plugin.getConfigManager().isJoinTitleEnabled()) {
            String title = plugin.getLangManager().color(
                    plugin.getConfigManager().getJoinTitle());
            String subtitle = plugin.getLangManager().color(
                    plugin.getConfigManager().getJoinSubtitle()
                            .replace("{player}", player.getName()));
            player.sendTitle(title, subtitle, 10, 60, 20);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        if (!plugin.getConfigManager().isJoinQuitEnabled()) return;
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        if (!plugin.getConfigManager().isQuitEnabled()) return;

        String prefix = plugin.getPrefixManager().getPrefix(player);
        String msg = plugin.getLangManager().color(
                plugin.getConfigManager().getQuitFormat()
                        .replace("{player}", player.getName())
                        .replace("{prefix}", prefix)
                        .replace("{displayname}", player.getDisplayName()));

        if (plugin.getConfigManager().isQuitBroadcast()) {
            plugin.getServer().broadcastMessage(msg);
        }

        plugin.getAfkManager().removePlayer(player.getUniqueId());
    }
}
