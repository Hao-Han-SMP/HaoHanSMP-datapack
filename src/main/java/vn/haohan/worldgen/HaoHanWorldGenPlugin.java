package vn.haohan.worldgen;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import vn.haohan.worldgen.config.LunarWorldConfig;

/**
 * Paper plugin entry point for HaoHan-WorldGen.
 * Manages the custom Lunar dimension world generator, biome provider,
 * and configuration.
 */
public final class HaoHanWorldGenPlugin extends JavaPlugin {

    public static final NamespacedKey LUNAR_WORLD_KEY = NamespacedKey.fromString("haohan:lunar");

    private LunarWorldConfig worldConfig;
    private LunarChunkGenerator chunkGenerator;

    @Override
    public void onEnable() {
        // 1. Load worldgen configuration
        worldConfig = new LunarWorldConfig(this);
        worldConfig.load();

        chunkGenerator = new LunarChunkGenerator(worldConfig);
        chunkGenerator.getStructurePopulator().preloadAllStructures();

        // 2. Automatically initialize and load the Lunar dimension as a Custom environment
        // Custom environment avoids vanilla EndDragonFight, eliminating dragons, gray fog, and chunk deadlocks
        World world = Bukkit.getWorld(LUNAR_WORLD_KEY);
        if (world == null) {
            try {
                WorldCreator creator = WorldCreator.ofKey(LUNAR_WORLD_KEY);
                creator.generator(chunkGenerator);
                creator.biomeProvider(chunkGenerator.getDefaultBiomeProvider(null));
                creator.environment(World.Environment.NORMAL);
                world = Bukkit.createWorld(creator);
                getLogger().info("Successfully loaded Lunar dimension world (" + LUNAR_WORLD_KEY + ") with custom Java WorldGen!");
            } catch (Exception e) {
                getLogger().warning("Failed to load Lunar dimension world: " + e.getMessage());
            }
        }

        if (world != null) {
            world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(org.bukkit.GameRule.DO_WEATHER_CYCLE, false);
            suppressDragonBattle(world);
            ensurePaperWorldConfig(world);
        }
    }

    private void ensurePaperWorldConfig(World world) {
        try {
            java.io.File worldDir = world.getWorldFolder();
            java.io.File configFile = new java.io.File(worldDir, "paper-world.yml");
            String content = """
                    _version: 31
                    entities:
                      behavior:
                        scan-for-legacy-ender-dragon: false
                        should-remove-dragon: true
                    misc:
                      allow-remote-ender-dragon-respawning: false
                    """;
            if (!configFile.exists() || !java.nio.file.Files.readString(configFile.toPath()).contains("scan-for-legacy-ender-dragon: false")) {
                java.nio.file.Files.writeString(configFile.toPath(), content);
                getLogger().info("Configured paper-world.yml for " + world.getName() + " to disable legacy dragon scans.");
            }
        } catch (Throwable ignored) {}
    }

    public static void suppressDragonBattle(World world) {
        if (world == null) return;
        try {
            DragonBattle battle = world.getEnderDragonBattle();
            if (battle != null) {
                battle.setPreviouslyKilled(true);
                battle.setRespawnPhase(DragonBattle.RespawnPhase.NONE);
                if (battle.getBossBar() != null) {
                    battle.getBossBar().setVisible(false);
                    battle.getBossBar().removeAll();
                }
                if (battle.getEnderDragon() != null) {
                    battle.getEnderDragon().remove();
                }
            }
        } catch (Throwable ignored) {}

        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            EntityType type = entity.getType();
            if (type == EntityType.ENDER_DRAGON || type == EntityType.ENDERMAN) {
                entity.remove();
            }
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (worldConfig == null) {
            worldConfig = new LunarWorldConfig(this);
            worldConfig.load();
        }
        return new LunarChunkGenerator(worldConfig);
    }

    public LunarWorldConfig getWorldConfig() {
        return worldConfig;
    }

    public LunarChunkGenerator getLunarChunkGenerator() {
        return chunkGenerator;
    }
}
