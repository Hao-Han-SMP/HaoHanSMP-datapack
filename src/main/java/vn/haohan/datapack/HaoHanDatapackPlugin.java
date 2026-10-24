package vn.haohan.datapack;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Main Paper/Purpur plugin entry point for HaoHanSMP-Datapack.
 * <p>
 * Implements a Terra-inspired Dimension Registry architecture:
 * Coordinates plugin lifecycle, auto-discovers and registers custom dimension worlds,
 * provides dimension lifecycle management (load/unload/query), and admin inspection commands.
 * </p>
 */
public final class HaoHanDatapackPlugin extends JavaPlugin {

    public static final NamespacedKey LUNAR_WORLD_KEY = NamespacedKey.fromString("haohan:lunar");

    private static HaoHanDatapackPlugin instance;
    private final Map<NamespacedKey, String> registeredDimensions = new LinkedHashMap<>();

    /**
     * Returns the singleton plugin instance.
     *
     * @return active plugin instance
     */
    public static HaoHanDatapackPlugin getInstance() {
        return instance;
    }

    /**
     * Checks if the given world matches a registered custom dimension.
     *
     * @param world world to check
     * @return true if world matches any registered dimension key
     */
    public static boolean isCustomWorld(World world) {
        if (world == null || instance == null) {
            return false;
        }
        return instance.registeredDimensions.containsKey(world.getKey());
    }

    /**
     * Checks if the given world is specifically the Lunar dimension.
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
        if (instance != null) {
            return instance.getOrLoadWorld(LUNAR_WORLD_KEY);
        }
        return Bukkit.getWorld(LUNAR_WORLD_KEY);
    }

    /**
     * Registers a custom dimension key into the registry.
     *
     * @param key         namespaced key of the dimension (e.g. haohan:lunar)
     * @param displayName human-readable name of the dimension
     */
    public void registerDimension(NamespacedKey key, String displayName) {
        registeredDimensions.put(key, displayName);
    }

    /**
     * Returns all registered dimension keys and their display names.
     *
     * @return unmodifiable map of registered dimensions
     */
    public Map<NamespacedKey, String> getRegisteredDimensions() {
        return Collections.unmodifiableMap(registeredDimensions);
    }

    /**
     * Retrieves or loads a world by its NamespacedKey.
     *
     * @param key namespaced key of the world
     * @return World instance, or null if loading failed
     */
    public World getOrLoadWorld(NamespacedKey key) {
        World world = Bukkit.getWorld(key);
        if (world != null) {
            return world;
        }
        try {
            WorldCreator creator = WorldCreator.ofKey(key);
            world = Bukkit.createWorld(creator);
            if (world != null) {
                getLogger().info("Successfully loaded dimension world (" + key + ")");
            }
        } catch (Exception e) {
            getLogger().warning("Failed to load dimension world (" + key + "): " + e.getMessage());
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

        // Register default dimensions (Terra-style Dimension Registry)
        registerDimension(LUNAR_WORLD_KEY, "Mặt Trăng (Lunar)");

        // Auto-load all registered dimensions on server startup
        for (NamespacedKey key : registeredDimensions.keySet()) {
            getOrLoadWorld(key);
        }

        // Register inspection and management command
        BukkitCommand command = new BukkitCommand("haohandatapack") {
            {
                setDescription("Quản lý và kiểm tra hệ thống thế giới / datapack");
                setPermission("haohan.admin");
                setAliases(List.of("hhd", "hhworld"));
            }

            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
                    sender.sendMessage("§6§l[HaoHanSMP-WorldGen] §aVersion " + getPluginMeta().getVersion());
                    sender.sendMessage("§eDanh sách các chiều không gian đăng ký:");
                    for (Map.Entry<NamespacedKey, String> entry : registeredDimensions.entrySet()) {
                        NamespacedKey key = entry.getKey();
                        World w = Bukkit.getWorld(key);
                        if (w != null) {
                            sender.sendMessage(" §a✔ §f" + entry.getValue() + " §7(" + key + "): §aĐã nạp §7[" + w.getLoadedChunks().length + " chunks, " + w.getPlayerCount() + " players]");
                        } else {
                            sender.sendMessage(" §c✖ §f" + entry.getValue() + " §7(" + key + "): §cChưa nạp");
                        }
                    }
                    sender.sendMessage("§7Dùng: §e/hhd load <key>§7 hoặc §e/hhd tp <key>");
                    return true;
                }

                if (args[0].equalsIgnoreCase("load")) {
                    if (args.length < 2) {
                        sender.sendMessage("§cCú pháp: /hhd load <key> (ví dụ: haohan:lunar)");
                        return true;
                    }
                    NamespacedKey key = NamespacedKey.fromString(args[1]);
                    if (key == null) {
                        sender.sendMessage("§cKey không hợp lệ!");
                        return true;
                    }
                    World loaded = getOrLoadWorld(key);
                    if (loaded != null) {
                        sender.sendMessage("§a[HaoHanSMP-WorldGen] Nạp thành công chiều không gian: §f" + key);
                    } else {
                        sender.sendMessage("§c[HaoHanSMP-WorldGen] Không thể nạp chiều không gian: §f" + key);
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("tp") && sender instanceof Player player) {
                    if (args.length < 2) {
                        sender.sendMessage("§cCú pháp: /hhd tp <key> (ví dụ: haohan:lunar)");
                        return true;
                    }
                    NamespacedKey key = NamespacedKey.fromString(args[1]);
                    World target = (key != null) ? getOrLoadWorld(key) : null;
                    if (target != null) {
                        player.teleport(target.getSpawnLocation());
                        player.sendMessage("§a[HaoHanSMP-WorldGen] Đã dịch chuyển đến spawn của: §f" + target.getName());
                    } else {
                        player.sendMessage("§c[HaoHanSMP-WorldGen] Thế giới không tồn tại hoặc nạp thất bại.");
                    }
                    return true;
                }

                sender.sendMessage("§cCú pháp không đúng. Dùng /hhd để xem danh sách.");
                return true;
            }
        };

        Bukkit.getCommandMap().register("haohan", command);

        getLogger().info("HaoHanSMP-Datapack (WorldGen Engine) enabled successfully with " + registeredDimensions.size() + " registered dimension(s)!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HaoHanSMP-Datapack plugin disabled.");
        registeredDimensions.clear();
        instance = null;
    }
}
