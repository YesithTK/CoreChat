package com.yesithx.corechat.config;

import com.yesithx.corechat.CoreChat;

import java.util.List;
import java.util.Set;

public class ConfigManager {

    private final CoreChat plugin;

    public ConfigManager(CoreChat plugin) { this.plugin = plugin; }

    public void reload() { plugin.reloadConfig(); }

    public String getLanguage() { return plugin.getConfig().getString("language", "es"); }
    public boolean isAllowMiniMessage() { return plugin.getConfig().getBoolean("format.allow-minimessage", true); }
    public boolean isAllowLegacyColors() { return plugin.getConfig().getBoolean("format.allow-legacy-colors", true); }
    public List<String> getBlockedMiniMessageTags() { return plugin.getConfig().getStringList("format.blocked-minimessage-tags"); }
    public boolean isParsePAPI() { return plugin.getConfig().getBoolean("format.parse-placeholderapi", true); }
    public boolean isAllowUrls() { return plugin.getConfig().getBoolean("format.allow-urls", false); }
    public String getUrlPermission() { return plugin.getConfig().getString("format.url-permission", "corechat.url"); }
    public String getUrlFormat() { return plugin.getConfig().getString("format.url-format", "&b&n{url}"); }
    public String getUrlRegex() { return plugin.getConfig().getString("format.url-regex", "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)"); }

    public String getDefaultChannel() { return plugin.getConfig().getString("channels.default", "global"); }
    public Set<String> getChannelNames() {
        var section = plugin.getConfig().getConfigurationSection("channels");
        if (section == null) return Set.of("global");
        var keys = section.getKeys(false);
        keys.remove("default");
        return keys;
    }
    public String getChannelFormat(String ch) { return plugin.getConfig().getString("channels." + ch + ".format", "&7{player}&8: &f{message}"); }
    public String getChannelPrefix(String ch) { return plugin.getConfig().getString("channels." + ch + ".prefix", ""); }
    public String getChannelSymbol(String ch) { return plugin.getConfig().getString("channels." + ch + ".symbol", ch.substring(0, 1).toUpperCase()); }
    public String getChannelColor(String ch) { return plugin.getConfig().getString("channels." + ch + ".color", "&f"); }
    public int getChannelRange(String ch) { return plugin.getConfig().getInt("channels." + ch + ".range", -1); }
    public String getChannelPermission(String ch) { return plugin.getConfig().getString("channels." + ch + ".permission", ""); }
    public boolean isChannelEnabled(String ch) { return plugin.getConfig().getBoolean("channels." + ch + ".enabled", true); }
    public boolean isChannelLog(String ch) { return plugin.getConfig().getBoolean("channels." + ch + ".log", true); }
    public int getChannelCooldownOverride(String ch) { return plugin.getConfig().getInt("channels." + ch + ".cooldown-override", -1); }

    public String getChatFormat() { return plugin.getConfig().getString("chat.format", "{luckperms_prefix}&7{player} &8» &f{message}"); }
    public String getConsoleChatFormat() { return plugin.getConfig().getString("chat.console-format", "[CHAT] {player}: {message}"); }
    public boolean isShowLuckPermsPrefix() { return plugin.getConfig().getBoolean("chat.show-luckperms-prefix", true); }
    public boolean isShowLuckPermsSuffix() { return plugin.getConfig().getBoolean("chat.show-luckperms-suffix", true); }
    public boolean isShowVaultPrefix() { return plugin.getConfig().getBoolean("chat.show-vault-prefix", true); }
    public boolean isPerWorldChat() { return plugin.getConfig().getBoolean("chat.per-world-chat", false); }
    public List<String> getSharedWorlds() { return plugin.getConfig().getStringList("chat.shared-worlds"); }

    public boolean isMsgEnabled() { return plugin.getConfig().getBoolean("private-message.enabled", true); }
    public String getMsgFormatSender() { return plugin.getConfig().getString("private-message.format-sender", "&8[&7Tú &8➜ &f{target}&8] &f{message}"); }
    public String getMsgFormatReceiver() { return plugin.getConfig().getString("private-message.format-receiver", "&8[&f{sender} &8➜ &7Tí&8] &f{message}"); }
    public String getMsgFormatSocialSpy() { return plugin.getConfig().getString("private-message.format-socialspy", "&8[&dSpy&8] &7{sender} &8➜ &7{target}&8: &f{message}"); }
    public boolean isMsgSoundEnabled() { return plugin.getConfig().getBoolean("private-message.sound-enabled", true); }
    public String getMsgSound() { return plugin.getConfig().getString("private-message.sound", "ENTITY_EXPERIENCE_ORB_PICKUP"); }
    public boolean isMsgLogToConsole() { return plugin.getConfig().getBoolean("private-message.log-to-console", true); }
    public int getMsgMaxLength() { return plugin.getConfig().getInt("private-message.max-length", 200); }

    public String getBroadcastFormat() { return plugin.getConfig().getString("broadcast.format", "&f{message}"); }
    public boolean isBroadcastSoundEnabled() { return plugin.getConfig().getBoolean("broadcast.sound-enabled", true); }
    public String getBroadcastSound() { return plugin.getConfig().getString("broadcast.sound", "ENTITY_PLAYER_LEVELUP"); }
    public boolean isBroadcastTitleEnabled() { return plugin.getConfig().getBoolean("broadcast.title-enabled", false); }
    public String getBroadcastTitle() { return plugin.getConfig().getString("broadcast.title", "&c&l[!]"); }
    public String getBroadcastSubtitle() { return plugin.getConfig().getString("broadcast.subtitle", "&f{message}"); }
    public int getBroadcastTitleFadeIn() { return plugin.getConfig().getInt("broadcast.title-fadein", 10); }
    public int getBroadcastTitleStay() { return plugin.getConfig().getInt("broadcast.title-stay", 60); }
    public int getBroadcastTitleFadeOut() { return plugin.getConfig().getInt("broadcast.title-fadeout", 20); }

    public boolean isCooldownEnabled() { return plugin.getConfig().getBoolean("cooldown.enabled", true); }
    public int getCooldownSeconds() { return plugin.getConfig().getInt("cooldown.seconds", 3); }
    public int getCooldownForGroup(String group) { return plugin.getConfig().getInt("cooldown.per-group." + group, getCooldownSeconds()); }

    public boolean isCapsEnabled() { return plugin.getConfig().getBoolean("caps.enabled", true); }
    public int getCapsMaxPercent() { return plugin.getConfig().getInt("caps.max-percent", 50); }
    public int getCapsMinLength() { return plugin.getConfig().getInt("caps.min-length", 6); }
    public String getCapsAction() { return plugin.getConfig().getString("caps.action", "replace"); }
    public boolean isCapsNotify() { return plugin.getConfig().getBoolean("caps.notify", true); }

    public boolean isFloodEnabled() { return plugin.getConfig().getBoolean("flood.enabled", true); }
    public int getFloodMinSimilarity() { return plugin.getConfig().getInt("flood.min-similarity", 85); }
    public String getFloodAction() { return plugin.getConfig().getString("flood.action", "block"); }

    public boolean isSpamEnabled() { return plugin.getConfig().getBoolean("spam.enabled", true); }
    public int getSpamMaxMessages() { return plugin.getConfig().getInt("spam.max-messages", 5); }
    public int getSpamTimeWindow() { return plugin.getConfig().getInt("spam.time-window", 5); }
    public String getSpamAction() { return plugin.getConfig().getString("spam.action", "block"); }
    public int getSpamMuteDuration() { return plugin.getConfig().getInt("spam.mute-duration", 60); }

    public boolean isAdvertisingEnabled() { return plugin.getConfig().getBoolean("advertising.enabled", true); }
    public String getAdvertisingAction() { return plugin.getConfig().getString("advertising.action", "block"); }
    public List<String> getAdvertisingWhitelist() { return plugin.getConfig().getStringList("advertising.whitelist"); }
    public List<String> getAdvertisingPatterns() { return plugin.getConfig().getStringList("advertising.patterns"); }

    public boolean isFilterEnabled() { return plugin.getConfig().getBoolean("filter.enabled", true); }
    public String getFilterAction() { return plugin.getConfig().getString("filter.action", "replace"); }
    public String getFilterReplaceChar() { return plugin.getConfig().getString("filter.replace-char", "*"); }
    public boolean isFilterWarnPlayer() { return plugin.getConfig().getBoolean("filter.warn-player", true); }
    public int getFilterWarnThreshold() { return plugin.getConfig().getInt("filter.warn-threshold", 3); }
    public int getFilterWarnResetSeconds() { return plugin.getConfig().getInt("filter.warn-reset-seconds", 3600); }
    public boolean isFilterMuteOnThreshold() { return plugin.getConfig().getBoolean("filter.mute-on-threshold", true); }
    public int getFilterMuteDuration() { return plugin.getConfig().getInt("filter.mute-duration-seconds", 300); }
    public boolean isFilterPrivateMessages() { return plugin.getConfig().getBoolean("filter.filter-private-messages", true); }
    public boolean isFilterLogFiltered() { return plugin.getConfig().getBoolean("filter.log-filtered", true); }
    public List<String> getFilterWords() { return plugin.getConfig().getStringList("filter.words"); }
    public List<String> getFilterRegex() { return plugin.getConfig().getStringList("filter.regex"); }

    public int getMuteDefaultDuration() { return plugin.getConfig().getInt("mute.default-duration-seconds", 300); }
    public boolean isMuteBroadcast() { return plugin.getConfig().getBoolean("mute.broadcast-mute", false); }
    public String getMuteBroadcastFormat() { return plugin.getConfig().getString("mute.broadcast-format", "&c{player} fue muteado."); }
    public boolean isMuteAllowPrivate() { return plugin.getConfig().getBoolean("mute.allow-private-messages", false); }
    public List<String> getMuteAllowedCommands() { return plugin.getConfig().getStringList("mute.allowed-commands"); }

    public int getClearChatLines() { return plugin.getConfig().getInt("clearchat.lines", 150); }
    public boolean isClearChatBroadcast() { return plugin.getConfig().getBoolean("clearchat.broadcast", true); }
    public boolean isClearChatShowClearer() { return plugin.getConfig().getBoolean("clearchat.show-clearer", true); }

    public boolean isMentionEnabled() { return plugin.getConfig().getBoolean("mention.enabled", true); }
    public String getMentionSymbol() { return plugin.getConfig().getString("mention.symbol", "@"); }
    public String getMentionSound() { return plugin.getConfig().getString("mention.sound", "ENTITY_EXPERIENCE_ORB_PICKUP"); }
    public String getMentionFormat() { return plugin.getConfig().getString("mention.format", "&e&l@{player}"); }
    public int getMentionCooldown() { return plugin.getConfig().getInt("mention.cooldown", 10); }
    public boolean isMentionEveryoneEnabled() { return plugin.getConfig().getBoolean("mention.allow-everyone", false); }

    public boolean isAfkEnabled() { return plugin.getConfig().getBoolean("afk.enabled", true); }
    public int getAfkTimeoutSeconds() { return plugin.getConfig().getInt("afk.timeout-seconds", 300); }
    public String getAfkNameFormat() { return plugin.getConfig().getString("afk.name-format", "&7[AFK] {name}"); }
    public boolean isAfkBroadcast() { return plugin.getConfig().getBoolean("afk.broadcast-on-afk", true); }
    public String getAfkBroadcastFormat() { return plugin.getConfig().getString("afk.broadcast-format", "&7{player} está AFK."); }
    public String getAfkBroadcastReturnFormat() { return plugin.getConfig().getString("afk.broadcast-return-format", "&7{player} ya no está AFK."); }
    public int getAfkKickAfterSeconds() { return plugin.getConfig().getInt("afk.kick-after-seconds", 0); }
    public String getAfkKickReason() { return plugin.getConfig().getString("afk.kick-reason", "Fuiste expulsado por inactividad."); }

    public boolean isChatGamesEnabled() { return plugin.getConfig().getBoolean("chat-games.enabled", false); }
    public boolean isMathGameEnabled() { return plugin.getConfig().getBoolean("chat-games.math.enabled", false); }
    public int getMathIntervalMinutes() { return plugin.getConfig().getInt("chat-games.math.interval-minutes", 15); }
    public double getMathRewardMoney() { return plugin.getConfig().getDouble("chat-games.math.reward-money", 100.0); }
    public String getMathFormat() { return plugin.getConfig().getString("chat-games.math.format", "&6[ChatGame] &eResuelve: &f{question}"); }
    public String getMathAnswerFormat() { return plugin.getConfig().getString("chat-games.math.answer-format", "&a[ChatGame] &f{player} &arespondió correctamente y ganó &f${reward}&a!"); }
    public int getMathTimeoutSeconds() { return plugin.getConfig().getInt("chat-games.math.timeout-seconds", 30); }
    public boolean isUnscrambleGameEnabled() { return plugin.getConfig().getBoolean("chat-games.unscramble.enabled", false); }
    public int getUnscrambleIntervalMinutes() { return plugin.getConfig().getInt("chat-games.unscramble.interval-minutes", 20); }
    public double getUnscrambleRewardMoney() { return plugin.getConfig().getDouble("chat-games.unscramble.reward-money", 150.0); }
    public String getUnscrambleFormat() { return plugin.getConfig().getString("chat-games.unscramble.format", "&6[ChatGame] &eDescifra: &f{word}"); }
    public String getUnscrambleAnswerFormat() { return plugin.getConfig().getString("chat-games.unscramble.answer-format", "&a[ChatGame] &f{player} &adescifrió la palabra y ganó &f${reward}&a!"); }
    public List<String> getUnscrambleWords() { return plugin.getConfig().getStringList("chat-games.unscramble.words"); }

    public boolean isLogEnabled() { return plugin.getConfig().getBoolean("log.enabled", true); }
    public boolean isLogToFile() { return plugin.getConfig().getBoolean("log.log-to-file", true); }
    public String getLogFileName() { return plugin.getConfig().getString("log.file-name", "chat-log.txt"); }
    public String getLogDateFormat() { return plugin.getConfig().getString("log.date-format", "yyyy-MM-dd HH:mm:ss"); }
    public String getLogFormat() { return plugin.getConfig().getString("log.format", "[{date}] [{world}] {player}: {message}"); }
    public boolean isLogBlocked() { return plugin.getConfig().getBoolean("log.log-blocked", true); }
    public boolean isLogPrivate() { return plugin.getConfig().getBoolean("log.log-private", true); }

    public boolean isJoinQuitEnabled() { return plugin.getConfig().getBoolean("join-quit.enabled", true); }
    public boolean isJoinEnabled() { return plugin.getConfig().getBoolean("join-quit.join.enabled", true); }
    public String getJoinFormat() { return plugin.getConfig().getString("join-quit.join.format", "&8[&a+&8] &7{player} &ase unió."); }
    public String getFirstJoinFormat() { return plugin.getConfig().getString("join-quit.join.first-join-format", "&8[&a+&8] &7{player} &aentró por primera vez."); }
    public boolean isJoinBroadcast() { return plugin.getConfig().getBoolean("join-quit.join.broadcast", true); }
    public boolean isJoinSoundEnabled() { return plugin.getConfig().getBoolean("join-quit.join.sound-enabled", true); }
    public String getJoinSound() { return plugin.getConfig().getString("join-quit.join.sound", "ENTITY_PLAYER_LEVELUP"); }
    public boolean isJoinTitleEnabled() { return plugin.getConfig().getBoolean("join-quit.join.title-enabled", false); }
    public String getJoinTitle() { return plugin.getConfig().getString("join-quit.join.title", "&a¡Bienvenido!"); }
    public String getJoinSubtitle() { return plugin.getConfig().getString("join-quit.join.subtitle", "&fBienvenido, {player}."); }
    public boolean isQuitEnabled() { return plugin.getConfig().getBoolean("join-quit.quit.enabled", true); }
    public String getQuitFormat() { return plugin.getConfig().getString("join-quit.quit.format", "&8[&c-&8] &7{player} &cse fue."); }
    public boolean isQuitBroadcast() { return plugin.getConfig().getBoolean("join-quit.quit.broadcast", true); }

    public boolean isIgnoreBlockPrivate() { return plugin.getConfig().getBoolean("ignore.block-private-messages", true); }
    public boolean isIgnoreBlockChat() { return plugin.getConfig().getBoolean("ignore.block-chat-messages", true); }
    public int getIgnoreMax() { return plugin.getConfig().getInt("ignore.max-ignored", 50); }

    public boolean isMeCommandEnabled() { return plugin.getConfig().getBoolean("commands.override-me", true); }
    public String getMeFormat() { return plugin.getConfig().getString("commands.me-format", "&7* &f{player} {message}"); }
}
