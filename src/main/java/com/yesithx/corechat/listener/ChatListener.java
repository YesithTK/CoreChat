package com.yesithx.corechat.listener;

import com.yesithx.corechat.CoreChat;
import com.yesithx.corechat.filter.ChatFilter;
import com.yesithx.corechat.spam.AntiSpam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;

public class ChatListener implements Listener {

    private final CoreChat plugin;
    private final LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();

    public ChatListener(CoreChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        event.setCancelled(true);

        if (plugin.getPlayerDataManager().isGlobalMuted()
                && !player.hasPermission("corechat.bypass.mutechat")) {
            player.sendMessage(plugin.getLangManager().get("chat.muted-global"));
            return;
        }

        if (plugin.getPlayerDataManager().isMuted(player.getUniqueId())) {
            long remaining = plugin.getPlayerDataManager().getMuteTimeRemaining(player.getUniqueId());
            player.sendMessage(plugin.getLangManager().get("chat.muted-personal",
                    Map.of("time", String.valueOf(remaining))));
            return;
        }

        if (!player.hasPermission("corechat.bypass.cooldown")
                && plugin.getPlayerDataManager().checkCooldown(player.getUniqueId())) {
            long remaining = plugin.getPlayerDataManager().getCooldownRemaining(player.getUniqueId());
            player.sendMessage(plugin.getLangManager().get("chat.cooldown",
                    Map.of("time", String.valueOf(remaining))));
            return;
        }

        if (!player.hasPermission("corechat.bypass.spam")) {
            AntiSpam.SpamResult spamResult = plugin.getAntiSpam().check(player.getUniqueId());
            if (spamResult == AntiSpam.SpamResult.SPAM) {
                player.sendMessage(plugin.getLangManager().get("spam.blocked"));
                String action = plugin.getConfigManager().getSpamAction();
                if (action.equals("mute")) {
                    int duration = plugin.getConfigManager().getSpamMuteDuration();
                    plugin.getPlayerDataManager().mutePlayer(player.getUniqueId(), duration, "Auto-mute: spam");
                    player.sendMessage(plugin.getLangManager().get("spam.muted",
                            Map.of("duration", String.valueOf(duration))));
                }
                plugin.getChatLogger().logBlocked(player.getName(), message, "SPAM");
                return;
            }
        }

        if (!player.hasPermission("corechat.bypass.advertising")
                && plugin.getAntiAdvertising().isAdvertising(message)) {
            String action = plugin.getConfigManager().getAdvertisingAction();
            if (action.equals("block")) {
                player.sendMessage(plugin.getLangManager().get("advertising.blocked"));
                plugin.getChatLogger().logBlocked(player.getName(), message, "ADVERTISING");
                return;
            }
            player.sendMessage(plugin.getLangManager().get("advertising.warned"));
        }

        if (!player.hasPermission("corechat.bypass.flood")
                && plugin.getPlayerDataManager().checkFlood(player.getUniqueId(), message)) {
            player.sendMessage(plugin.getLangManager().get("chat.flood-blocked"));
            plugin.getChatLogger().logBlocked(player.getName(), message, "FLOOD");
            return;
        }

        if (!player.hasPermission("corechat.bypass.caps")) {
            message = processCaps(player, message);
            if (message == null) return;
        }

        if (!player.hasPermission("corechat.bypass.filter")) {
            ChatFilter.FilterResult result = plugin.getChatFilter().process(player.getUniqueId(), message);
            if (result.triggered()) {
                if (plugin.getConfigManager().isFilterLogFiltered()) {
                    plugin.getChatLogger().logBlocked(player.getName(), message, "FILTER");
                }
                if (plugin.getConfigManager().isFilterWarnPlayer()) {
                    player.sendMessage(plugin.getLangManager().get("filter.warned",
                            Map.of("count", String.valueOf(result.warnCount()),
                                    "max", String.valueOf(result.warnThreshold()))));
                }
                if (result.isBlocked()) return;
                if (result.shouldMute()) {
                    int duration = plugin.getConfigManager().getFilterMuteDuration();
                    plugin.getPlayerDataManager().mutePlayer(player.getUniqueId(), duration, "Auto-mute: filter");
                    player.sendMessage(plugin.getLangManager().get("filter.muted",
                            Map.of("duration", String.valueOf(duration))));
                    return;
                }
                message = result.processedMessage();
            }
        }

        if (plugin.getChatGamesManager().checkMathAnswer(player, message)) return;
        if (plugin.getChatGamesManager().checkUnscrambleAnswer(player, message)) return;

        if (plugin.getAfkManager().isAfk(player.getUniqueId())) {
            plugin.getAfkManager().setAfk(player, false);
        }

        final String finalMessage = message;

        if (plugin.getPlayerDataManager().isStaffChatEnabled(player.getUniqueId())
                && player.hasPermission("corechat.staffchat")) {
            Bukkit.getScheduler().runTask(plugin, () -> sendToChannel(player, "staff", finalMessage));
        } else {
            String channel = plugin.getPlayerDataManager().getChannel(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> sendToChannel(player, channel, finalMessage));
        }

        plugin.getPlayerDataManager().updateCooldown(player.getUniqueId());
        plugin.getPlayerDataManager().updateLastMessage(player.getUniqueId(), message);
    }

    private void sendToChannel(Player player, String channel, String message) {
        String format = plugin.getConfigManager().getChannelFormat(channel);
        int range = plugin.getConfigManager().getChannelRange(channel);

        Component msgComponent = plugin.getFormatProcessor().processMessage(player, message);
        String msgLegacy = legacySection.serialize(msgComponent);

        String mentionProcessed = processMentionText(msgLegacy, player);

        String formatted = format.replace("%message%", mentionProcessed);
        String resolved = plugin.getFormatProcessor().buildFormat(player, formatted);
        Component finalComponent = plugin.getFormatProcessor().toComponent(resolved);

        for (Player receiver : Bukkit.getOnlinePlayers()) {
            if (plugin.getConfigManager().isIgnoreBlockChat()
                    && plugin.getPlayerDataManager().isIgnoring(receiver.getUniqueId(), player.getUniqueId())) {
                continue;
            }
            if (range > 0) {
                if (!receiver.getWorld().equals(player.getWorld())) continue;
                if (receiver.getLocation().distance(player.getLocation()) > range) continue;
            }
            receiver.sendMessage(finalComponent);
        }

        if (plugin.getConfigManager().isChannelLog(channel)) {
            plugin.getChatLogger().logChat(player.getWorld().getName(), player.getName(), message);
        }

        plugin.getServer().getConsoleSender().sendMessage(
                plugin.getLangManager().color(
                        plugin.getConfigManager().getConsoleChatFormat()
                                .replace("%player%", player.getName())
                                .replace("%message%", message)
                                .replace("%world%", player.getWorld().getName())));

        processMentionSounds(message, player);
    }

    private String processMentionText(String message, Player sender) {
        if (!plugin.getConfigManager().isMentionEnabled()) return message;
        String symbol = plugin.getConfigManager().getMentionSymbol();
        String mentionFormat = plugin.getLangManager().color(
                plugin.getConfigManager().getMentionFormat());
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(sender)) continue;
            message = message.replace(symbol + online.getName(),
                    mentionFormat.replace("%player%", online.getName()));
        }
        return message;
    }

    private void processMentionSounds(String message, Player sender) {
        if (!plugin.getConfigManager().isMentionEnabled()) return;
        String symbol = plugin.getConfigManager().getMentionSymbol();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(sender)) continue;
            if (message.contains(symbol + online.getName())) {
                try {
                    online.playSound(online.getLocation(),
                            Sound.valueOf(plugin.getConfigManager().getMentionSound()), 1.0f, 1.0f);
                } catch (Exception ignored) {}
            }
        }
    }

    private String processCaps(Player player, String message) {
        if (!plugin.getConfigManager().isCapsEnabled()) return message;
        if (message.length() < plugin.getConfigManager().getCapsMinLength()) return message;
        long caps = message.chars().filter(Character::isUpperCase).count();
        int percent = (int) ((caps * 100.0) / message.length());
        if (percent > plugin.getConfigManager().getCapsMaxPercent()) {
            if (plugin.getConfigManager().isCapsNotify()) {
                player.sendMessage(plugin.getLangManager().get("chat.caps-replaced"));
            }
            if (plugin.getConfigManager().getCapsAction().equals("block")) return null;
            return message.toLowerCase();
        }
        return message;
    }
}
