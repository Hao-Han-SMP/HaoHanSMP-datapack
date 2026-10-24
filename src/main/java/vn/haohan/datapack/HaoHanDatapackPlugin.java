package vn.haohan.datapack;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper plugin entry point for HaoHanSMP-Datapack.
 * Automatically loads the Lunar dimension on server startup.
 */
public final class HaoHanDatapackPlugin extends JavaPlugin {

    public static final NamespacedKey LUNAR_WORLD_KEY = NamespacedKey.fromString("haohan:lunar");

    @Override
    public void onEnable() {
        World world = Bukkit.getWorld(LUNAR_WORLD_KEY);
        if (world == null) {
            try {
                Bukkit.createWorld(WorldCreator.ofKey(LUNAR_WORLD_KEY));
                getLogger().info("Successfully loaded Lunar dimension world (" + LUNAR_WORLD_KEY + ")");
            } catch (Exception e) {
                getLogger().warning("Failed to load Lunar dimension world: " + e.getMessage());
            }
        }
    }
}
