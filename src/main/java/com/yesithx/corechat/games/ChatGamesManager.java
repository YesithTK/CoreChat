package com.yesithx.corechat.games;

import com.yesithx.corechat.CoreChat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class ChatGamesManager {

    private final CoreChat plugin;
    private final Random random = new Random();
    private BukkitTask mathTask;
    private BukkitTask unscrambleTask;
    private BukkitTask mathTimeoutTask;
    private BukkitTask unscrambleTimeoutTask;

    private String currentMathAnswer = null;
    private String currentScrambledWord = null;
    private String currentUnscrambleAnswer = null;

    public ChatGamesManager(CoreChat plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfigManager().isChatGamesEnabled()) return;

        if (plugin.getConfigManager().isMathGameEnabled()) {
            long interval = plugin.getConfigManager().getMathIntervalMinutes() * 60L * 20L;
            mathTask = Bukkit.getScheduler().runTaskTimer(plugin, this::startMathGame, interval, interval);
        }

        if (plugin.getConfigManager().isUnscrambleGameEnabled()) {
            long interval = plugin.getConfigManager().getUnscrambleIntervalMinutes() * 60L * 20L;
            unscrambleTask = Bukkit.getScheduler().runTaskTimer(plugin, this::startUnscrambleGame, interval, interval);
        }
    }

    public void stop() {
        if (mathTask != null) mathTask.cancel();
        if (unscrambleTask != null) unscrambleTask.cancel();
        if (mathTimeoutTask != null) mathTimeoutTask.cancel();
        if (unscrambleTimeoutTask != null) unscrambleTimeoutTask.cancel();
    }

    private void startMathGame() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        int a = random.nextInt(50) + 1;
        int b = random.nextInt(50) + 1;
        int result = a + b;
        currentMathAnswer = String.valueOf(result);

        String question = a + " + " + b;
        String format = plugin.getLangManager().color(
                plugin.getConfigManager().getMathFormat().replace("{question}", question));
        Bukkit.broadcastMessage(format);

        long timeout = plugin.getConfigManager().getMathTimeoutSeconds() * 20L;
        if (mathTimeoutTask != null) mathTimeoutTask.cancel();
        mathTimeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (currentMathAnswer != null) {
                currentMathAnswer = null;
                Bukkit.broadcastMessage(plugin.getLangManager().color("&cNadie respondió la pregunta. La respuesta era: &f" + result));
            }
        }, timeout);
    }

    private void startUnscrambleGame() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        List<String> words = plugin.getConfigManager().getUnscrambleWords();
        if (words.isEmpty()) return;
        String word = words.get(random.nextInt(words.size()));
        currentUnscrambleAnswer = word;
        currentScrambledWord = scramble(word);

        String format = plugin.getLangManager().color(
                plugin.getConfigManager().getUnscrambleFormat()
                        .replace("{word}", currentScrambledWord));
        Bukkit.broadcastMessage(format);

        long timeout = plugin.getConfigManager().getMathTimeoutSeconds() * 20L;
        if (unscrambleTimeoutTask != null) unscrambleTimeoutTask.cancel();
        unscrambleTimeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (currentUnscrambleAnswer != null) {
                String ans = currentUnscrambleAnswer;
                currentUnscrambleAnswer = null;
                Bukkit.broadcastMessage(plugin.getLangManager().color("&cNadie adivinó la palabra. Era: &f" + ans));
            }
        }, timeout);
    }

    public boolean checkMathAnswer(Player player, String answer) {
        if (currentMathAnswer == null) return false;
        if (!currentMathAnswer.equals(answer.trim())) return false;

        String correct = currentMathAnswer;
        currentMathAnswer = null;
        if (mathTimeoutTask != null) mathTimeoutTask.cancel();

        double reward = plugin.getConfigManager().getMathRewardMoney();
        Economy economy = plugin.getEconomy();
        if (economy != null && reward > 0) economy.depositPlayer(player, reward);

        String format = plugin.getLangManager().color(
                plugin.getConfigManager().getMathAnswerFormat()
                        .replace("{player}", player.getName())
                        .replace("{reward}", String.valueOf((int) reward)));
        Bukkit.broadcastMessage(format);
        return true;
    }

    public boolean checkUnscrambleAnswer(Player player, String answer) {
        if (currentUnscrambleAnswer == null) return false;
        if (!currentUnscrambleAnswer.equalsIgnoreCase(answer.trim())) return false;

        currentUnscrambleAnswer = null;
        if (unscrambleTimeoutTask != null) unscrambleTimeoutTask.cancel();

        double reward = plugin.getConfigManager().getUnscrambleRewardMoney();
        Economy economy = plugin.getEconomy();
        if (economy != null && reward > 0) economy.depositPlayer(player, reward);

        String format = plugin.getLangManager().color(
                plugin.getConfigManager().getUnscrambleAnswerFormat()
                        .replace("{player}", player.getName())
                        .replace("{reward}", String.valueOf((int) reward)));
        Bukkit.broadcastMessage(format);
        return true;
    }

    private String scramble(String word) {
        char[] chars = word.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
