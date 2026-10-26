package vn.haohan.worldgen;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

/**
 * Lunar biome definitions faithfully reflecting the 7 datapack biomes:
 * - lunar_maria
 * - lunar_terrae
 * - lunar_craters
 * - lunar_bright_patches
 * - lunar_crystal_craters
 * - lunar_giant_crystals
 * - lunar_giant_crystal_outskirts
 *
 * Mapped to vanilla The End biomes (the_end, end_highlands, end_midlands, end_barrens, small_end_islands)
 * to provide the native End skybox, void horizon, dark ambient lighting, and zero natural overworld spawns.
 */
public enum LunarBiome {
    LUNAR_MARIA(
            "Lunar Maria",
            NamespacedKey.minecraft("the_end"),
            Material.BLACK_CONCRETE_POWDER,
            Material.BLACKSTONE,
            Material.DEEPSLATE,
            Material.TUFF
    ),
    LUNAR_TERRAE(
            "Lunar Terrae",
            NamespacedKey.minecraft("end_highlands"),
            Material.LIGHT_GRAY_CONCRETE_POWDER,
            Material.ANDESITE,
            Material.STONE,
            Material.TUFF
    ),
    LUNAR_CRATERS(
            "Lunar Craters",
            NamespacedKey.minecraft("end_barrens"),
            Material.BLACK_CONCRETE_POWDER,
            Material.BLACKSTONE,
            Material.DEEPSLATE,
            Material.TUFF
    ),
    LUNAR_BRIGHT_PATCHES(
            "Lunar Bright Patches",
            NamespacedKey.minecraft("end_midlands"),
            Material.WHITE_CONCRETE_POWDER,
            Material.DIORITE,
            Material.CALCITE,
            Material.TUFF
    ),
    LUNAR_CRYSTAL_CRATERS(
            "Lunar Crystal Craters",
            NamespacedKey.minecraft("small_end_islands"),
            Material.AMETHYST_BLOCK,
            Material.CALCITE,
            Material.SMOOTH_QUARTZ,
            Material.TUFF
    ),
    LUNAR_GIANT_CRYSTALS(
            "Lunar Giant Crystals",
            NamespacedKey.minecraft("end_highlands"),
            Material.AMETHYST_BLOCK,
            Material.CALCITE,
            Material.SMOOTH_QUARTZ,
            Material.TUFF
    ),
    LUNAR_GIANT_CRYSTAL_OUTSKIRTS(
            "Lunar Giant Crystal Outskirts",
            NamespacedKey.minecraft("end_midlands"),
            Material.SMOOTH_QUARTZ,
            Material.CALCITE,
            Material.TUFF,
            Material.DEEPSLATE
    );

    private final String displayName;
    private final NamespacedKey vanillaBiomeKey;
    private final Material topBlock;
    private final Material secondaryTopBlock;
    private final Material subSurfaceBlock;
    private final Material baseStone;

    LunarBiome(String displayName, NamespacedKey vanillaBiomeKey, Material topBlock, Material secondaryTopBlock, Material subSurfaceBlock, Material baseStone) {
        this.displayName = displayName;
        this.vanillaBiomeKey = vanillaBiomeKey;
        this.topBlock = topBlock;
        this.secondaryTopBlock = secondaryTopBlock;
        this.subSurfaceBlock = subSurfaceBlock;
        this.baseStone = baseStone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NamespacedKey getVanillaBiomeKey() {
        return vanillaBiomeKey;
    }

    public Biome toVanillaBiome() {
        try {
            Biome biome = Registry.BIOME.get(vanillaBiomeKey);
            if (biome != null) return biome;
        } catch (Throwable ignored) {}
        return Biome.THE_END;
    }

    public Biome getVanillaBiome() {
        return toVanillaBiome();
    }

    public Material getTopBlock() {
        return topBlock;
    }

    public Material getSecondaryTopBlock() {
        return secondaryTopBlock;
    }

    public Material getSubSurfaceBlock() {
        return subSurfaceBlock;
    }

    public Material getBaseStone() {
        return baseStone;
    }
}
