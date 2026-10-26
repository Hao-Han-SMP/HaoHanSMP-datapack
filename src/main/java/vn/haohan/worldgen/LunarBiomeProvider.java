package vn.haohan.worldgen;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import vn.haohan.worldgen.noise.NormalNoise;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Custom BiomeProvider replicating Minecraft's multi_noise biome source
 * from data/haohan/dimension/lunar.json.
 */
public final class LunarBiomeProvider extends BiomeProvider {

    private record BiomeParameterPoint(
            LunarBiome biome,
            double minC, double maxC,
            double minE, double maxE,
            double minT, double maxT,
            double minH, double maxH,
            double minW, double maxW,
            double minD, double maxD,
            double offset
    ) {
        double distanceSquared(double c, double e, double t, double h, double w, double d) {
            double dc = dist(c, minC, maxC);
            double de = dist(e, minE, maxE);
            double dt = dist(t, minT, maxT);
            double dh = dist(h, minH, maxH);
            double dw = dist(w, minW, maxW);
            double dd = dist(d, minD, maxD);
            return dc * dc + de * de + dt * dt + dh * dh + dw * dw + dd * dd + offset;
        }

        private static double dist(double val, double min, double max) {
            if (val < min) return min - val;
            if (val > max) return val - max;
            return 0.0;
        }
    }

    // Exact parameters from data/haohan/dimension/lunar.json
    private static final BiomeParameterPoint[] PARAMETER_POINTS = new BiomeParameterPoint[] {
            new BiomeParameterPoint(LunarBiome.LUNAR_GIANT_CRYSTALS,
                    -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 0.58, 1.0, 0.56, 1.0, 0.0),
            new BiomeParameterPoint(LunarBiome.LUNAR_GIANT_CRYSTAL_OUTSKIRTS,
                    -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 0.58, 1.0, 0.32, 0.55, 0.0),
            new BiomeParameterPoint(LunarBiome.LUNAR_CRYSTAL_CRATERS,
                    -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 0.58, 1.0, -1.0, 0.31, 0.0),
            new BiomeParameterPoint(LunarBiome.LUNAR_BRIGHT_PATCHES,
                    0.15, 1.0, -1.0, 0.35, -1.0, 1.0, 0.72, 1.0, -1.0, 0.46, -1.0, 1.0, 0.0),
            new BiomeParameterPoint(LunarBiome.LUNAR_MARIA,
                    -1.0, -0.15, -1.0, 0.35, -1.0, -0.10, -1.0, 0.58, -1.0, 0.46, -1.0, 1.0, 0.0),
            new BiomeParameterPoint(LunarBiome.LUNAR_TERRAE,
                    -1.0, 1.0, -1.0, 0.40, -1.0, 1.0, -1.0, 0.58, -1.0, 0.46, -1.0, 1.0, 0.15),
            new BiomeParameterPoint(LunarBiome.LUNAR_CRATERS,
                    -1.0, 1.0, 0.30, 1.0, -1.0, 1.0, -1.0, 0.58, -1.0, 0.46, -1.0, 1.0, 0.10)
    };

    private final NormalNoise continentsNoise;
    private final NormalNoise erosionNoise;
    private final NormalNoise temperatureNoise;
    private final NormalNoise humidityNoise;
    private final NormalNoise weirdnessNoise;
    private final NormalNoise depthNoise;
    private final NormalNoise borderDetailNoise;
    private final NormalNoise warpXNoise;
    private final NormalNoise warpZNoise;

    public LunarBiomeProvider(long seed) {
        this.continentsNoise = new NormalNoise(seed, "haohan:lunar/biome/continents", -9, 1.0, 0.5);
        this.erosionNoise = new NormalNoise(seed, "haohan:lunar/biome/erosion", -10, 1.0, 0.5, 0.25, 0.12, 0.06);
        this.temperatureNoise = new NormalNoise(seed, "haohan:lunar/biome/temperature", -9, 1.0, 0.5, 0.25, 0.12, 0.06);
        this.humidityNoise = new NormalNoise(seed, "haohan:lunar/biome/bright_patches", -5, 1.0, 0.6, 0.3, 0.15);
        this.weirdnessNoise = new NormalNoise(seed, "haohan:lunar/biome/crystal_regions", -9, 1.0, 0.35, 0.15);
        this.depthNoise = new NormalNoise(seed, "haohan:lunar/biome/crystal_spires", -7, 1.0, 0.5, 0.25);
        this.borderDetailNoise = new NormalNoise(seed, "haohan:lunar/biome/border_detail", -6, 1.0, 0.5, 0.25);
        this.warpXNoise = new NormalNoise(seed ^ 0x51A8C1L, "haohan:lunar/biome/warp_x", -6, 1.0, 0.5);
        this.warpZNoise = new NormalNoise(seed ^ 0x937BF2L, "haohan:lunar/biome/warp_z", -6, 1.0, 0.5);
    }

    /**
     * Maps 2D world coordinates to one of the 7 Lunar biomes using Minecraft's
     * multi_noise nearest parameter point algorithm with high-frequency domain warping.
     */
    public LunarBiome getLunarBiome(int x, int z) {
        // High-frequency border domain warping for natural, fractal transitions
        double wx = x + warpXNoise.sample(x * 0.03, z * 0.03) * 18.0;
        double wz = z + warpZNoise.sample(z * 0.03, x * 0.03) * 18.0;

        double border = borderDetailNoise.sample(wx, wz);
        double c = continentsNoise.sample(wx, wz) + 0.045 * border;
        double e = erosionNoise.sample(wx, wz) + 0.05 * border;
        double t = temperatureNoise.sample(wx, wz) + 0.04 * border;
        double h = humidityNoise.sample(wx, wz) + 0.055 * border;
        double w = weirdnessNoise.sample(wx, wz, 0.8) + 0.05 * border;
        double d = depthNoise.sample(wx, wz) + 0.035 * border;

        double bestDistance = Double.MAX_VALUE;
        LunarBiome bestBiome = LunarBiome.LUNAR_TERRAE;

        for (BiomeParameterPoint point : PARAMETER_POINTS) {
            double distSq = point.distanceSquared(c, e, t, h, w, d);
            if (distSq < bestDistance) {
                bestDistance = distSq;
                bestBiome = point.biome();
            }
        }

        return bestBiome;
    }

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        Biome b = getLunarBiome(x, z).getVanillaBiome();
        return b != null ? b : Biome.THE_END;
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return Arrays.stream(LunarBiome.values())
                .map(LunarBiome::getVanillaBiome)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
