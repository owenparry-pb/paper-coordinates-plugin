package com.parryproject;

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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CoordinatesPlugin extends JavaPlugin implements Listener {

    // Set to track players who have coordinates enabled
    private final Set<UUID> coordsEnabled = new HashSet<>();

    @Override
    public void onEnable() {
        // --- UPDATE CHECK: runs automatically on server/plugin start ---
        runUpdateCheck();

        // Register event listener
        getServer().getPluginManager().registerEvents(this, this);

        // Register command executor (safeguard; plugin.yml does this)
        PluginCommand coordsCommand = getCommand("coords");
        if (coordsCommand != null) {
            coordsCommand.setExecutor(this);
        }

        // By default, enable for all online players (useful for reloads)
        getServer().getScheduler().runTaskLater(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                coordsEnabled.add(player.getUniqueId());
            }
        }, 1L);

        // Repeating task every 20 ticks (1 second)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    if (coordsEnabled.contains(player.getUniqueId())) {
                        String coords = String.format("§aX: %.1f §bY: %.1f §cZ: %.1f",
                                player.getLocation().getX(),
                                player.getLocation().getY(),
                                player.getLocation().getZ());
                        player.sendActionBar(Component.text(coords));
                    }
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    // --- Update Check Logic ---
    private void runUpdateCheck() {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                // Change the repo owner/name below if your repo is different!
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
        // Optional: default to enabled for new join
        coordsEnabled.add(player.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /coords <on|off>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "on":
                coordsEnabled.add(player.getUniqueId());
                player.sendMessage(Component.text("Coordinates will now show on your screen."));
                break;
            case "off":
                coordsEnabled.remove(player.getUniqueId());
                player.sendMessage(Component.text("Coordinates will no longer show on your screen."));
                break;
            default:
                player.sendMessage(Component.text("Usage: /coords <on|off>"));
                break;
        }
        return true;
    }
}
