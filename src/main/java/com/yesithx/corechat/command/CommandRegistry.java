package com.yesithx.corechat.command;

import com.yesithx.corechat.CoreChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandRegistry {

    private final CoreChat plugin;

    public CommandRegistry(CoreChat plugin) {
        this.plugin = plugin;
    }

    public void register() {
        registerCommand("corechat", new CoreChatCommand());
        registerCommand("msg", new MsgCommand());
        registerCommand("reply", new ReplyCommand());
        registerCommand("broadcast", new BroadcastCommand());
        registerCommand("mute", new MuteCommand());
        registerCommand("unmute", new UnmuteCommand());
        registerCommand("mutechat", new MuteChatCommand());
        registerCommand("clearchat", new ClearChatCommand());
        registerCommand("channel", new ChannelCommand());
        registerCommand("socialspy", new SocialSpyCommand());
        registerCommand("staffchat", new StaffChatCommand());
        registerCommand("ignore", new IgnoreCommand());
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand cmd = plugin.getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            if (executor instanceof TabCompleter tc) cmd.setTabCompleter(tc);
        }
    }

    private boolean noPermission(CommandSender sender, String perm) {
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(plugin.getLangManager().get("general.no-permission"));
            return true;
        }
        return false;
    }

    private boolean playerOnly(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangManager().get("general.player-only"));
            return true;
        }
        return false;
    }

    class CoreChatCommand implements CommandExecutor, TabCompleter {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (noPermission(s, "corechat.admin")) return true;
            if (a.length == 0 || a[0].equalsIgnoreCase("help")) {
                plugin.getLangManager().getList("help.entries").forEach(s::sendMessage);
                return true;
            }
            if (a[0].equalsIgnoreCase("reload")) {
                plugin.getConfigManager().reload();
                plugin.getLangManager().load();
                plugin.getChatFilter().compileRegex();
                s.sendMessage(plugin.getLangManager().get("general.reloaded"));
            } else if (a[0].equalsIgnoreCase("info")) {
                s.sendMessage(plugin.getLangManager().get("prefix") + "CoreChat v" + plugin.getDescription().getVersion() + " by YesithX");
            }
            return true;
        }
        public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            return a.length == 1 ? Arrays.asList("reload", "info", "help") : List.of();
        }
    }

    class MsgCommand implements CommandExecutor, TabCompleter {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (playerOnly(s)) return true;
            if (noPermission(s, "corechat.msg")) return true;
            Player sender = (Player) s;
            if (a.length < 2) { sender.sendMessage(plugin.getLangManager().get("general.invalid-usage", Map.of("usage", "/msg <player> <message>"))); return true; }
            Player target = Bukkit.getPlayer(a[0]);
            if (target == null) { sender.sendMessage(plugin.getLangManager().get("general.player-not-found", Map.of("player", a[0]))); return true; }
            if (target.equals(sender)) { sender.sendMessage(plugin.getLangManager().get("private-message.self-message")); return true; }
            if (plugin.getPlayerDataManager().isIgnoring(target.getUniqueId(), sender.getUniqueId())) { sender.sendMessage(plugin.getLangManager().get("private-message.ignored")); return true; }
            if (plugin.getPlayerDataManager().isIgnoring(sender.getUniqueId(), target.getUniqueId())) { sender.sendMessage(plugin.getLangManager().get("private-message.ignoring")); return true; }
            String message = String.join(" ", Arrays.copyOfRange(a, 1, a.length));
            sendPrivateMessage(sender, target, message);
            return true;
        }
        public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (a.length == 1) return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(a[0].toLowerCase())).toList();
            return List.of();
        }
    }

    class ReplyCommand implements CommandExecutor {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (playerOnly(s)) return true;
            if (noPermission(s, "corechat.msg")) return true;
            Player sender = (Player) s;
            if (a.length < 1) { sender.sendMessage(plugin.getLangManager().get("general.invalid-usage", Map.of("usage", "/r <message>"))); return true; }
            UUID targetId = plugin.getPlayerDataManager().getLastTarget(sender.getUniqueId());
            if (targetId == null) { sender.sendMessage(plugin.getLangManager().get("private-message.no-reply")); return true; }
            Player target = Bukkit.getPlayer(targetId);
            if (target == null) { sender.sendMessage(plugin.getLangManager().get("private-message.offline")); return true; }
            String message = String.join(" ", a);
            sendPrivateMessage(sender, target, message);
            return true;
        }
    }

    class BroadcastCommand implements CommandExecutor {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (noPermission(s, "corechat.broadcast")) return true;
            if (a.length < 1) { s.sendMessage(plugin.getLangManager().get("general.invalid-usage", Map.of("usage", "/bc <message>"))); return true; }
            String message = String.join(" ", a);
            String format = plugin.getConfigManager().getBroadcastFormat().replace("%message}", message);
            Component component = plugin.getFormatProcessor().toComponent(format);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(component);
                if (plugin.getConfigManager().isBroadcastSoundEnabled()) {
                    try { p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfigManager().getBroadcastSound()), 1.0f, 1.0f); } catch (Exception ignored) {}
                }
            }
            return true;
        }
    }

    class MuteCommand implements CommandExecutor, TabCompleter {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (noPermission(s, "corechat.mute")) return true;
            if (a.length < 1) { s.sendMessage(plugin.getLangManager().get("general.invalid-usage", Map.of("usage", "/mute <player> [seconds] [reason]"))); return true; }
            Player target = Bukkit.getPlayer(a[0]);
            if (target == null) { s.sendMessage(plugin.getLangManager().get("general.player-not-found", Map.of("player", a[0]))); return true; }
            if (plugin.getPlayerDataManager().isMuted(target.getUniqueId())) { s.sendMessage(plugin.getLangManager().get("mute.already-muted")); return true; }
            int duration = a.length > 1 ? parseInt(a[1], plugin.getConfigManager().getMuteDefaultDuration()) : plugin.getConfigManager().getMuteDefaultDuration();
            String reason = a.length > 2 ? String.join(" ", Arrays.copyOfRange(a, 2, a.length)) : "N/A";
            plugin.getPlayerDataManager().mutePlayer(target.getUniqueId(), duration, reason);
            s.sendMessage(plugin.getLangManager().get("mute.muted", Map.of("player", target.getName(), "duration", String.valueOf(duration))));
            target.sendMessage(plugin.getLangManager().get("mute.notify-muted", Map.of("duration", String.valueOf(duration), "reason", reason)));
            return true;
        }
        public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (a.length == 1) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            return List.of();
        }
    }

    class UnmuteCommand implements CommandExecutor, TabCompleter {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (noPermission(s, "corechat.mute")) return true;
            if (a.length < 1) { s.sendMessage(plugin.getLangManager().get("general.invalid-usage", Map.of("usage", "/unmute <player>"))); return true; }
            Player target = Bukkit.getPlayer(a[0]);
            if (target == null) { s.sendMessage(plugin.getLangManager().get("general.player-not-found", Map.of("player", a[0]))); return true; }
            if (!plugin.getPlayerDataManager().isMuted(target.getUniqueId())) { s.sendMessage(plugin.getLangManager().get("mute.not-muted")); return true; }
            plugin.getPlayerDataManager().unmutePlayer(target.getUniqueId());
            s.sendMessage(plugin.getLangManager().get("mute.unmuted", Map.of("player", target.getName())));
            target.sendMessage(plugin.getLangManager().get("mute.notify-unmuted"));
            return true;
        }
        public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (a.length == 1) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            return List.of();
        }
    }

    class MuteChatCommand implements CommandExecutor {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (noPermission(s, "corechat.mutechat")) return true;
            plugin.getPlayerDataManager().toggleGlobalMute();
            if (plugin.getPlayerDataManager().isGlobalMuted()) {
                Bukkit.broadcast(net.kyori.adventure.text.Component.text(plugin.getLangManager().get("mute.global-muted")));
            } else {
                Bukkit.broadcast(net.kyori.adventure.text.Component.text(plugin.getLangManager().get("mute.global-unmuted")));
            }
            return true;
        }
    }

    class ClearChatCommand implements CommandExecutor {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (noPermission(s, "corechat.clearchat")) return true;
            String lines = "\n".repeat(plugin.getConfigManager().getClearChatLines());
            String name = s instanceof Player p ? p.getName() : "Console";
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(lines);
                p.sendMessage(plugin.getLangManager().get("clearchat.broadcast"));
            }
            return true;
        }
    }

    class ChannelCommand implements CommandExecutor, TabCompleter {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (playerOnly(s)) return true;
            Player player = (Player) s;
            if (a.length == 0) {
                player.sendMessage(plugin.getLangManager().getRaw("channel.list-header"));
                for (String ch : plugin.getConfigManager().getChannelNames()) {
                    if (!plugin.getConfigManager().isChannelEnabled(ch)) continue;
                    player.sendMessage(plugin.getLangManager().getRaw("channel.list-entry",
                            Map.of("symbol", plugin.getConfigManager().getChannelSymbol(ch), "name", ch, "description", ch)));
                }
                player.sendMessage(plugin.getLangManager().getRaw("channel.list-footer"));
                return true;
            }
            String channel = a[0].toLowerCase();
            if (!plugin.getConfigManager().getChannelNames().contains(channel)) {
                player.sendMessage(plugin.getLangManager().get("channel.not-found")); return true;
            }
            String perm = plugin.getConfigManager().getChannelPermission(channel);
            if (perm != null && !perm.isEmpty() && !player.hasPermission(perm)) {
                player.sendMessage(plugin.getLangManager().get("channel.no-permission")); return true;
            }
            plugin.getPlayerDataManager().setChannel(player.getUniqueId(), channel);
            player.sendMessage(plugin.getLangManager().get("channel.switched", Map.of("channel", channel)));
            return true;
        }
        public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (a.length == 1) return new ArrayList<>(plugin.getConfigManager().getChannelNames());
            return List.of();
        }
    }

    class SocialSpyCommand implements CommandExecutor {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (playerOnly(s)) return true;
            if (noPermission(s, "corechat.socialspy")) return true;
            Player player = (Player) s;
            plugin.getPlayerDataManager().toggleSocialSpy(player.getUniqueId());
            if (plugin.getPlayerDataManager().isSocialSpyEnabled(player.getUniqueId())) {
                player.sendMessage(plugin.getLangManager().get("socialspy.enabled"));
            } else {
                player.sendMessage(plugin.getLangManager().get("socialspy.disabled"));
            }
            return true;
        }
    }

    class StaffChatCommand implements CommandExecutor {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (playerOnly(s)) return true;
            if (noPermission(s, "corechat.staffchat")) return true;
            Player player = (Player) s;
            plugin.getPlayerDataManager().toggleStaffChat(player.getUniqueId());
            if (plugin.getPlayerDataManager().isStaffChatEnabled(player.getUniqueId())) {
                player.sendMessage(plugin.getLangManager().get("staffchat.enabled"));
            } else {
                player.sendMessage(plugin.getLangManager().get("staffchat.disabled"));
            }
            return true;
        }
    }

    class IgnoreCommand implements CommandExecutor, TabCompleter {
        public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (playerOnly(s)) return true;
            if (noPermission(s, "corechat.ignore")) return true;
            Player player = (Player) s;
            if (a.length == 0) {
                player.sendMessage(plugin.getLangManager().getRaw("ignore.list-header"));
                Set<UUID> ignored = plugin.getPlayerDataManager().getIgnored(player.getUniqueId());
                if (ignored.isEmpty()) { player.sendMessage(plugin.getLangManager().getRaw("ignore.list-empty")); return true; }
                for (UUID id : ignored) {
                    String name = Bukkit.getOfflinePlayer(id).getName();
                    player.sendMessage(plugin.getLangManager().getRaw("ignore.list-entry", Map.of("player", name != null ? name : id.toString())));
                }
                return true;
            }
            Player target = Bukkit.getPlayer(a[0]);
            if (target == null) { player.sendMessage(plugin.getLangManager().get("general.player-not-found", Map.of("player", a[0]))); return true; }
            if (target.equals(player)) { player.sendMessage(plugin.getLangManager().get("ignore.self")); return true; }
            if (plugin.getPlayerDataManager().isIgnoring(player.getUniqueId(), target.getUniqueId())) {
                plugin.getPlayerDataManager().removeIgnore(player.getUniqueId(), target.getUniqueId());
                player.sendMessage(plugin.getLangManager().get("ignore.removed", Map.of("player", target.getName())));
            } else {
                plugin.getPlayerDataManager().addIgnore(player.getUniqueId(), target.getUniqueId());
                player.sendMessage(plugin.getLangManager().get("ignore.added", Map.of("player", target.getName())));
            }
            return true;
        }
        public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
            if (a.length == 1) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            return List.of();
        }
    }

    private void sendPrivateMessage(Player sender, Player target, String message) {
        String formatSender = plugin.getConfigManager().getMsgFormatSender()
                .replace("%target}", target.getName()).replace("%message}", message);
        String formatReceiver = plugin.getConfigManager().getMsgFormatReceiver()
                .replace("%sender}", sender.getName()).replace("%message}", message);
        String formatSpy = plugin.getConfigManager().getMsgFormatSocialSpy()
                .replace("%sender}", sender.getName()).replace("%target}", target.getName()).replace("%message}", message);

        sender.sendMessage(plugin.getFormatProcessor().toComponent(formatSender));
        target.sendMessage(plugin.getFormatProcessor().toComponent(formatReceiver));

        if (plugin.getConfigManager().isMsgSoundEnabled()) {
            try { target.playSound(target.getLocation(), Sound.valueOf(plugin.getConfigManager().getMsgSound()), 1.0f, 1.0f); } catch (Exception ignored) {}
        }

        plugin.getPlayerDataManager().setLastTarget(sender.getUniqueId(), target.getUniqueId());
        plugin.getPlayerDataManager().setLastTarget(target.getUniqueId(), sender.getUniqueId());

        Component spyComponent = plugin.getFormatProcessor().toComponent(formatSpy);
        for (UUID spyId : plugin.getPlayerDataManager().getSocialSpyPlayers()) {
            Player spy = Bukkit.getPlayer(spyId);
            if (spy != null && !spy.equals(sender) && !spy.equals(target)) {
                spy.sendMessage(spyComponent);
            }
        }
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
