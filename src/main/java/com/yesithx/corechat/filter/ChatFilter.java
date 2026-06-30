package com.yesithx.corechat.filter;

import com.yesithx.corechat.CoreChat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class ChatFilter {

    private final CoreChat plugin;
    private final Map<UUID, Integer> warnCount = new HashMap<>();
    private List<Pattern> compiledRegex;

    public ChatFilter(CoreChat plugin) {
        this.plugin = plugin;
        compileRegex();
    }

    public void compileRegex() {
        compiledRegex = plugin.getConfigManager().getFilterRegex().stream()
                .map(r -> Pattern.compile(r, Pattern.CASE_INSENSITIVE))
                .toList();
    }

    public FilterResult process(UUID uuid, String message) {
        if (!plugin.getConfigManager().isFilterEnabled()) {
            return new FilterResult(message, false);
        }

        boolean found = false;
        String result = message;

        for (String word : plugin.getConfigManager().getFilterWords()) {
            if (message.toLowerCase().contains(word.toLowerCase())) {
                found = true;
                result = replaceWord(result, word);
            }
        }

        for (Pattern pattern : compiledRegex) {
            if (pattern.matcher(message).find()) {
                found = true;
                result = pattern.matcher(result).replaceAll(buildReplacement(message, pattern));
            }
        }

        if (found) {
            int count = warnCount.merge(uuid, 1, Integer::sum);
            int threshold = plugin.getConfigManager().getFilterWarnThreshold();
            boolean shouldMute = plugin.getConfigManager().isFilterMuteOnThreshold() && count >= threshold;

            String action = plugin.getConfigManager().getFilterAction();
            return switch (action) {
                case "block" -> new FilterResult(null, true, count, threshold, shouldMute);
                case "replace" -> new FilterResult(result, true, count, threshold, shouldMute);
                default -> new FilterResult(result, true, count, threshold, shouldMute);
            };
        }

        return new FilterResult(message, false);
    }

    private String replaceWord(String message, String word) {
        String rep = plugin.getConfigManager().getFilterReplaceChar().repeat(word.length());
        return message.replaceAll("(?i)" + Pattern.quote(word), rep);
    }

    private String buildReplacement(String original, Pattern pattern) {
        var matcher = pattern.matcher(original);
        if (matcher.find()) {
            return plugin.getConfigManager().getFilterReplaceChar().repeat(matcher.group().length());
        }
        return plugin.getConfigManager().getFilterReplaceChar();
    }

    public void resetWarnings(UUID uuid) {
        warnCount.remove(uuid);
    }

    public int getWarnings(UUID uuid) {
        return warnCount.getOrDefault(uuid, 0);
    }

    public record FilterResult(String processedMessage, boolean triggered,
                               int warnCount, int warnThreshold, boolean shouldMute) {
        public FilterResult(String processedMessage, boolean triggered) {
            this(processedMessage, triggered, 0, 0, false);
        }
        public boolean isBlocked() { return triggered && processedMessage == null; }
    }
}
