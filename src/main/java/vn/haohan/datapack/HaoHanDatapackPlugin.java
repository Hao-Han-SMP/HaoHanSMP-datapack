package vn.haohan.datapack;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Main Paper/Purpur plugin entry point for HaoHanSMP-Datapack.
 * Coordinates plugin lifecycle, auto-loads custom dimension worlds (haohan:lunar),
 * and provides inspection commands.
 */
public final class HaoHanDatapackPlugin extends JavaPlugin {

    public static final NamespacedKey LUNAR_WORLD_KEY = NamespacedKey.fromString("haohan:lunar");

    private static HaoHanDatapackPlugin instance;

    /**
     * Returns the singleton plugin instance.
     *
     * @return active plugin instance
     */
    public static HaoHanDatapackPlugin getInstance() {
        return instance;
    }

    /**
     * Checks if the given world is the Lunar dimension.
     *
     * @param world world to check
     * @return true if world matches the Lunar dimension key
     */
    public static boolean isLunarWorld(World world) {
        return world != null && LUNAR_WORLD_KEY.equals(world.getKey());
    }

    /**
     * Retrieves the loaded Lunar dimension world, or attempts to load/create it if not loaded.
     *
     * @return lunar world instance or null if failed
     */
    public static World getOrLoadLunarWorld() {
        World world = Bukkit.getWorld(LUNAR_WORLD_KEY);
        if (world != null) {
            return world;
        }
        try {
            WorldCreator creator = WorldCreator.ofKey(LUNAR_WORLD_KEY);
            world = Bukkit.createWorld(creator);
            if (world != null && instance != null) {
                instance.getLogger().info("Successfully loaded Lunar dimension world (" + LUNAR_WORLD_KEY + ")");
            }
        } catch (Exception e) {
            if (instance != null) {
                instance.getLogger().warning("Failed to load Lunar dimension world: " + e.getMessage());
            }
        }
        return world;
    }

    @Override
    public void onLoad() {
        try {
            var datapackManager = getServer().getDatapackManager();
            var pack = datapackManager.getPack(getPluginMeta().getName() + "/provided");
            if (pack != null && pack.isEnabled()) {
                getSLF4JLogger().info("Bundled datapack loaded and enabled successfully: {}/provided", getPluginMeta().getName());
            } else if (pack != null) {
                getSLF4JLogger().info("Bundled datapack discovered: {}/provided (enabled={})", getPluginMeta().getName(), pack.isEnabled());
            }
        } catch (Exception e) {
            getSLF4JLogger().debug("Datapack check during onLoad: {}", e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        // Auto-load Lunar dimension world
        getOrLoadLunarWorld();

        // Register inspection command
        BukkitCommand command = new BukkitCommand("haohandatapack") {
            {
                setDescription("Kiem tra trang thai cua HaoHanSMP Datapack Plugin");
                setPermission("haohan.admin");
                setAliases(List.of("hhd"));
            }

            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                sender.sendMessage("§e[HaoHanSMP-Datapack] §aVersion " + getPluginMeta().getVersion() + " (Purpur 1.21.11)");
                World lunarWorld = Bukkit.getWorld(LUNAR_WORLD_KEY);
                if (lunarWorld != null) {
                    sender.sendMessage("§e- The gioi Mat Trang (haohan:lunar): §aDa load (" + lunarWorld.getName() + ")");
                } else {
                    sender.sendMessage("§e- The gioi Mat Trang (haohan:lunar): §cChua load (go /hhd load de load)");
                }

                if (args.length > 0 && args[0].equalsIgnoreCase("load")) {
                    World loaded = getOrLoadLunarWorld();
                    if (loaded != null) {
                        sender.sendMessage("§a[HaoHanSMP-Datapack] Load the gioi Mat Trang thanh cong!");
                    } else {
                        sender.sendMessage("§c[HaoHanSMP-Datapack] Khong the load the gioi Mat Trang.");
                    }
                }
                return true;
            }
        };

        Bukkit.getCommandMap().register("haohan", command);

        getLogger().info("HaoHanSMP-Datapack plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HaoHanSMP-Datapack plugin disabled.");
        instance = null;
    }
}
