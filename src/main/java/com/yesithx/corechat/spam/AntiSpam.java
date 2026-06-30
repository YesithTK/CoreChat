package com.yesithx.corechat.spam;

import com.yesithx.corechat.CoreChat;

import java.util.*;

public class AntiSpam {

    private final CoreChat plugin;
    private final Map<UUID, List<Long>> messageTimestamps = new HashMap<>();

    public AntiSpam(CoreChat plugin) {
        this.plugin = plugin;
    }

    public SpamResult check(UUID uuid) {
        if (!plugin.getConfigManager().isSpamEnabled()) return SpamResult.ALLOWED;

        long now = System.currentTimeMillis();
        long window = plugin.getConfigManager().getSpamTimeWindow() * 1000L;
        int max = plugin.getConfigManager().getSpamMaxMessages();

        List<Long> timestamps = messageTimestamps.computeIfAbsent(uuid, k -> new ArrayList<>());
        timestamps.removeIf(t -> now - t > window);
        timestamps.add(now);

        if (timestamps.size() > max) return SpamResult.SPAM;
        return SpamResult.ALLOWED;
    }

    public void clear(UUID uuid) {
        messageTimestamps.remove(uuid);
    }

    public enum SpamResult {
        ALLOWED, SPAM
    }
}
