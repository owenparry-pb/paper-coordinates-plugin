package com.coordinatesplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

public class CoordinatesPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Start a repeating task every 20 ticks (1 second)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    String coords = String.format("§aX: %.1f §bY: %.1f §cZ: %.1f",
                            player.getLocation().getX(),
                            player.getLocation().getY(),
                            player.getLocation().getZ());
                    player.sendActionBar(Component.text(coords));
                }
            }
        }.runTaskTimer(this, 0, 20);
    }
}
