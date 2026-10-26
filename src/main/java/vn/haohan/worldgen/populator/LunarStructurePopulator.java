package vn.haohan.worldgen.populator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.structure.Palette;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.bukkit.block.BlockState;
import org.bukkit.util.BlockVector;
import vn.haohan.worldgen.LunarBiome;
import vn.haohan.worldgen.LunarBiomeProvider;
import vn.haohan.worldgen.config.LunarWorldConfig;
import vn.haohan.worldgen.terrain.LunarTerrainPipeline;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 100% Java implementation of structure placement for the Lunar dimension.
 * Directly replaces Minecraft jigsaw/datapack structure loading with high-performance,
 * multithread-safe NBT structure slicing.
 *
 * Implements all structures from the original datapack:
 * - Giant Crystal Spires (spire_a, spire_b, spire_c, spire_chisel, spire_crown, spire_fan, spire_monolith, spire_twins)
 * - Giant Crystal Outskirts (outskirt_a, outskirt_b, outskirt_blades, outskirt_c, outskirt_chunks, outskirt_leaner, outskirt_steps)
 * - Crystal Pillars (pillar_buttressed, pillar_fork, pillar_leaning, pillar_needle, pillar_tall, pillar_tapered)
 * - Crystal Craters (crater_a, crater_b, crater_c)
 * - Floating Crystal Islands (10 variants, hovering at surface + 28)
 * - Rest Base (rest_base.nbt)
 * - Space Station (space_station.nbt, fixed in high orbit at Y=210)
 * - Delta Abandoned Base Complex (center, lab, factory, storage, corridors, antenna, power poles, habitat pods)
 */
public final class LunarStructurePopulator extends BlockPopulator {

    private final LunarWorldConfig config;
    private final LunarBiomeProvider biomeProvider;
    private final LunarTerrainPipeline terrainPipeline;

    private final Map<String, Structure> structureCache = new ConcurrentHashMap<>();
    private final Map<String, List<BlockState>> sortedCraterBlocks = new ConcurrentHashMap<>();

    // 1. Giant Crystal Spire Field (8 variants)
    private static final List<String> GIANT_SPIRES = List.of(
            "/structures/giant_crystal_field/spire_a.nbt",
            "/structures/giant_crystal_field/spire_b.nbt",
            "/structures/giant_crystal_field/spire_c.nbt",
            "/structures/giant_crystal_field/spire_chisel.nbt",
            "/structures/giant_crystal_field/spire_crown.nbt",
            "/structures/giant_crystal_field/spire_fan.nbt",
            "/structures/giant_crystal_field/spire_monolith.nbt",
            "/structures/giant_crystal_field/spire_twins.nbt"
    );

    // 2. Giant Crystal Outskirts (7 variants)
    private static final List<String> OUTSKIRT_SPIRES = List.of(
            "/structures/giant_crystal_outskirts/outskirt_a.nbt",
            "/structures/giant_crystal_outskirts/outskirt_b.nbt",
            "/structures/giant_crystal_outskirts/outskirt_blades.nbt",
            "/structures/giant_crystal_outskirts/outskirt_c.nbt",
            "/structures/giant_crystal_outskirts/outskirt_chunks.nbt",
            "/structures/giant_crystal_outskirts/outskirt_leaner.nbt",
            "/structures/giant_crystal_outskirts/outskirt_steps.nbt"
    );

    // 3. Crystal Pillars (9 variants, weighted)
    public record WeightedStructure(String path, int weight) {}

    private static final List<WeightedStructure> CRYSTAL_PILLARS = List.of(
            new WeightedStructure("/structures/crystal_pillar/pillar_leaning.nbt", 3),
            new WeightedStructure("/structures/crystal_pillar/pillar_slender.nbt", 3),
            new WeightedStructure("/structures/crystal_pillar/pillar_shard.nbt", 3),
            new WeightedStructure("/structures/crystal_pillar/pillar_cluster.nbt", 2),
            new WeightedStructure("/structures/crystal_pillar/pillar_crooked.nbt", 2),
            new WeightedStructure("/structures/crystal_pillar/pillar_ridge.nbt", 2),
            new WeightedStructure("/structures/crystal_pillar/pillar_twins.nbt", 2),
            new WeightedStructure("/structures/crystal_pillar/pillar_forked.nbt", 1),
            new WeightedStructure("/structures/crystal_pillar/pillar_buttressed.nbt", 1)
    );

    // 4. Crystal Craters (3 variants)
    private static final List<String> CRYSTAL_CRATERS = List.of(
            "/structures/crystal_crater/crater_a.nbt",
            "/structures/crystal_crater/crater_b.nbt",
            "/structures/crystal_crater/crater_c.nbt"
    );

    // 5. Floating Crystal Islands (10 islands, weighted)
    private static final List<WeightedStructure> FLOATING_ISLANDS = List.of(
            new WeightedStructure("/structures/floating_crystal_island/island_small.nbt", 5),
            new WeightedStructure("/structures/floating_crystal_island/island_chip.nbt", 4),
            new WeightedStructure("/structures/floating_crystal_island/island_medium.nbt", 3),
            new WeightedStructure("/structures/floating_crystal_island/island_hook.nbt", 3),
            new WeightedStructure("/structures/floating_crystal_island/island_split.nbt", 2),
            new WeightedStructure("/structures/floating_crystal_island/island_crag.nbt", 2),
            new WeightedStructure("/structures/floating_crystal_island/island_needle.nbt", 2),
            new WeightedStructure("/structures/floating_crystal_island/island_crown.nbt", 2),
            new WeightedStructure("/structures/floating_crystal_island/island_large.nbt", 1),
            new WeightedStructure("/structures/floating_crystal_island/island_broad.nbt", 1)
    );

    private static final String REST_BASE_PATH = "/structures/rest_base/rest_base.nbt";
    private static final String SPACE_STATION_PATH = "/structures/space_station/space_station.nbt";

    // Delta Abandoned Base module parts
    private static final String DELTA_CENTER_PATH = "/structures/delta_abandoned_base/center.nbt";
    private static final String DELTA_LAB_PATH = "/structures/delta_abandoned_base/structure/lab.nbt";
    private static final String DELTA_FACTORY_PATH = "/structures/delta_abandoned_base/structure/factory.nbt";
    private static final String DELTA_STORAGE_PATH = "/structures/delta_abandoned_base/structure/storage.nbt";
    private static final String DELTA_PATH_MAIN = "/structures/delta_abandoned_base/path/path_to_main_structure.nbt";
    private static final String DELTA_PATH_STRAIGHT = "/structures/delta_abandoned_base/path/path_straight.nbt";
    private static final String DELTA_ANTENNA_PATH = "/structures/delta_abandoned_base/sub_structure/antenna.nbt";
    private static final String DELTA_POLE1_PATH = "/structures/delta_abandoned_base/sub_structure/electric_pole_1.nbt";
    private static final String DELTA_POLE2_PATH = "/structures/delta_abandoned_base/sub_structure/electric_pole_2.nbt";
    private static final String DELTA_HOUSE1_PATH = "/structures/delta_abandoned_base/sub_structure/small_house_1.nbt";
    private static final String DELTA_HOUSE2_PATH = "/structures/delta_abandoned_base/sub_structure/small_house_2.nbt";

    public LunarStructurePopulator(LunarWorldConfig config, LunarBiomeProvider biomeProvider, LunarTerrainPipeline terrainPipeline) {
        this.config = config;
        this.biomeProvider = biomeProvider;
        this.terrainPipeline = terrainPipeline;
    }

    public LunarStructurePopulator(LunarWorldConfig config, LunarBiomeProvider biomeProvider) {
        this(config, biomeProvider, new LunarTerrainPipeline(19690720L, config));
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        if (Bukkit.getServer() == null) return; // Safeguard for unit test environments

        // 1. Giant Crystal Spire Field (spacing 3, separation 1, salt 93476125)
        checkAndPlaceStructures(chunkX, chunkZ, limitedRegion, 3, 1, 93476125, 3, -5, false,
                GIANT_SPIRES, LunarBiome.LUNAR_GIANT_CRYSTALS);

        // 2. Giant Crystal Outskirts (spacing 3, separation 1, salt 93476191)
        checkAndPlaceStructures(chunkX, chunkZ, limitedRegion, 3, 1, 93476191, 2, -5, false,
                OUTSKIRT_SPIRES, LunarBiome.LUNAR_GIANT_CRYSTAL_OUTSKIRTS);

        // 3. Crystal Pillars (spacing 7, separation 3, salt 93476237)
        checkAndPlaceWeighted(chunkX, chunkZ, limitedRegion, 7, 3, 93476237, 1, -3, false,
                CRYSTAL_PILLARS, null, true);

        // 4. Crystal Craters (spacing 12, separation 5, salt 184736521) - Anchored at rim with bowl carved
        checkAndPlaceCraters(chunkX, chunkZ, limitedRegion, 12, 5, 184736521, 3);

        // 5. Floating Crystal Islands (spacing 9, separation 5, salt 93476319, height: surface + 28)
        checkAndPlaceWeighted(chunkX, chunkZ, limitedRegion, 9, 5, 93476319, 1, 28, false,
                FLOATING_ISLANDS, null, true);

        // 6. Rest Base (spacing 120, separation 40, salt 58291047, height: surface)
        checkAndPlaceSingle(chunkX, chunkZ, limitedRegion, 120, 40, 58291047, 1, 0, true,
                REST_BASE_PATH, LunarBiome.LUNAR_TERRAE);

        // 7. Space Station (spacing 160, separation 50, salt 38947120, height: fixed Y=210 in orbit)
        checkAndPlaceOrbital(chunkX, chunkZ, limitedRegion, 160, 50, 38947120, 6, 210,
                SPACE_STATION_PATH, LunarBiome.LUNAR_TERRAE);

        // 8. Delta Abandoned Base - Complete modular research base complex with underground research bunker
        checkAndPlaceDeltaBaseComplex(chunkX, chunkZ, limitedRegion, 120, 40, 48291053, 6);
    }

    private void checkAndPlaceStructures(int chunkX, int chunkZ, LimitedRegion region,
                                         int spacing, int separation, int salt, int searchRadius,
                                         int heightOffset, boolean carveAir,
                                         List<String> paths, LunarBiome requiredBiome) {
        for (int acx = chunkX - searchRadius; acx <= chunkX + searchRadius; acx++) {
            for (int acz = chunkZ - searchRadius; acz <= chunkZ + searchRadius; acz++) {
                if (isSpreadChunk(acx, acz, spacing, separation, salt)) {
                    long seed = getDeterministicSeed(acx, acz, salt);
                    Random rand = new Random(seed);

                    int anchorX = (acx << 4) + rand.nextInt(8);
                    int anchorZ = (acz << 4) + rand.nextInt(8);

                    // Sample height near center of structure
                    int surfaceY = terrainPipeline.calculateHeight(anchorX + 16, anchorZ + 16);
                    LunarBiome biome = biomeProvider.getLunarBiome(anchorX + 16, anchorZ + 16);
                    if (requiredBiome != null && biome != requiredBiome) continue;

                    String path = paths.get(rand.nextInt(paths.size()));
                    int rot = rand.nextInt(4);
                    int startY = surfaceY + heightOffset;

                    renderStructureSlice(region, chunkX, chunkZ, path, anchorX, startY, anchorZ, rot, carveAir);
                }
            }
        }
    }

    private void checkAndPlaceCraters(int chunkX, int chunkZ, LimitedRegion region,
                                      int spacing, int separation, int salt, int searchRadius) {
        for (int acx = chunkX - searchRadius; acx <= chunkX + searchRadius; acx++) {
            for (int acz = chunkZ - searchRadius; acz <= chunkZ + searchRadius; acz++) {
                if (isSpreadChunk(acx, acz, spacing, separation, salt)) {
                    long seed = getDeterministicSeed(acx, acz, salt);
                    Random rand = new Random(seed);

                    int anchorX = (acx << 4) + rand.nextInt(6);
                    int anchorZ = (acz << 4) + rand.nextInt(6);

                    int centerX = anchorX + 24;
                    int centerZ = anchorZ + 24;
                    LunarBiome biome = biomeProvider.getLunarBiome(centerX, centerZ);
                    if (biome != LunarBiome.LUNAR_CRYSTAL_CRATERS) continue;

                    // Sample terrain heights around crater center to calculate natural slope
                    int hCenter = terrainPipeline.calculateHeight(centerX, centerZ);
                    int hEast = terrainPipeline.calculateHeight(centerX + 18, centerZ);
                    int hWest = terrainPipeline.calculateHeight(centerX - 18, centerZ);
                    int hSouth = terrainPipeline.calculateHeight(centerX, centerZ + 18);
                    int hNorth = terrainPipeline.calculateHeight(centerX, centerZ - 18);

                    // Terrain gradient (slope) for self-tilting
                    double rawSlopeX = (hEast - hWest) / 36.0;
                    double rawSlopeZ = (hSouth - hNorth) / 36.0;
                    double slopeLen = Math.hypot(rawSlopeX, rawSlopeZ);

                    // Clamp maximum tilt to 0.40 (~22 deg) to preserve structural integrity
                    double maxSlope = 0.40;
                    double slopeX = rawSlopeX;
                    double slopeZ = rawSlopeZ;
                    if (slopeLen > maxSlope) {
                        slopeX = (rawSlopeX / slopeLen) * maxSlope;
                        slopeZ = (rawSlopeZ / slopeLen) * maxSlope;
                    }

                    int avgSurfaceY = (hCenter * 2 + hEast + hWest + hSouth + hNorth) / 6;
                    int startY = avgSurfaceY - 40;

                    String path = CRYSTAL_CRATERS.get(rand.nextInt(CRYSTAL_CRATERS.size()));
                    int rot = rand.nextInt(4);

                    // Render with slope self-tilting, surface terrain clearing, and floating trim
                    renderCraterSlice(region, chunkX, chunkZ, path, anchorX, startY, anchorZ, rot, centerX, centerZ, slopeX, slopeZ);
                }
            }
        }
    }

    private void checkAndPlaceWeighted(int chunkX, int chunkZ, LimitedRegion region,
                                       int spacing, int separation, int salt, int searchRadius,
                                       int heightOffset, boolean carveAir,
                                       List<WeightedStructure> list, LunarBiome requiredBiome, boolean crystalBiomesOnly) {
        for (int acx = chunkX - searchRadius; acx <= chunkX + searchRadius; acx++) {
            for (int acz = chunkZ - searchRadius; acz <= chunkZ + searchRadius; acz++) {
                if (isSpreadChunk(acx, acz, spacing, separation, salt)) {
                    long seed = getDeterministicSeed(acx, acz, salt);
                    Random rand = new Random(seed);

                    int anchorX = (acx << 4) + rand.nextInt(8);
                    int anchorZ = (acz << 4) + rand.nextInt(8);

                    int surfaceY = terrainPipeline.calculateHeight(anchorX + 8, anchorZ + 8);
                    LunarBiome biome = biomeProvider.getLunarBiome(anchorX + 8, anchorZ + 8);
                    if (crystalBiomesOnly && !isCrystalBiome(biome)) continue;
                    if (requiredBiome != null && biome != requiredBiome) continue;

                    String path = pickWeighted(list, rand);
                    int rot = rand.nextInt(4);
                    int startY = surfaceY + heightOffset;

                    renderStructureSlice(region, chunkX, chunkZ, path, anchorX, startY, anchorZ, rot, carveAir);
                }
            }
        }
    }

    private void checkAndPlaceSingle(int chunkX, int chunkZ, LimitedRegion region,
                                     int spacing, int separation, int salt, int searchRadius,
                                     int heightOffset, boolean carveAir,
                                     String path, LunarBiome requiredBiome) {
        for (int acx = chunkX - searchRadius; acx <= chunkX + searchRadius; acx++) {
            for (int acz = chunkZ - searchRadius; acz <= chunkZ + searchRadius; acz++) {
                if (isSpreadChunk(acx, acz, spacing, separation, salt)) {
                    long seed = getDeterministicSeed(acx, acz, salt);
                    Random rand = new Random(seed);

                    int anchorX = (acx << 4) + rand.nextInt(8);
                    int anchorZ = (acz << 4) + rand.nextInt(8);

                    int surfaceY = terrainPipeline.calculateHeight(anchorX + 10, anchorZ + 10);
                    LunarBiome biome = biomeProvider.getLunarBiome(anchorX + 10, anchorZ + 10);
                    if (requiredBiome != null && biome != requiredBiome) continue;

                    int rot = rand.nextInt(4);
                    int startY = surfaceY + heightOffset;

                    renderStructureSlice(region, chunkX, chunkZ, path, anchorX, startY, anchorZ, rot, carveAir);
                }
            }
        }
    }

    private void checkAndPlaceOrbital(int chunkX, int chunkZ, LimitedRegion region,
                                      int spacing, int separation, int salt, int searchRadius,
                                      int fixedY, String path, LunarBiome requiredBiome) {
        for (int acx = chunkX - searchRadius; acx <= chunkX + searchRadius; acx++) {
            for (int acz = chunkZ - searchRadius; acz <= chunkZ + searchRadius; acz++) {
                if (isSpreadChunk(acx, acz, spacing, separation, salt)) {
                    long seed = getDeterministicSeed(acx, acz, salt);
                    Random rand = new Random(seed);

                    int anchorX = (acx << 4) + rand.nextInt(8);
                    int anchorZ = (acz << 4) + rand.nextInt(8);

                    LunarBiome biome = biomeProvider.getLunarBiome(anchorX, anchorZ);
                    if (requiredBiome != null && biome != requiredBiome) continue;

                    int rot = rand.nextInt(4);
                    renderStructureSlice(region, chunkX, chunkZ, path, anchorX, fixedY, anchorZ, rot, true);
                }
            }
        }
    }

    /**
     * Places the complete Delta Abandoned Base research compound:
     * - Central Command / Habitat module (center.nbt)
     * - Pressurized connecting corridors (path_to_main_structure.nbt, path_straight.nbt)
     * - Research Laboratory (lab.nbt)
     * - Mineral Processing Factory (factory.nbt)
     * - Underground Resource Storage Depot (storage.nbt)
     * - Deep Space Comms Antenna (antenna.nbt)
     * - Power Transmission Pylons (electric_pole_1.nbt, electric_pole_2.nbt)
     * - Autonomous Habitat Living Pods (small_house_1.nbt, small_house_2.nbt)
     * - Excavated Subterranean Research Bunker under the central dome
     */
    private void checkAndPlaceDeltaBaseComplex(int chunkX, int chunkZ, LimitedRegion region,
                                               int spacing, int separation, int salt, int searchRadius) {
        for (int acx = chunkX - searchRadius; acx <= chunkX + searchRadius; acx++) {
            for (int acz = chunkZ - searchRadius; acz <= chunkZ + searchRadius; acz++) {
                if (isSpreadChunk(acx, acz, spacing, separation, salt)) {
                    long seed = getDeterministicSeed(acx, acz, salt);
                    Random rand = new Random(seed);

                    int anchorX = (acx << 4) + rand.nextInt(4);
                    int anchorZ = (acz << 4) + rand.nextInt(4);

                    int surfaceY = terrainPipeline.calculateHeight(anchorX + 22, anchorZ + 22);
                    LunarBiome biome = biomeProvider.getLunarBiome(anchorX + 22, anchorZ + 22);
                    if (biome != LunarBiome.LUNAR_TERRAE && biome != LunarBiome.LUNAR_MARIA) continue;

                    int baseY = surfaceY - 1;

                    // 1. Central Command / Habitat Dome
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_CENTER_PATH, anchorX, baseY, anchorZ, 0, true);

                    // 2. South Corridor -> Research Laboratory
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_PATH_MAIN, anchorX + 15, baseY, anchorZ + 45, 0, true);
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_LAB_PATH, anchorX + 5, baseY, anchorZ + 59, 0, true);

                    // 3. East Corridor -> Resource Extraction Factory
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_PATH_STRAIGHT, anchorX + 45, baseY, anchorZ + 15, 1, true);
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_FACTORY_PATH, anchorX + 59, baseY, anchorZ + 5, 0, true);

                    // 4. West Corridor -> Supply & Geological Storage
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_PATH_STRAIGHT, anchorX - 14, baseY, anchorZ + 15, 1, true);
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_STORAGE_PATH, anchorX - 57, baseY, anchorZ + 5, 0, true);

                    // 5. Deep Space Comms Antenna & Power Grid
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_ANTENNA_PATH, anchorX + 20, baseY, anchorZ - 12, 0, false);
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_POLE1_PATH, anchorX + 38, baseY, anchorZ + 48, 0, false);
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_POLE2_PATH, anchorX - 8, baseY, anchorZ + 48, 0, false);

                    // 6. Perimeter Living Pods
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_HOUSE1_PATH, anchorX - 14, baseY, anchorZ + 32, 0, true);
                    renderStructureSlice(region, chunkX, chunkZ, DELTA_HOUSE2_PATH, anchorX + 50, baseY, anchorZ + 32, 0, true);

                    // 7. Excavated Subterranean Research Bunker under Central Dome
                    excavateUndergroundBunker(region, chunkX, chunkZ, anchorX + 14, baseY - 8, anchorZ + 14, 18, 7, 18);
                }
            }
        }
    }

    private void excavateUndergroundBunker(LimitedRegion region, int chunkX, int chunkZ,
                                         int bx, int by, int bz, int width, int height, int depth) {
        int chunkMinX = chunkX << 4;
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = chunkZ << 4;
        int chunkMaxZ = chunkMinZ + 15;

        if (bx + width - 1 < chunkMinX || bx > chunkMaxX || bz + depth - 1 < chunkMinZ || bz > chunkMaxZ) {
            return;
        }

        for (int x = 0; x < width; x++) {
            int wx = bx + x;
            if ((wx >> 4) != chunkX) continue;

            for (int z = 0; z < depth; z++) {
                int wz = bz + z;
                if ((wz >> 4) != chunkZ) continue;

                for (int y = 0; y < height; y++) {
                    int wy = by + y;
                    if (!region.isInRegion(wx, wy, wz)) continue;

                    boolean isFloor = (y == 0);
                    boolean isCeiling = (y == height - 1);
                    boolean isWall = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);

                    if (isFloor) {
                        region.setBlockData(wx, wy, wz, Material.POLISHED_ANDESITE.createBlockData());
                    } else if (isCeiling) {
                        if ((x == width / 2 || z == depth / 2) && (x % 4 == 0 || z % 4 == 0)) {
                            region.setBlockData(wx, wy, wz, Material.REDSTONE_LAMP.createBlockData());
                        } else {
                            region.setBlockData(wx, wy, wz, Material.SMOOTH_STONE.createBlockData());
                        }
                    } else if (isWall) {
                        region.setBlockData(wx, wy, wz, Material.SMOOTH_STONE.createBlockData());
                    } else {
                        // Interior hollow space
                        region.setBlockData(wx, wy, wz, Material.AIR.createBlockData());
                    }
                }
            }
        }
    }

    /**
     * Specialized crater renderer implementing:
     * 1. Terrain slope self-tilting: adapts the crater bowl angle to follow natural hill contours.
     * 2. Full surface terrain clearing: ensures no hillside or surface blocks cover the crater opening.
     * 3. Floating trim (gọt bớt): skips blocks hanging over cliffs, steep drop-offs, or open caverns.
     */
    private void renderCraterSlice(LimitedRegion region, int chunkX, int chunkZ,
                                   String path, int anchorX, int startY, int anchorZ, int rot,
                                   int centerX, int centerZ, double slopeX, double slopeZ) {
        Structure structure = getOrLoadStructure(path);
        if (structure == null) return;

        BlockVector size = structure.getSize();
        int sizeX = size.getBlockX();
        int sizeZ = size.getBlockZ();

        int widthX = (rot == 1 || rot == 3) ? sizeZ : sizeX;
        int widthZ = (rot == 1 || rot == 3) ? sizeX : sizeZ;

        int minBX = anchorX;
        int maxBX = anchorX + widthX - 1;
        int minBZ = anchorZ;
        int maxBZ = anchorZ + widthZ - 1;

        int chunkMinX = chunkX << 4;
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = chunkZ << 4;
        int chunkMaxZ = chunkMinZ + 15;

        // Skip immediately if the structure does not intersect the current chunk
        if (maxBX < chunkMinX || minBX > chunkMaxX || maxBZ < chunkMinZ || minBZ > chunkMaxZ) {
            return;
        }

        StructureRotation rotation = getBukkitRotation(rot);

        // Pre-sorted by Y ascending so lower solid blocks provide support for higher blocks
        List<BlockState> blocks = sortedCraterBlocks.computeIfAbsent(path, p -> {
            List<BlockState> list = new ArrayList<>();
            for (Palette palette : structure.getPalettes()) {
                list.addAll(palette.getBlocks());
            }
            list.sort(Comparator.comparingInt(BlockState::getY));
            return list;
        });

        for (BlockState block : blocks) {
            int rx = block.getX();
            int ry = block.getY();
            int rz = block.getZ();

            int worldX = anchorX + switch (rot) {
                case 1 -> sizeZ - 1 - rz;
                case 2 -> sizeX - 1 - rx;
                case 3 -> rz;
                default -> rx;
            };

            int worldZ = anchorZ + switch (rot) {
                case 1 -> rx;
                case 2 -> sizeZ - 1 - rz;
                case 3 -> sizeX - 1 - rx;
                default -> rz;
            };

            // Strict boundary check: ONLY place blocks that belong to the active chunk
            if ((worldX >> 4) != chunkX || (worldZ >> 4) != chunkZ) {
                continue;
            }

            // Tilt formula: shifts Y along the terrain gradient
            double dx = worldX - centerX;
            double dz = worldZ - centerZ;
            int tiltY = (int) Math.round(slopeX * dx + slopeZ * dz);
            int worldY = startY + ry + tiltY;

            if (worldY < config.getMinHeight() || worldY >= config.getMaxHeight()) {
                continue;
            }
            if (!region.isInRegion(worldX, worldY, worldZ)) {
                continue;
            }

            BlockData data = block.getBlockData();
            Material mat = data.getMaterial();
            if (mat == Material.STRUCTURE_VOID) continue;

            int colSurfaceY = terrainPipeline.calculateHeight(worldX, worldZ);

            if (mat == Material.AIR) {
                // Item 1: Carve air inside the bowl (never skip natural crystal surface materials!)
                region.setBlockData(worldX, worldY, worldZ, data);

                // Fix 1: If top air of structure (ry == 47), clear any higher hillside terrain up to surface
                if (ry == 47 && colSurfaceY > worldY) {
                    int clearMax = Math.min(colSurfaceY, config.getMaxHeight() - 1);
                    for (int cy = worldY + 1; cy <= clearMax; cy++) {
                        if (region.isInRegion(worldX, cy, worldZ)) {
                            region.setBlockData(worldX, cy, worldZ, data);
                        }
                    }
                }
            } else {
                // Solid structure block
                // Fix 2: Trim (gọt bớt) blocks floating in mid-air above terrain surface or into cave/void
                if (worldY > colSurfaceY + 2) {
                    continue; // Floating in the air above slope or cliff
                }

                Material existing = region.getType(worldX, worldY, worldZ);
                if (existing == Material.AIR || existing == Material.CAVE_AIR) {
                    // Placing into air requires solid support directly underneath
                    if (worldY > config.getMinHeight() && region.isInRegion(worldX, worldY - 1, worldZ)) {
                        Material below = region.getType(worldX, worldY - 1, worldZ);
                        if (below == Material.AIR || below == Material.CAVE_AIR) {
                            continue; // Floating in cave or void without support: trim!
                        }
                    }
                }

                if (rot != 0) {
                    data = data.clone();
                    data.rotate(rotation);
                }
                region.setBlockData(worldX, worldY, worldZ, data);
            }
        }
    }

    private void renderStructureSlice(LimitedRegion region, int chunkX, int chunkZ,
                                      String path, int anchorX, int startY, int anchorZ,
                                      int rot, boolean carveAir) {
        Structure structure = getOrLoadStructure(path);
        if (structure == null) return;

        BlockVector size = structure.getSize();
        int sizeX = size.getBlockX();
        int sizeZ = size.getBlockZ();

        int widthX = (rot == 1 || rot == 3) ? sizeZ : sizeX;
        int widthZ = (rot == 1 || rot == 3) ? sizeX : sizeZ;

        int minBX = anchorX;
        int maxBX = anchorX + widthX - 1;
        int minBZ = anchorZ;
        int maxBZ = anchorZ + widthZ - 1;

        int chunkMinX = chunkX << 4;
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = chunkZ << 4;
        int chunkMaxZ = chunkMinZ + 15;

        // Skip immediately if the structure does not intersect the current chunk
        if (maxBX < chunkMinX || minBX > chunkMaxX || maxBZ < chunkMinZ || minBZ > chunkMaxZ) {
            return;
        }

        StructureRotation rotation = getBukkitRotation(rot);

        for (Palette palette : structure.getPalettes()) {
            for (BlockState block : palette.getBlocks()) {
                int rx = block.getX();
                int ry = block.getY();
                int rz = block.getZ();

                int worldX = anchorX + switch (rot) {
                    case 1 -> sizeZ - 1 - rz;
                    case 2 -> sizeX - 1 - rx;
                    case 3 -> rz;
                    default -> rx;
                };

                int worldY = startY + ry;

                int worldZ = anchorZ + switch (rot) {
                    case 1 -> rx;
                    case 2 -> sizeZ - 1 - rz;
                    case 3 -> sizeX - 1 - rx;
                    default -> rz;
                };

                // Strict boundary check: ONLY place blocks that belong to the active chunk and region
                if ((worldX >> 4) == chunkX && (worldZ >> 4) == chunkZ) {
                    if (worldY >= config.getMinHeight() && worldY < config.getMaxHeight() && region.isInRegion(worldX, worldY, worldZ)) {
                        BlockData data = block.getBlockData();
                        Material mat = data.getMaterial();
                        if (mat == Material.STRUCTURE_VOID) continue;

                        if (!carveAir && mat == Material.AIR) continue;

                        // Item 4: Never slice crystal formations with crater air
                        if (carveAir && mat == Material.AIR) {
                            Material existing = region.getType(worldX, worldY, worldZ);
                            if (isCrystalMaterial(existing)) {
                                continue;
                            }
                        }

                        if (rot != 0) {
                            data = data.clone();
                            data.rotate(rotation);
                        }

                        region.setBlockData(worldX, worldY, worldZ, data);

                        // Item 4: Ground crystal spires/pillars if base is floating on uneven slopes/crater walls
                        if (isGroundCrystal(path) && ry == 0 && mat != Material.AIR) {
                            int checkY = worldY - 1;
                            int maxDown = Math.max(config.getMinHeight() + 5, worldY - 24);
                            Material baseFill = (mat == Material.CALCITE || mat == Material.SMOOTH_QUARTZ) ? Material.CALCITE : Material.AMETHYST_BLOCK;
                            while (checkY >= maxDown && region.isInRegion(worldX, checkY, worldZ)) {
                                Material below = region.getType(worldX, checkY, worldZ);
                                if (below == Material.AIR || below == Material.CAVE_AIR) {
                                    region.setBlockData(worldX, checkY, worldZ, baseFill.createBlockData());
                                    checkY--;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Structure getOrLoadStructure(String path) {
        return structureCache.computeIfAbsent(path, p -> {
            try (InputStream is = getClass().getResourceAsStream(p)) {
                if (is == null) {
                    Bukkit.getLogger().warning("[LunarStructurePopulator] Structure not found in resources: " + p);
                    return null;
                }
                byte[] bytes = is.readAllBytes();
                StructureManager manager = Bukkit.getStructureManager();
                return manager.loadStructure(new ByteArrayInputStream(bytes));
            } catch (Exception e) {
                Bukkit.getLogger().severe("[LunarStructurePopulator] Error loading structure " + p + ": " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Preloads all NBT structures into memory at startup.
     * Guarantees zero disk I/O, zero zip locks, and nanosecond retrieval during multithreaded chunk generation.
     */
    public void preloadAllStructures() {
        if (Bukkit.getServer() == null) return;
        List<String> allPaths = new ArrayList<>();
        allPaths.addAll(GIANT_SPIRES);
        allPaths.addAll(OUTSKIRT_SPIRES);
        for (WeightedStructure ws : CRYSTAL_PILLARS) allPaths.add(ws.path());
        allPaths.addAll(CRYSTAL_CRATERS);
        for (WeightedStructure ws : FLOATING_ISLANDS) allPaths.add(ws.path());
        allPaths.add(REST_BASE_PATH);
        allPaths.add(SPACE_STATION_PATH);
        allPaths.add(DELTA_CENTER_PATH);
        allPaths.add(DELTA_LAB_PATH);
        allPaths.add(DELTA_FACTORY_PATH);
        allPaths.add(DELTA_STORAGE_PATH);
        allPaths.add(DELTA_PATH_MAIN);
        allPaths.add(DELTA_PATH_STRAIGHT);
        allPaths.add(DELTA_ANTENNA_PATH);
        allPaths.add(DELTA_POLE1_PATH);
        allPaths.add(DELTA_POLE2_PATH);
        allPaths.add(DELTA_HOUSE1_PATH);
        allPaths.add(DELTA_HOUSE2_PATH);

        int loaded = 0;
        for (String p : allPaths) {
            Structure s = getOrLoadStructure(p);
            if (s != null) {
                loaded++;
                if (CRYSTAL_CRATERS.contains(p)) {
                    sortedCraterBlocks.computeIfAbsent(p, key -> {
                        List<BlockState> list = new ArrayList<>();
                        for (Palette palette : s.getPalettes()) {
                            list.addAll(palette.getBlocks());
                        }
                        list.sort(Comparator.comparingInt(BlockState::getY));
                        return list;
                    });
                }
            }
        }
        Bukkit.getLogger().info("[LunarStructurePopulator] Successfully preloaded " + loaded + "/" + allPaths.size() + " NBT structures into memory.");
    }

    public static boolean isSpreadChunk(int chunkX, int chunkZ, int spacing, int separation, int salt) {
        int cellX = Math.floorDiv(chunkX, spacing);
        int cellZ = Math.floorDiv(chunkZ, spacing);

        long seed = (long) cellX * 341873128712L + (long) cellZ * 132897987541L + salt;
        seed = (seed ^ (seed >> 16)) * 0x45d9f3bL;
        seed = (seed ^ (seed >> 16)) * 0x45d9f3bL;
        seed = seed ^ (seed >> 16);

        int maxOffset = spacing - separation;
        if (maxOffset <= 0) maxOffset = 1;

        Random rand = new Random(seed);
        int candidateX = cellX * spacing + rand.nextInt(maxOffset);
        int candidateZ = cellZ * spacing + rand.nextInt(maxOffset);

        return (chunkX == candidateX) && (chunkZ == candidateZ);
    }

    private static long getDeterministicSeed(int chunkX, int chunkZ, int salt) {
        long seed = ((long) chunkX * 3129871L) ^ ((long) chunkZ * 116129781L) ^ salt;
        seed = (seed ^ (seed >> 16)) * 0x45d9f3bL;
        return seed ^ (seed >> 16);
    }

    private static String pickWeighted(List<WeightedStructure> list, Random rand) {
        int total = 0;
        for (WeightedStructure ws : list) total += ws.weight();
        int r = rand.nextInt(total);
        int current = 0;
        for (WeightedStructure ws : list) {
            current += ws.weight();
            if (r < current) return ws.path();
        }
        return list.get(0).path();
    }

    private static boolean isCrystalBiome(LunarBiome biome) {
        return biome == LunarBiome.LUNAR_CRYSTAL_CRATERS
                || biome == LunarBiome.LUNAR_GIANT_CRYSTALS
                || biome == LunarBiome.LUNAR_GIANT_CRYSTAL_OUTSKIRTS;
    }

    private static boolean isCrystalMaterial(Material m) {
        if (m == null) return false;
        return m == Material.AMETHYST_BLOCK
                || m == Material.BUDDING_AMETHYST
                || m == Material.CALCITE
                || m == Material.SMOOTH_QUARTZ
                || m == Material.TINTED_GLASS
                || m == Material.AMETHYST_CLUSTER
                || m.name().endsWith("_STAINED_GLASS");
    }

    private static boolean isGroundCrystal(String path) {
        if (path == null) return false;
        return path.contains("giant_crystal_field")
                || path.contains("giant_crystal_outskirts")
                || path.contains("crystal_pillar");
    }

    private static StructureRotation getBukkitRotation(int rot) {
        return switch (rot) {
            case 1 -> StructureRotation.CLOCKWISE_90;
            case 2 -> StructureRotation.CLOCKWISE_180;
            case 3 -> StructureRotation.COUNTERCLOCKWISE_90;
            default -> StructureRotation.NONE;
        };
    }
}
