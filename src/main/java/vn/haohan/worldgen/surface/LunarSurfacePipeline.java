package vn.haohan.worldgen.surface;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import vn.haohan.worldgen.LunarBiome;
import vn.haohan.worldgen.LunarBiomeProvider;
import vn.haohan.worldgen.config.LunarWorldConfig;
import vn.haohan.worldgen.noise.NormalNoise;
import vn.haohan.worldgen.terrain.LunarTerrainPipeline;

/**
 * Surface rules and geological strata faithfully converted from data/haohan/worldgen/noise_settings/lunar.json.
 * Uses the exact datapack sequence and mathematics, shifted by Delta Y = +72 to match elevated plugin terrain.
 */
public final class LunarSurfacePipeline {

    private final LunarWorldConfig config;
    private final LunarBiomeProvider biomeProvider;
    private final LunarTerrainPipeline terrainPipeline;

    private final NormalNoise fractalMixNoise;
    private final NormalNoise gradientDetailNoise;
    private final NormalNoise obsidianCracksNoise;
    private final NormalNoise surfaceMixNoise;
    private final NormalNoise surfaceDepthNoise;

    public LunarSurfacePipeline(long seed, LunarWorldConfig config, LunarBiomeProvider biomeProvider) {
        this(seed, config, biomeProvider, null);
    }

    public LunarSurfacePipeline(long seed, LunarWorldConfig config, LunarBiomeProvider biomeProvider,
                                LunarTerrainPipeline terrainPipeline) {
        this.config = config;
        this.biomeProvider = biomeProvider;
        this.terrainPipeline = terrainPipeline;

        this.fractalMixNoise = new NormalNoise(seed, "haohan:lunar/fractal_mix", -5, 1.0, 0.7, 0.45, 0.2, 0.08, 0.025);
        this.gradientDetailNoise = new NormalNoise(seed, "haohan:lunar/gradient_detail", -4, 1.0, 0.5, 0.15);
        this.obsidianCracksNoise = new NormalNoise(seed, "haohan:lunar/obsidian_cracks", -4, 1.0, 0.5, 0.25, 0.12, 0.06);
        this.surfaceMixNoise = new NormalNoise(seed, "haohan:lunar/surface_mix", -4, 1.0, 1.0, 0.5);
        this.surfaceDepthNoise = new NormalNoise(seed ^ 0x48A39CL, "haohan:lunar/surface_depth", -4, 1.0, 0.6, 0.3);
    }

    /**
     * Applies surface materials and subsurface strata to a vertical column.
     * Replicates the exact datapack offset: 0 floor rule and subsurface offset rules (offset: 8..60).
     */
    public void applyColumn(ChunkGenerator.ChunkData chunkData, int localX, int localZ,
                            int worldX, int worldZ, int topY) {

        LunarBiome biome = biomeProvider.getLunarBiome(worldX, worldZ);
        double fractalMix = fractalMixNoise.sample(worldX, worldZ);
        double gradDetail = gradientDetailNoise.sample(worldX, worldZ);
        double obsidianCracks = obsidianCracksNoise.sample(worldX, worldZ);
        double surfaceMix = surfaceMixNoise.sample(worldX, worldZ);

        // add_stone_depth: true (surface depth perturbation from datapack)
        double surfaceDepth = surfaceDepthNoise.sample(worldX, worldZ) * 3.5;
        double effectiveY = topY + surfaceDepth;

        // Exact floor material from datapack offset: 0 rule (height shifted by +72)
        Material floorMat = determineFloorMaterial(biome, effectiveY, fractalMix, gradDetail, obsidianCracks, surfaceMix);

        int minY = chunkData.getMinHeight();

        // Subsurface strata matching datapack offsets: 8, 13, 18, 23, 28, 33, 38, 53, 60
        for (int y = topY; y > minY + 4; y--) {
            Material existing = chunkData.getType(localX, y, localZ);
            if (existing == Material.AIR || existing == Material.CAVE_AIR
                    || existing == Material.MAGMA_BLOCK
                    || existing == Material.SMOOTH_BASALT
                    || existing == Material.AMETHYST_BLOCK
                    || existing == Material.BUDDING_AMETHYST
                    || existing == Material.CALCITE
                    || existing == Material.SMOOTH_QUARTZ
                    || existing == Material.AMETHYST_CLUSTER) {
                continue;
            }

            int depth = topY - y;
            Material subMat;

            // offset: 0 (depth 0..2)
            if (depth == 0) {
                subMat = floorMat;
            } else if (depth <= 2) {
                if (floorMat == Material.OBSIDIAN) {
                    subMat = Material.OBSIDIAN; // preserve crack thickness
                } else if (fractalMix > 0.45 && depth == 2) {
                    subMat = Material.TUFF;
                } else {
                    subMat = floorMat;
                }
            } else if (depth <= 8) {
                // offset: 8 -> minecraft:tuff
                subMat = Material.TUFF;
            } else if (depth <= 13) {
                // offset: 13 -> fractal_mix in [-0.1, 0.1]: diorite, grad_detail >= 0.0: andesite, else stone
                if (fractalMix >= -0.1 && fractalMix <= 0.1) subMat = Material.DIORITE;
                else if (gradDetail >= 0.0) subMat = Material.ANDESITE;
                else subMat = Material.STONE;
            } else if (depth <= 18) {
                // offset: 18 -> fractal_mix in [0.25, 0.45]: diorite, grad_detail <= -0.2: andesite, else stone
                if (fractalMix >= 0.25 && fractalMix <= 0.45) subMat = Material.DIORITE;
                else if (gradDetail <= -0.2) subMat = Material.ANDESITE;
                else subMat = Material.STONE;
            } else if (depth <= 23) {
                // offset: 23 -> fractal_mix in [-0.45, -0.25]: diorite, grad_detail in [-0.5, 0.5]: andesite, else stone
                if (fractalMix >= -0.45 && fractalMix <= -0.25) subMat = Material.DIORITE;
                else if (gradDetail >= -0.5 && gradDetail <= 0.5) subMat = Material.ANDESITE;
                else subMat = Material.STONE;
            } else if (depth <= 28) {
                // offset: 28 -> fractal_mix in [0.35, 0.55]: diorite, grad_detail >= 0.3: andesite, else stone
                if (fractalMix >= 0.35 && fractalMix <= 0.55) subMat = Material.DIORITE;
                else if (gradDetail >= 0.3) subMat = Material.ANDESITE;
                else subMat = Material.STONE;
            } else if (depth <= 33) {
                // offset: 33 -> fractal_mix in [-0.2, 0.0]: diorite, grad_detail <= 0.1: andesite, else stone
                if (fractalMix >= -0.2 && fractalMix <= 0.0) subMat = Material.DIORITE;
                else if (gradDetail <= 0.1) subMat = Material.ANDESITE;
                else subMat = Material.STONE;
            } else if (depth <= 38) {
                // offset: 38 -> fractal_mix in [0.1, 0.3]: diorite, grad_detail >= -0.4: andesite, else stone
                if (fractalMix >= 0.1 && fractalMix <= 0.3) subMat = Material.DIORITE;
                else if (gradDetail >= -0.4) subMat = Material.ANDESITE;
                else subMat = Material.STONE;
            } else if (depth <= 53) {
                // offset: 53 -> minecraft:deepslate
                subMat = Material.DEEPSLATE;
            } else if (depth <= 60) {
                // offset: 60 -> minecraft:blackstone
                subMat = Material.BLACKSTONE;
            } else {
                // > 60 -> minecraft:black_concrete_powder
                subMat = Material.BLACK_CONCRETE_POWDER;
            }

            chunkData.setBlock(localX, y, localZ, subMat);
        }
    }

    /**
     * Replicates the exact offset: 0 sequence from data/haohan/worldgen/noise_settings/lunar.json.
     * All elevation anchors shifted by +72 (e.g., 76 -> 148, 18 -> 90).
     */
    private Material determineFloorMaterial(LunarBiome biome, double effectiveY,
                                            double fractalMix, double gradDetail,
                                            double obsidianCracks, double surfaceMix) {

        // --- Sequence 1: Crystal Biomes ---
        if (biome == LunarBiome.LUNAR_CRYSTAL_CRATERS
                || biome == LunarBiome.LUNAR_GIANT_CRYSTALS
                || biome == LunarBiome.LUNAR_GIANT_CRYSTAL_OUTSKIRTS) {
            double absCracks = Math.abs(obsidianCracks);
            if (absCracks < 0.018) return Material.COBBLED_DEEPSLATE;
            if (absCracks < 0.035) return Material.TUFF;
            if (absCracks < 0.08) return Material.AMETHYST_BLOCK;
            if (Math.abs(surfaceMix) < 0.35) return Material.CALCITE;
            return Material.SMOOTH_QUARTZ;
        }

        // --- Sequence 2: Bright Patches ---
        if (biome == LunarBiome.LUNAR_BRIGHT_PATCHES) {
            if (surfaceMix < -0.2) return Material.DIORITE;
            return Material.WHITE_CONCRETE_POWDER;
        }

        // --- Sequence 3: Elevation Bands (datapack anchors shifted by +72) ---
        // Datapack anchor 76 -> 148
        if (effectiveY > 148.0) {
            return Material.LIGHT_GRAY_CONCRETE_POWDER;
        }

        // Datapack anchor 70 -> 142
        if (effectiveY > 142.0) {
            if (fractalMix >= -0.12 || gradDetail >= 0.28) {
                return Material.LIGHT_GRAY_CONCRETE_POWDER;
            }
            return Material.ANDESITE;
        }

        // Datapack anchor 64 -> 136
        if (effectiveY > 136.0) {
            return Material.ANDESITE;
        }

        // Datapack anchor 58 -> 130
        if (effectiveY > 130.0) {
            if (fractalMix >= -0.12 || gradDetail >= 0.28) {
                return Material.ANDESITE;
            }
            return Material.GRAVEL;
        }

        // Datapack anchor 54 -> 126
        if (effectiveY > 126.0) {
            return Material.GRAVEL;
        }

        // Datapack anchor 48 -> 120
        if (effectiveY > 120.0) {
            if (fractalMix >= -0.12 || gradDetail >= 0.28) {
                return Material.GRAVEL;
            }
            return Material.DEEPSLATE;
        }

        // Datapack anchor 44 -> 116
        if (effectiveY > 116.0) {
            return Material.DEEPSLATE;
        }

        // Datapack anchor 38 -> 110
        if (effectiveY > 110.0) {
            if (fractalMix >= -0.12 || gradDetail >= 0.28) {
                return Material.DEEPSLATE;
            }
            return Material.BLACKSTONE;
        }

        // Datapack anchor 30 -> 102
        if (effectiveY > 102.0) {
            return Material.BLACKSTONE;
        }

        // Datapack anchor 24 -> 96
        if (effectiveY > 96.0) {
            if (fractalMix >= -0.12 || gradDetail >= 0.28) {
                return Material.BLACKSTONE;
            }
            return Material.BLACK_CONCRETE_POWDER;
        }

        // Datapack anchor 18 -> 90
        if (effectiveY > 90.0) {
            return Material.BLACK_CONCRETE_POWDER;
        }

        // Datapack below 18 (effectiveY <= 90): Deepest Crater Basins & Trenches
        double absCracks = Math.abs(obsidianCracks);
        if (absCracks < 0.025) {
            return Material.OBSIDIAN;
        }
        if (absCracks < 0.07) {
            return Material.BLACKSTONE;
        }
        return Material.BLACK_CONCRETE_POWDER;
    }
}
