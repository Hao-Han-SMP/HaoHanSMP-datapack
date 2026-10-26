package vn.haohan.worldgen.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * World generation configuration loader and manager for the Lunar dimension.
 * Allows easy YAML-based tweaking and live reloading via commands.
 */
public class LunarWorldConfig {

    private final Plugin plugin;
    private final File configFile;
    private FileConfiguration config;

    // World bounds
    private int minHeight = -64;
    private int maxHeight = 320;
    private int seaLevel = -64;

    // Terrain settings
    private double mariaBaseHeight = 35.0;
    private double mariaRoughness = 2.5;
    private double highlandsBaseHeight = 68.0;
    private double highlandsRoughness = 14.0;

    // Craters
    private boolean cratersEnabled = true;
    private double craterFrequencyScale = 1.0;
    private double craterDepthScale = 1.0;
    private double craterRimScale = 1.0;

    // Volcanoes
    private boolean volcanoesEnabled = true;

    // Surface & slope rules
    private double slantThreshold = 1.2;
    private boolean obsidianCracksEnabled = true;

    // Ores & Features
    private boolean anorthositeEnabled = true;
    private boolean ilmeniteEnabled = true;
    private boolean kreepBasaltEnabled = true;
    private boolean pyroxeneEnabled = true;
    private boolean crystalsEnabled = true;
    private boolean bouldersEnabled = true;

    public LunarWorldConfig() {
        this.plugin = null;
        this.configFile = null;
    }

    public LunarWorldConfig(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = (plugin != null) ? new File(plugin.getDataFolder(), "worldgen.yml") : null;
    }

    public LunarWorldConfig(File dataFolder) {
        this.plugin = null;
        this.configFile = new File(dataFolder, "worldgen.yml");
    }

    public void load() {
        if (configFile == null) return;

        if (!configFile.exists()) {
            saveDefaults();
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        minHeight = config.getInt("world.min-height", -64);
        maxHeight = config.getInt("world.max-height", 320);
        seaLevel = config.getInt("world.sea-level", -64);

        mariaBaseHeight = config.getDouble("terrain.maria.base-height", 35.0);
        mariaRoughness = config.getDouble("terrain.maria.roughness", 2.5);
        highlandsBaseHeight = config.getDouble("terrain.highlands.base-height", 68.0);
        highlandsRoughness = config.getDouble("terrain.highlands.roughness", 14.0);

        cratersEnabled = config.getBoolean("craters.enabled", true);
        craterFrequencyScale = config.getDouble("craters.frequency-scale", 1.0);
        craterDepthScale = config.getDouble("craters.depth-scale", 1.0);
        craterRimScale = config.getDouble("craters.rim-scale", 1.0);

        volcanoesEnabled = config.getBoolean("volcanoes.enabled", true);

        slantThreshold = config.getDouble("surface.slant-threshold", 1.2);
        obsidianCracksEnabled = config.getBoolean("surface.obsidian-cracks", true);

        anorthositeEnabled = config.getBoolean("ores.anorthosite", true);
        ilmeniteEnabled = config.getBoolean("ores.ilmenite", true);
        kreepBasaltEnabled = config.getBoolean("ores.kreep-basalt", true);
        pyroxeneEnabled = config.getBoolean("ores.pyroxene", true);

        crystalsEnabled = config.getBoolean("features.crystals", true);
        bouldersEnabled = config.getBoolean("features.boulders", true);

        if (plugin != null) {
            plugin.getLogger().info("Loaded Lunar worldgen configuration (slantThreshold=" + slantThreshold + ")");
        }
    }

    public void saveDefaults() {
        if (configFile == null) return;

        File parent = configFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        config = new YamlConfiguration();
        config.set("world.min-height", -64);
        config.set("world.max-height", 320);
        config.set("world.sea-level", -64);

        config.set("terrain.maria.base-height", 35.0);
        config.set("terrain.maria.roughness", 2.5);
        config.set("terrain.highlands.base-height", 68.0);
        config.set("terrain.highlands.roughness", 14.0);

        config.set("craters.enabled", true);
        config.set("craters.frequency-scale", 1.0);
        config.set("craters.depth-scale", 1.0);
        config.set("craters.rim-scale", 1.0);

        config.set("volcanoes.enabled", true);

        config.set("surface.slant-threshold", 1.2);
        config.set("surface.obsidian-cracks", true);

        config.set("ores.anorthosite", true);
        config.set("ores.ilmenite", true);
        config.set("ores.kreep-basalt", true);
        config.set("ores.pyroxene", true);

        config.set("features.crystals", true);
        config.set("features.boulders", true);

        try {
            config.save(configFile);
        } catch (IOException e) {
            if (plugin != null) {
                plugin.getLogger().warning("Could not save default worldgen.yml: " + e.getMessage());
            }
        }
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public double getMariaBaseHeight() {
        return mariaBaseHeight;
    }

    public double getMariaRoughness() {
        return mariaRoughness;
    }

    public double getHighlandsBaseHeight() {
        return highlandsBaseHeight;
    }

    public double getHighlandsRoughness() {
        return highlandsRoughness;
    }

    public boolean isCratersEnabled() {
        return cratersEnabled;
    }

    public double getCraterFrequencyScale() {
        return craterFrequencyScale;
    }

    public double getCraterDepthScale() {
        return craterDepthScale;
    }

    public double getCraterRimScale() {
        return craterRimScale;
    }

    public boolean isVolcanoesEnabled() {
        return volcanoesEnabled;
    }

    public double getSlantThreshold() {
        return slantThreshold;
    }

    public boolean isObsidianCracksEnabled() {
        return obsidianCracksEnabled;
    }

    public boolean isAnorthositeEnabled() {
        return anorthositeEnabled;
    }

    public boolean isIlmeniteEnabled() {
        return ilmeniteEnabled;
    }

    public boolean isKreepBasaltEnabled() {
        return kreepBasaltEnabled;
    }

    public boolean isPyroxeneEnabled() {
        return pyroxeneEnabled;
    }

    public boolean isCrystalsEnabled() {
        return crystalsEnabled;
    }

    public boolean isBouldersEnabled() {
        return bouldersEnabled;
    }
}
