package com.yesithx.corechat.advertising;

import com.yesithx.corechat.CoreChat;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AntiAdvertising {

    private final CoreChat plugin;
    private List<Pattern> compiledPatterns;

    public AntiAdvertising(CoreChat plugin) {
        this.plugin = plugin;
        compile();
    }

    public void compile() {
        compiledPatterns = plugin.getConfigManager().getAdvertisingPatterns().stream()
                .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());
    }

    public boolean isAdvertising(String message) {
        if (!plugin.getConfigManager().isAdvertisingEnabled()) return false;
        String lower = message.toLowerCase();
        List<String> whitelist = plugin.getConfigManager().getAdvertisingWhitelist();

        for (Pattern pattern : compiledPatterns) {
            var matcher = pattern.matcher(lower);
            while (matcher.find()) {
                String found = matcher.group();
                boolean whitelisted = whitelist.stream().anyMatch(w -> found.contains(w.toLowerCase()));
                if (!whitelisted) return true;
            }
        }
        return false;
    }
}
