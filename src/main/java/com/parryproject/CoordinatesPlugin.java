package com.parryproject;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.*;

public class CoordinatesPlugin extends JavaPlugin implements Listener {

    private final Set<UUID> coordsEnabled = new HashSet<>();
    private final Map<UUID, DisplayLocation> locationPrefs = new HashMap<>();
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();

    private enum DisplayLocation {
        ACTIONBAR,
        SCOREBOARD
    }

    @Override
    public void onEnable() {
        runUpdateCheck();

        getServer().getPluginManager().registerEvents(this, this);

        PluginCommand coordsCommand = getCommand("coords");
        if (coordsCommand != null) {
            coordsCommand.setExecutor(this);
        }

        getServer().getScheduler().runTaskLater(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                coordsEnabled.add(player.getUniqueId());
                locationPrefs.putIfAbsent(player.getUniqueId(), DisplayLocation.ACTIONBAR);
            }
        }, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    if (coordsEnabled.contains(uuid)) {
                        DisplayLocation location = locationPrefs.getOrDefault(uuid, DisplayLocation.ACTIONBAR);
                        String coords = String.format("X: %.1f Y: %.1f Z: %.1f",
                                player.getLocation().getX(),
                                player.getLocation().getY(),
                                player.getLocation().getZ());
                        if (location == DisplayLocation.ACTIONBAR) {
                            player.sendActionBar(Component.text("§a" + coords));
                            removeScoreboard(player);
                        } else {
                            showScoreboard(player, coords);
                        }
                    } else {
                        removeScoreboard(player);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    // Helper to show scoreboard
    private void showScoreboard(Player player, String coords) {
        Scoreboard scoreboard = playerScoreboards.computeIfAbsent(player.getUniqueId(), k -> Bukkit.getScoreboardManager().getNewScoreboard());
        Objective obj = scoreboard.getObjective("coords");
        if (obj == null) {
            obj = scoreboard.registerNewObjective("coords", "dummy", Component.text("Coordinates"));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        obj.getScore(coords).setScore(1);
        // Remove old entries
        for (String entry : scoreboard.getEntries()) {
            if (!entry.equals(coords)) scoreboard.resetScores(entry);
        }
        player.setScoreboard(scoreboard);
    }

    private void removeScoreboard(Player player) {
        if (playerScoreboards.containsKey(player.getUniqueId())) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            playerScoreboards.remove(player.getUniqueId());
        }
    }

    // --- Update Check Logic ---
    private void runUpdateCheck() {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/parryproject/coordinatesplugin/releases/latest");
                try (InputStream is = url.openStream();
                     JsonReader reader = Json.createReader(is)) {
                    JsonObject release = reader.readObject();
                    String latest = release.getString("tag_name");
                    logUpdateCheck(latest);
                }
            } catch (Exception e) {
                logUpdateCheck("Update check failed: " + e.getMessage());
            }
        });
    }

    private void logUpdateCheck(String latestVersion) {
        String logEntry = "Update check at " + LocalDateTime.now() + ": Latest version/tag is " + latestVersion + System.lineSeparator();
        try {
            Files.write(Paths.get(getDataFolder().getAbsolutePath(), "update-check.log"), logEntry.getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            getLogger().warning("Could not log update check: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("Use /coords off if you want to hide your coordinates."));
        coordsEnabled.add(player.getUniqueId());
        locationPrefs.putIfAbsent(player.getUniqueId(), DisplayLocation.ACTIONBAR);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /coords <on|off|location>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on":
                coordsEnabled.add(uuid);
                player.sendMessage(Component.text("Coordinates will now show on your screen."));
                break;
            case "off":
                coordsEnabled.remove(uuid);
                player.sendMessage(Component.text("Coordinates will no longer show on your screen."));
                removeScoreboard(player);
                break;
            case "location":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /coords location <actionbar|scoreboard>"));
                    return true;
                }
                String loc = args[1].toLowerCase();
                if (loc.equals("actionbar")) {
                    locationPrefs.put(uuid, DisplayLocation.ACTIONBAR);
                    player.sendMessage(Component.text("Coordinates will now show on the action bar."));
                    removeScoreboard(player);
                } else if (loc.equals("scoreboard")) {
                    locationPrefs.put(uuid, DisplayLocation.SCOREBOARD);
                    player.sendMessage(Component.text("Coordinates will now show in the sidebar scoreboard."));
                } else {
                    player.sendMessage(Component.text("Unknown location. Use actionbar or scoreboard."));
                }
                break;
            default:
                player.sendMessage(Component.text("Usage: /coords <on|off|location>"));
                break;
        }
        return true;
    }
}
