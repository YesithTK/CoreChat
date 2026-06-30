package com.yesithx.corechat.format;

import com.yesithx.corechat.CoreChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FormatProcessor {

    private final CoreChat plugin;
    private final MiniMessage miniMessage;
    private MiniMessage restrictedMiniMessage;
    private final LegacyComponentSerializer legacyAmpersand;
    private final LegacyComponentSerializer legacySection;
    private boolean papiAvailable;

    public FormatProcessor(CoreChat plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();
        this.legacySection = LegacyComponentSerializer.legacySection();
        this.papiAvailable = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        buildRestricted();
    }

    public void buildRestricted() {
        List<String> blocked = plugin.getConfigManager().getBlockedMiniMessageTags();
        List<TagResolver> allowed = new ArrayList<>();
        if (!blocked.contains("color")) allowed.add(StandardTags.color());
        if (!blocked.contains("decoration")) allowed.add(StandardTags.decorations());
        if (!blocked.contains("gradient")) allowed.add(StandardTags.gradient());
        if (!blocked.contains("rainbow")) allowed.add(StandardTags.rainbow());
        if (!blocked.contains("reset")) allowed.add(StandardTags.reset());
        restrictedMiniMessage = MiniMessage.builder()
                .tags(TagResolver.resolver(allowed))
                .build();
    }

    public String applyPAPI(Player player, String text) {
        if (!papiAvailable || player == null) return text;
        try {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        } catch (Exception e) {
            return text;
        }
    }

    public String buildFormat(Player player, String format) {
        String prefix = plugin.getPrefixManager().getPrefix(player);
        String suffix = plugin.getPrefixManager().getSuffix(player);
        String group = plugin.getPrefixManager().getGroup(player);

        String result = format
                .replace("%player%", player.getName())
                .replace("%displayname%", player.getDisplayName())
                .replace("%world%", player.getWorld().getName())
                .replace("%prefix%", prefix)
                .replace("%suffix%", suffix)
                .replace("%group%", group)
                .replace("%luckperms_prefix%", prefix)
                .replace("%luckperms_suffix%", suffix)
                .replace("%luckperms_group%", group);

        result = applyPAPI(player, result);
        return result;
    }

    public Component processMessage(Player player, String message) {
        boolean canMiniMessage = plugin.getConfigManager().isAllowMiniMessage()
                && player.hasPermission("corechat.minimessage");
        boolean canLegacy = plugin.getConfigManager().isAllowLegacyColors()
                && player.hasPermission("corechat.legacy");

        if (canMiniMessage) return restrictedMiniMessage.deserialize(message);
        if (canLegacy) return legacyAmpersand.deserialize(message);
        return Component.text(message);
    }

    public Component toComponent(String formattedText) {
        return legacyAmpersand.deserialize(formattedText);
    }

    public Component toComponentSection(String formattedText) {
        return legacySection.deserialize(formattedText);
    }

    public String color(String text) {
        return text.replace("&", "\u00a7");
    }

    public String stripFormatting(String message) {
        return message.replaceAll("&[0-9a-fk-or]", "")
                .replaceAll("<[^>]+>", "")
                .trim();
    }

    public String serializeComponent(Component component) {
        return legacySection.serialize(component);
    }

    public boolean isPapiAvailable() { return papiAvailable; }
}
