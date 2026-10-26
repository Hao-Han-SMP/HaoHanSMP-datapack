package vn.haohan.worldgen;

import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import vn.haohan.worldgen.cave.LunarCavePipeline;
import vn.haohan.worldgen.config.LunarWorldConfig;
import vn.haohan.worldgen.populator.LunarPopulator;
import vn.haohan.worldgen.populator.LunarStructurePopulator;
import vn.haohan.worldgen.surface.LunarSurfacePipeline;
import vn.haohan.worldgen.terrain.LunarTerrainPipeline;

import java.util.List;
import java.util.Random;

/**
 * Modern Paper ChunkGenerator for the Lunar dimension.
 * Implements the exact world generation pipeline ported from the Lunar datapack:
 * - 2D density function terrain (Maria, Highlands, Craters, Volcanoes)
 * - Strata surface rules (elevation gradients, fractal blending, and subsurface layers)
 * - Custom multi-noise biomes
 * - Space/Lunar cave systems (Lava tubes, Crystal geodes, Fissure canyons)
 * - Note-block custom ores (Kreep Basalt, Pyroxene, Ilmenite, Anorthosite)
 * - Multithreaded chunk generation
 */
public final class LunarChunkGenerator extends ChunkGenerator {

    private final LunarWorldConfig config;
    private final LunarBiomeProvider biomeProvider;
    private final LunarTerrainPipeline terrainPipeline;
    private final LunarSurfacePipeline surfacePipeline;
    private final LunarCavePipeline cavePipeline;
    private final LunarPopulator populator;
    private final LunarStructurePopulator structurePopulator;

    public LunarChunkGenerator(LunarWorldConfig config) {
        this(config, 19690720L); // Default seed: Apollo 11 Moon landing date
    }

    public LunarChunkGenerator(LunarWorldConfig config, long seed) {
        this.config = config;
        this.biomeProvider = new LunarBiomeProvider(seed);
        this.terrainPipeline = new LunarTerrainPipeline(seed, config);
        this.surfacePipeline = new LunarSurfacePipeline(seed, config, biomeProvider, terrainPipeline);
        this.cavePipeline = new LunarCavePipeline(seed, config, biomeProvider, terrainPipeline);
        this.populator = new LunarPopulator(config, biomeProvider);
        this.structurePopulator = new LunarStructurePopulator(config, biomeProvider, terrainPipeline);
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return biomeProvider;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return List.of(populator, structurePopulator);
    }

    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        int minY = chunkData.getMinHeight();
        chunkData.setRegion(0, minY, 0, 16, minY + 1, 16, Material.BEDROCK);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY + 1; y <= minY + 4; y++) {
                    if (random.nextInt(5) < (minY + 5 - y)) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    }
                }
            }
        }
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        int originX = chunkX << 4;
        int originZ = chunkZ << 4;
        int minY = chunkData.getMinHeight();

        // Fast bulk fill bottom solid bedrock layers
        chunkData.setRegion(0, minY, 0, 16, minY + 2, 16, Material.BEDROCK);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = originX + x;
                int worldZ = originZ + z;
                int topY = terrainPipeline.calculateHeight(worldX, worldZ);

                if (topY >= minY + 2) {
                    chunkData.setRegion(x, minY + 2, z, x + 1, topY + 1, z + 1, Material.TUFF);
                }
            }
        }
    }

    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        cavePipeline.applyCaves(chunkData, chunkX, chunkZ);
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        int originX = chunkX << 4;
        int originZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = originX + x;
                int worldZ = originZ + z;
                int topY = terrainPipeline.calculateHeight(worldX, worldZ);
                surfacePipeline.applyColumn(chunkData, x, z, worldX, worldZ, topY);
            }
        }
    }

    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap) {
        return terrainPipeline.calculateHeight(x, z);
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateBedrock() {
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    public LunarWorldConfig getConfig() {
        return config;
    }

    public LunarBiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    public LunarTerrainPipeline getTerrainPipeline() {
        return terrainPipeline;
    }

    public LunarSurfacePipeline getSurfacePipeline() {
        return surfacePipeline;
    }

    public LunarCavePipeline getCavePipeline() {
        return cavePipeline;
    }

    public LunarPopulator getPopulator() {
        return populator;
    }

    public LunarStructurePopulator getStructurePopulator() {
        return structurePopulator;
    }
}
