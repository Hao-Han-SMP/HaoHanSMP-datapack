package vn.haohan.worldgen.terrain;

import vn.haohan.worldgen.config.LunarWorldConfig;
import vn.haohan.worldgen.noise.CubicSpline;
import vn.haohan.worldgen.noise.CubicSpline.Point;
import vn.haohan.worldgen.noise.NormalNoise;

/**
 * 100% Java implementation of the original Lunar datapack terrain density pipeline.
 * Faithfully ports:
 * - data/haohan/worldgen/density_function/lunar/final_density.json
 * - data/haohan/worldgen/density_function/lunar/components.json
 * - data/haohan/worldgen/density_function/lunar/terrain/ (base_relief, crystal_basin_relief, highlands_relief, mare_relief)
 * - data/haohan/worldgen/density_function/lunar/meteor_hole/ (micro, small, large, mega, basin, depressions, reliefs)
 * - data/haohan/worldgen/density_function/lunar/volcano/ (large, offset)
 */
public final class LunarTerrainPipeline {

    // Cubic Hermite Splines ported exactly from the datapack
    public static final CubicSpline CONTINENTS_SPLINE = new CubicSpline(
            new Point(-1.0, 1.0),
            new Point(-0.45, 0.96),
            new Point(-0.20, 0.85),
            new Point(0.02, 0.45),
            new Point(0.18, 0.0),
            new Point(1.0, 0.0)
    );

    public static final CubicSpline TEMPERATURE_SPLINE = new CubicSpline(
            new Point(-1.0, 1.0),
            new Point(-0.45, 0.96),
            new Point(-0.20, 0.85),
            new Point(0.02, 0.45),
            new Point(0.18, 0.0),
            new Point(1.0, 0.0)
    );

    public static final CubicSpline MARE_ROLLING_SPLINE = new CubicSpline(
            new Point(-1.0, -0.48),
            new Point(-0.4, -0.38),
            new Point(0.0, -0.30),
            new Point(0.5, -0.22),
            new Point(1.0, -0.16)
    );

    public static final CubicSpline HIGHLANDS_ROLLING_SPLINE = new CubicSpline(
            new Point(-1.0, 0.12),
            new Point(-0.3, 0.20),
            new Point(0.1, 0.32),
            new Point(0.5, 0.40),
            new Point(1.0, 0.48)
    );

    public static final CubicSpline CRYSTAL_BASIN_SPLINE = new CubicSpline(
            new Point(-1.0, 0.0, 0.0),
            new Point(0.3, 0.0, 0.0),
            new Point(0.42, 0.08, 0.0),
            new Point(0.52, 0.04, -0.35),
            new Point(0.62, -0.18, -0.55),
            new Point(0.76, -0.38, -0.25),
            new Point(1.0, -0.48, 0.0)
    );

    public static final CubicSpline METEOR_MICRO_SPLINE = new CubicSpline(
            new Point(-1.0, -0.105),
            new Point(-0.8, -0.082),
            new Point(-0.66, 0.035),
            new Point(-0.52, 0.0),
            new Point(0.52, 0.0),
            new Point(0.66, 0.035),
            new Point(0.8, -0.082),
            new Point(1.0, -0.105)
    );

    public static final CubicSpline METEOR_SMALL_SPLINE = new CubicSpline(
            new Point(-1.0, -0.215),
            new Point(-0.7, -0.175),
            new Point(-0.5, 0.05),
            new Point(-0.38, 0.0),
            new Point(0.38, 0.0),
            new Point(0.5, 0.05),
            new Point(0.7, -0.175),
            new Point(1.0, -0.215)
    );

    public static final CubicSpline METEOR_LARGE_SPLINE = new CubicSpline(
            new Point(-1.0, -0.325),
            new Point(-0.6, -0.275),
            new Point(-0.4, 0.075),
            new Point(-0.3, 0.0),
            new Point(0.3, 0.0),
            new Point(0.4, 0.075),
            new Point(0.6, -0.275),
            new Point(1.0, -0.325)
    );

    public static final CubicSpline METEOR_MEGA_SPLINE = new CubicSpline(
            new Point(-1.0, -0.425),
            new Point(-0.55, -0.365),
            new Point(-0.32, 0.12),
            new Point(-0.24, 0.0),
            new Point(0.24, 0.0),
            new Point(0.32, 0.12),
            new Point(0.55, -0.365),
            new Point(1.0, -0.425)
    );

    public static final CubicSpline METEOR_BASIN_SPLINE = new CubicSpline(
            new Point(-1.0, -0.55),
            new Point(-0.5, -0.46),
            new Point(-0.28, 0.16),
            new Point(-0.2, 0.0),
            new Point(0.2, 0.0),
            new Point(0.28, 0.16),
            new Point(0.5, -0.46),
            new Point(1.0, -0.55)
    );

    public static final CubicSpline VOLCANO_LARGE_SPLINE = new CubicSpline(
            new Point(-1.0, 0.0),
            new Point(0.45, 0.0),
            new Point(0.65, 0.025),
            new Point(0.78, 0.075),
            new Point(0.86, 0.14),
            new Point(0.89, 0.16),
            new Point(0.93, 0.02),
            new Point(0.97, -0.25),
            new Point(1.0, -0.38)
    );

    private final LunarWorldConfig config;

    // Climate and terrain noise ported faithfully from datapack definitions
    private final NormalNoise continentsNoise;
    private final NormalNoise temperatureNoise;
    private final NormalNoise crystalRegionsNoise;
    private final NormalNoise borderDetailNoise;

    private final NormalNoise rollingNoise;
    private final NormalNoise midNoise;
    private final NormalNoise detailNoise;
    private final NormalNoise highlandsNoise;

    private final NormalNoise meteorMicroNoise;
    private final NormalNoise meteorSmallNoise;
    private final NormalNoise meteorLargeNoise;
    private final NormalNoise meteorMegaNoise;
    private final NormalNoise meteorBasinNoise;

    private final NormalNoise volcanoLargeNoise;

    public LunarTerrainPipeline(long seed, LunarWorldConfig config) {
        this.config = config;

        this.continentsNoise = new NormalNoise(seed, "haohan:lunar/biome/continents", -9, 1.0, 0.5);
        this.temperatureNoise = new NormalNoise(seed, "haohan:lunar/biome/temperature", -9, 1.0, 0.5, 0.25, 0.12, 0.06);
        this.crystalRegionsNoise = new NormalNoise(seed, "haohan:lunar/biome/crystal_regions", -9, 1.0, 0.35, 0.15);
        this.borderDetailNoise = new NormalNoise(seed, "haohan:lunar/biome/border_detail", -6, 1.0, 0.5, 0.25);

        this.rollingNoise = new NormalNoise(seed, "haohan:lunar/terrain/rolling", -9, 1.0, 0.5, 0.25);
        this.midNoise = new NormalNoise(seed, "haohan:lunar/terrain/mid", -6, 1.0, 0.6, 0.3);
        this.detailNoise = new NormalNoise(seed, "haohan:lunar/terrain/detail", -3, 1.0, 0.5, 0.25);
        this.highlandsNoise = new NormalNoise(seed, "haohan:lunar/terrain/highlands", -5, 1.0, 0.7, 0.4);

        this.meteorMicroNoise = new NormalNoise(seed, "haohan:lunar/meteor_hole/micro", -5, 1.0);
        this.meteorSmallNoise = new NormalNoise(seed, "haohan:lunar/meteor_hole/small", -6, 1.0);
        this.meteorLargeNoise = new NormalNoise(seed, "haohan:lunar/meteor_hole/large", -8, 1.0);
        this.meteorMegaNoise = new NormalNoise(seed, "haohan:lunar/meteor_hole/mega", -9, 1.0);
        this.meteorBasinNoise = new NormalNoise(seed, "haohan:lunar/meteor_hole/basin", -11, 1.0);

        this.volcanoLargeNoise = new NormalNoise(seed, "haohan:lunar/volcano/large", -7, 1.0);
    }

    /**
     * Calculates the surface height Y at world coordinates (x, z) using the exact
     * density function mathematics from the original datapack.
     */
    public int calculateHeight(double x, double z) {
        // 1. Biome climate values
        double border = borderDetailNoise.sample(x, z);
        double continents = continentsNoise.sample(x, z) + 0.045 * border;
        double temperature = temperatureNoise.sample(x, z) + 0.04 * border;
        double crystalRegions = crystalRegionsNoise.sample(x, z, 0.8) + 0.05 * border;

        double splineContinents = CONTINENTS_SPLINE.sample(continents);
        double splineTemperature = TEMPERATURE_SPLINE.sample(temperature);

        // 2. Base terrain relief (smoothed slopes and gradual transitions)
        double mareRelief = MARE_ROLLING_SPLINE.sample(rollingNoise.sample(x, z, 1.2))
                + midNoise.sample(x, z, 1.1) * 0.015;

        double highlandsRelief = HIGHLANDS_ROLLING_SPLINE.sample(rollingNoise.sample(x, z, 1.45))
                + midNoise.sample(x, z, 1.25) * 0.05
                + detailNoise.sample(x, z, 1.35) * 0.005
                + highlandsNoise.sample(x, z, 1.50) * 0.03;

        double baseRelief = highlandsRelief + (mareRelief - highlandsRelief) * splineContinents * splineTemperature;
        double crystalBasinRelief = CRYSTAL_BASIN_SPLINE.sample(crystalRegions);
        double terrain = baseRelief + crystalBasinRelief;

        // 3. Meteor holes (deep, plunging impact craters with majestic rims)
        double micro = METEOR_MICRO_SPLINE.sample(meteorMicroNoise.sample(x, z));
        double small = METEOR_SMALL_SPLINE.sample(meteorSmallNoise.sample(x, z));
        double large = METEOR_LARGE_SPLINE.sample(meteorLargeNoise.sample(x, z));
        double mega = METEOR_MEGA_SPLINE.sample(meteorMegaNoise.sample(x, z));
        double basin = METEOR_BASIN_SPLINE.sample(meteorBasinNoise.sample(x, z));

        // Plunging deep craters for large, mega, and basin impacts
        double majorDepressions = Math.min(0.0, Math.min(large * 1.35, Math.min(mega * 1.45, basin * 1.60)));
        double minorDepressions = Math.min(0.0, Math.min(micro * 0.15, small * 0.40));
        double depressions = majorDepressions + minorDepressions;

        // Smooth reliefs without jagged micro-steps
        double reliefs = Math.min(0.25, Math.max(0.0, Math.max(small * 0.35, Math.max(large, Math.max(mega, basin)))));

        // 4. Impact Crater system (flared outer rim, rounded crest, and curved inward bowl)
        double craterEffect = VOLCANO_LARGE_SPLINE.sample(volcanoLargeNoise.sample(x, z));

        // 5. haohan:lunar/components
        double components = terrain + depressions + reliefs + craterEffect;

        // 6. Elevated Lunar crust: base Y raised to ~108.0 to allow expansive subterranean cave networks
        // Lowland Maria ~ Y=55-90, Highland plains ~ Y=115-135, Mountain rims ~ Y=140-165
        double finalY = 108.0 + 44.0 * components;

        int clamped = (int) Math.round(finalY);
        int minY = config.getMinHeight() + 1;
        int maxY = config.getMaxHeight() - 4;
        return Math.max(minY, Math.min(maxY, clamped));
    }

    /**
     * Determines if a location falls within the sunken floor of a large impact crater.
     */
    public boolean isLargeCraterFloor(double x, double z) {
        double large = METEOR_LARGE_SPLINE.sample(meteorLargeNoise.sample(x, z));
        double mega = METEOR_MEGA_SPLINE.sample(meteorMegaNoise.sample(x, z));
        double basin = METEOR_BASIN_SPLINE.sample(meteorBasinNoise.sample(x, z));
        double craterEffect = VOLCANO_LARGE_SPLINE.sample(volcanoLargeNoise.sample(x, z));
        return (large < -0.15 || mega < -0.18 || basin < -0.22 || craterEffect < -0.12);
    }
}
