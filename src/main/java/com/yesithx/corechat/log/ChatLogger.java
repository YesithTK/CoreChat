package com.yesithx.corechat.log;

import com.yesithx.corechat.CoreChat;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatLogger {

    private final CoreChat plugin;
    private File logFile;
    private SimpleDateFormat dateFormat;

    public ChatLogger(CoreChat plugin) {
        this.plugin = plugin;
    }

    public void init() {
        if (!plugin.getConfigManager().isLogEnabled() || !plugin.getConfigManager().isLogToFile()) return;
        logFile = new File(plugin.getDataFolder(), plugin.getConfigManager().getLogFileName());
        dateFormat = new SimpleDateFormat(plugin.getConfigManager().getLogDateFormat());
    }

    public void logChat(String world, String player, String message) {
        if (!plugin.getConfigManager().isLogEnabled()) return;
        String entry = plugin.getConfigManager().getLogFormat()
                .replace("{date}", dateFormat.format(new Date()))
                .replace("{world}", world)
                .replace("{player}", player)
                .replace("{message}", message);
        write(entry);
    }

    public void logPrivate(String sender, String target, String message) {
        if (!plugin.getConfigManager().isLogEnabled() || !plugin.getConfigManager().isLogPrivate()) return;
        String entry = "[" + dateFormat.format(new Date()) + "] [PM] " + sender + " -> " + target + ": " + message;
        write(entry);
    }

    public void logBlocked(String player, String message, String reason) {
        if (!plugin.getConfigManager().isLogEnabled() || !plugin.getConfigManager().isLogBlocked()) return;
        String entry = "[" + dateFormat.format(new Date()) + "] [BLOCKED:" + reason + "] " + player + ": " + message;
        write(entry);
    }

    private void write(String line) {
        if (logFile == null) return;
        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write to chat log: " + e.getMessage());
        }
    }
}
