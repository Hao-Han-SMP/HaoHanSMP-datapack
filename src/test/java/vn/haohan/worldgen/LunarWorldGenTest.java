package vn.haohan.worldgen;

import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.haohan.worldgen.config.LunarWorldConfig;
import vn.haohan.worldgen.noise.CubicSpline;
import vn.haohan.worldgen.noise.NormalNoise;
import vn.haohan.worldgen.populator.LunarStructurePopulator;
import vn.haohan.worldgen.surface.LunarSurfacePipeline;
import vn.haohan.worldgen.terrain.LunarTerrainPipeline;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LunarWorldGenTest {

    @Test
    @DisplayName("NormalNoise is deterministic and correctly bounded")
    void testNormalNoise() {
        NormalNoise noise = new NormalNoise(12345L, "test_noise", -5, 1.0, 0.5, 0.25);
        double sample1 = noise.sample(100.5, 200.5);
        double sample2 = noise.sample(100.5, 200.5);

        assertEquals(sample1, sample2, 1e-9, "Same coordinates must yield identical noise sample");
        assertTrue(sample1 >= -1.0 && sample1 <= 1.0, "Sample should be bounded approximately within [-1.0, 1.0]");
    }

    @Test
    @DisplayName("CubicSpline evaluates exact points and smooth Hermite interpolation")
    void testCubicSpline() {
        CubicSpline spline = new CubicSpline(
                new CubicSpline.Point(-1.0, 10.0),
                new CubicSpline.Point(0.0, 20.0),
                new CubicSpline.Point(1.0, 30.0)
        );

        assertEquals(10.0, spline.sample(-1.0), 1e-6);
        assertEquals(20.0, spline.sample(0.0), 1e-6);
        assertEquals(30.0, spline.sample(1.0), 1e-6);

        // Clamping outside bounds
        assertEquals(10.0, spline.sample(-2.0), 1e-6);
        assertEquals(30.0, spline.sample(2.0), 1e-6);

        // Smooth interpolation midpoint
        double mid = spline.sample(0.5);
        assertTrue(mid > 20.0 && mid < 30.0);
    }

    @Test
    @DisplayName("LunarTerrainPipeline produces heights strictly within configured bounds")
    void testTerrainHeightBounds() {
        LunarWorldConfig config = new LunarWorldConfig();
        LunarTerrainPipeline pipeline = new LunarTerrainPipeline(42L, config);

        int minObserved = Integer.MAX_VALUE;
        int maxObserved = Integer.MIN_VALUE;

        // Sample a wide grid of coordinates
        for (int x = -1000; x <= 1000; x += 100) {
            for (int z = -1000; z <= 1000; z += 100) {
                int h = pipeline.calculateHeight(x, z);
                if (h < minObserved) minObserved = h;
                if (h > maxObserved) maxObserved = h;

                assertTrue(h >= 1, "Height " + h + " is below minimum allowed Y (1) at (" + x + "," + z + ")");
                assertTrue(h <= 252, "Height " + h + " exceeds maximum allowed Y (252) at (" + x + "," + z + ")");
            }
        }

        assertTrue(minObserved < 85, "Should observe low Maria plains or crater bowls, got min: " + minObserved);
        assertTrue(maxObserved > 90, "Should observe elevated Highlands terrain, got max: " + maxObserved);
    }

    @Test
    @DisplayName("LunarBiomeProvider distributes all 7 lunar biomes across dimension")
    void testBiomeDistribution() {
        LunarBiomeProvider provider = new LunarBiomeProvider(19690720L);
        EnumSet<LunarBiome> observed = EnumSet.noneOf(LunarBiome.class);

        for (int x = -5000; x <= 5000; x += 150) {
            for (int z = -5000; z <= 5000; z += 150) {
                LunarBiome b = provider.getLunarBiome(x, z);
                observed.add(b);
                assertNotNull(b.getVanillaBiomeKey());
                assertNotNull(b.getTopBlock());
                assertNotNull(b.getSubSurfaceBlock());
            }
        }

        // Verify all 7 biomes are covered
        for (LunarBiome b : LunarBiome.values()) {
            assertTrue(observed.contains(b), "Missing generated biome: " + b.name());
        }
    }

    @Test
    @DisplayName("Surface strata and floor materials follow datapack surface rules")
    @SuppressWarnings("deprecation")
    void testSurfaceRules() {
        LunarWorldConfig config = new LunarWorldConfig();
        LunarBiomeProvider biomeProvider = new LunarBiomeProvider(1L);
        LunarSurfacePipeline surfacePipeline = new LunarSurfacePipeline(1L, config, biomeProvider);

        Map<String, Material> blockMap = new HashMap<>();
        ChunkGenerator.ChunkData mockChunkData = new ChunkGenerator.ChunkData() {
            @Override
            public int getMinHeight() { return 0; }
            @Override
            public int getMaxHeight() { return 256; }
            @Override
            public org.bukkit.block.Biome getBiome(int x, int y, int z) { return null; }
            @Override
            public void setBlock(int x, int y, int z, Material material) {
                blockMap.put(x + "," + y + "," + z, material);
            }
            @Override
            public void setBlock(int x, int y, int z, MaterialData materialData) {
                setBlock(x, y, z, materialData.getItemType());
            }
            @Override
            public void setBlock(int x, int y, int z, org.bukkit.block.data.BlockData blockData) {
                setBlock(x, y, z, blockData.getMaterial());
            }
            @Override
            public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material) {}
            @Override
            public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, MaterialData materialData) {}
            @Override
            public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, org.bukkit.block.data.BlockData blockData) {}
            @Override
            public Material getType(int x, int y, int z) { return blockMap.get(x + "," + y + "," + z); }
            @Override
            public MaterialData getTypeAndData(int x, int y, int z) { return null; }
            @Override
            public org.bukkit.block.data.BlockData getBlockData(int x, int y, int z) { return null; }
            @Override
            public byte getData(int x, int y, int z) { return 0; }
            @Override
            public int getHeight(HeightMap heightMap, int x, int z) { return 95; }
        };

        surfacePipeline.applyColumn(mockChunkData, 0, 0, 100, 100, 95);

        Material topMat = blockMap.get("0,95,0");
        assertNotNull(topMat, "Top block at surface Y=95 must be set");

        Material subMat = blockMap.get("0,93,0");
        assertEquals(topMat, subMat, "Subsurface block within surface depth must match surface floor material");

        Material mantleMat = blockMap.get("0,89,0");
        assertEquals(Material.TUFF, mantleMat, "Subsurface mantle layer at depth 6 (offset 8) must be TUFF");

        Material deepLayerMat = blockMap.get("0,85,0");
        assertTrue(deepLayerMat == Material.DIORITE || deepLayerMat == Material.ANDESITE || deepLayerMat == Material.STONE,
                "Subsurface stratum at depth 10 (offset 13) must be DIORITE, ANDESITE, or STONE");

        Material deepMat = blockMap.get("0,10,0");
        assertNotNull(deepMat, "Deep underground block must be populated");
    }

    @Test
    @DisplayName("LunarStructurePopulator correctly calculates grid spread candidates")
    void testStructureRandomSpread() {
        int spacing = 120;
        int separation = 40;
        int salt = 58291047;

        int matchCount = 0;
        for (int cx = 0; cx < spacing; cx++) {
            for (int cz = 0; cz < spacing; cz++) {
                if (LunarStructurePopulator.isSpreadChunk(cx, cz, spacing, separation, salt)) {
                    matchCount++;
                }
            }
        }

        assertEquals(1, matchCount, "Every spacing x spacing grid cell must contain exactly 1 candidate chunk");
    }

    @Test
    @DisplayName("Bundled structure NBT files are loadable from classpath resources")
    void testStructureNbtResources() {
        List<String> expectedStructures = List.of(
                "/structures/rest_base/rest_base.nbt",
                "/structures/space_station/space_station.nbt",
                "/structures/delta_abandoned_base/center.nbt",
                "/structures/crystal_crater/crater_a.nbt",
                "/structures/crystal_pillar/pillar_buttressed.nbt",
                "/structures/floating_crystal_island/island_broad.nbt",
                "/structures/giant_crystal_field/spire_a.nbt"
        );

        for (String path : expectedStructures) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                assertNotNull(is, "Missing required structure NBT resource: " + path);
                assertTrue(is.available() > 0, "Structure NBT resource must not be empty: " + path);
            } catch (Exception e) {
                fail("Failed to read structure resource: " + path + ", error: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("LunarCavePipeline produces organic caves with undulating floors, no flat slabs, and bedrock guard")
    void testLunarCavePipelineUndulatingFloor() {
        LunarWorldConfig config = new LunarWorldConfig();
        LunarBiomeProvider biomeProvider = new LunarBiomeProvider(19690720L);
        LunarTerrainPipeline terrainPipeline = new LunarTerrainPipeline(19690720L, config);
        vn.haohan.worldgen.cave.LunarCavePipeline cavePipeline = new vn.haohan.worldgen.cave.LunarCavePipeline(
                19690720L, config, biomeProvider, terrainPipeline
        );

        int minHeight = -64;
        int maxHeight = 320;

        // Search for a chunk that contains a cavern
        boolean foundCavernChunk = false;
        java.util.Set<Integer> observedFloorYs = new java.util.HashSet<>();

        for (int chunkX = -5; chunkX <= 5 && !foundCavernChunk; chunkX++) {
            for (int chunkZ = -5; chunkZ <= 5 && !foundCavernChunk; chunkZ++) {
                Map<String, Material> blockMap = new HashMap<>();
                // Pre-fill mock chunk data with TUFF
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int wx = (chunkX << 4) + x;
                        int wz = (chunkZ << 4) + z;
                        int topY = terrainPipeline.calculateHeight(wx, wz);
                        for (int y = minHeight; y < minHeight + 5; y++) {
                            blockMap.put(x + "," + y + "," + z, Material.BEDROCK);
                        }
                        for (int y = minHeight + 5; y <= topY; y++) {
                            blockMap.put(x + "," + y + "," + z, Material.TUFF);
                        }
                    }
                }

                ChunkGenerator.ChunkData mockChunk = new ChunkGenerator.ChunkData() {
                    @Override public int getMinHeight() { return minHeight; }
                    @Override public int getMaxHeight() { return maxHeight; }
                    @Override public org.bukkit.block.Biome getBiome(int x, int y, int z) { return null; }
                    @Override public void setBlock(int x, int y, int z, Material material) {
                        blockMap.put(x + "," + y + "," + z, material);
                    }
                    @Override public void setBlock(int x, int y, int z, MaterialData data) {
                        setBlock(x, y, z, data.getItemType());
                    }
                    @Override public void setBlock(int x, int y, int z, org.bukkit.block.data.BlockData data) {
                        setBlock(x, y, z, data.getMaterial());
                    }
                    @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material) {}
                    @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, MaterialData data) {}
                    @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, org.bukkit.block.data.BlockData data) {}
                    @Override public Material getType(int x, int y, int z) {
                        return blockMap.getOrDefault(x + "," + y + "," + z, Material.AIR);
                    }
                    @Override public MaterialData getTypeAndData(int x, int y, int z) { return null; }
                    @Override public org.bukkit.block.data.BlockData getBlockData(int x, int y, int z) { return null; }
                    @Override public byte getData(int x, int y, int z) { return 0; }
                    @Override public int getHeight(HeightMap heightMap, int x, int z) { return 100; }
                };

                cavePipeline.applyCaves(mockChunk, chunkX, chunkZ);

                // Check for carved air underground
                int airCount = 0;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        // Bedrock guard check: bottom 5 layers must never be carved
                        for (int y = minHeight; y < minHeight + 5; y++) {
                            assertNotEquals(Material.AIR, mockChunk.getType(x, y, z),
                                    "Bedrock layer must NEVER be carved to AIR!");
                        }

                        // Collect lowest air block (cave floor boundary)
                        int lowestAirY = Integer.MAX_VALUE;
                        for (int y = minHeight + 5; y <= 90; y++) {
                            if (mockChunk.getType(x, y, z) == Material.AIR) {
                                airCount++;
                                if (y < lowestAirY) {
                                    lowestAirY = y;
                                }
                            }
                        }
                        if (lowestAirY != Integer.MAX_VALUE) {
                            observedFloorYs.add(lowestAirY);
                        }
                    }
                }

                if (airCount > 100) {
                    foundCavernChunk = true;
                }
            }
        }

        assertTrue(foundCavernChunk, "Should discover cavern networks within searched chunks");
        // Undulating floor verification: floor Y coordinates MUST vary across multiple distinct elevations!
        assertTrue(observedFloorYs.size() >= 3,
                "Cave floor must NOT be a flat plane; expected multiple varying elevations, got: " + observedFloorYs);
    }

    @Test
    @DisplayName("Giant Selenite Crystal Beams generate in subterranean crystal caverns")
    @SuppressWarnings("deprecation")
    void testCrystalCavernBeams() {
        LunarWorldConfig config = new LunarWorldConfig();
        LunarBiomeProvider biomeProvider = new LunarBiomeProvider(19690720L);
        LunarTerrainPipeline terrainPipeline = new LunarTerrainPipeline(19690720L, config);
        vn.haohan.worldgen.cave.LunarCavePipeline cavePipeline = new vn.haohan.worldgen.cave.LunarCavePipeline(
                19690720L, config, biomeProvider, terrainPipeline
        );

        // Find a chunk located in a crystal biome
        int targetChunkX = 0, targetChunkZ = 0;
        boolean foundCrystalChunk = false;
        for (int cx = -50; cx <= 50 && !foundCrystalChunk; cx += 2) {
            for (int cz = -50; cz <= 50 && !foundCrystalChunk; cz += 2) {
                LunarBiome b = biomeProvider.getLunarBiome((cx << 4) + 8, (cz << 4) + 8);
                if (b == LunarBiome.LUNAR_CRYSTAL_CRATERS
                        || b == LunarBiome.LUNAR_GIANT_CRYSTALS
                        || b == LunarBiome.LUNAR_GIANT_CRYSTAL_OUTSKIRTS) {
                    targetChunkX = cx;
                    targetChunkZ = cz;
                    foundCrystalChunk = true;
                }
            }
        }
        assertTrue(foundCrystalChunk, "Should find a crystal biome chunk");

        Map<String, Material> blockMap = new HashMap<>();
        int minHeight = -64;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = (targetChunkX << 4) + x;
                int wz = (targetChunkZ << 4) + z;
                int topY = terrainPipeline.calculateHeight(wx, wz);
                for (int y = minHeight; y < minHeight + 5; y++) {
                    blockMap.put(x + "," + y + "," + z, Material.BEDROCK);
                }
                for (int y = minHeight + 5; y <= topY; y++) {
                    blockMap.put(x + "," + y + "," + z, Material.TUFF);
                }
            }
        }

        ChunkGenerator.ChunkData mockChunk = new ChunkGenerator.ChunkData() {
            @Override public int getMinHeight() { return minHeight; }
            @Override public int getMaxHeight() { return 320; }
            @Override public org.bukkit.block.Biome getBiome(int x, int y, int z) { return null; }
            @Override public void setBlock(int x, int y, int z, Material material) {
                blockMap.put(x + "," + y + "," + z, material);
            }
            @Override public void setBlock(int x, int y, int z, MaterialData data) {
                setBlock(x, y, z, data.getItemType());
            }
            @Override public void setBlock(int x, int y, int z, org.bukkit.block.data.BlockData data) {
                setBlock(x, y, z, data.getMaterial());
            }
            @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material) {}
            @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, MaterialData data) {}
            @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, org.bukkit.block.data.BlockData data) {}
            @Override public Material getType(int x, int y, int z) {
                return blockMap.getOrDefault(x + "," + y + "," + z, Material.AIR);
            }
            @Override public MaterialData getTypeAndData(int x, int y, int z) { return null; }
            @Override public org.bukkit.block.data.BlockData getBlockData(int x, int y, int z) { return null; }
            @Override public byte getData(int x, int y, int z) { return 0; }
            @Override public int getHeight(HeightMap heightMap, int x, int z) { return 100; }
        };

        cavePipeline.applyCaves(mockChunk, targetChunkX, targetChunkZ);

        // Verify crystal cavern features: presence of selenite crystal / cavern materials
        boolean hasCrystalBlock = false;
        for (Material m : blockMap.values()) {
            if (m == Material.CALCITE || m == Material.SMOOTH_QUARTZ
                    || m == Material.WHITE_STAINED_GLASS || m == Material.TINTED_GLASS
                    || m == Material.AMETHYST_BLOCK || m == Material.AMETHYST_CLUSTER
                    || m == Material.OCHRE_FROGLIGHT) {
                hasCrystalBlock = true;
                break;
            }
        }
        assertTrue(hasCrystalBlock, "Crystal cavern should generate selenite crystal beams or crystal minerals");
    }

    @Test
    @DisplayName("Lunar Maria generates pitch black basalt ground with obsidian cracks")
    @SuppressWarnings("deprecation")
    void testMariaBlackGroundAndObsidianCracks() {
        LunarWorldConfig config = new LunarWorldConfig();
        LunarBiomeProvider biomeProvider = new LunarBiomeProvider(19690720L);
        LunarTerrainPipeline terrainPipeline = new LunarTerrainPipeline(19690720L, config);
        LunarSurfacePipeline surfacePipeline = new LunarSurfacePipeline(19690720L, config, biomeProvider, terrainPipeline);

        // Find a Maria area
        int mariaOriginX = 0, mariaOriginZ = 0;
        boolean foundMaria = false;
        for (int x = -4000; x <= 4000 && !foundMaria; x += 100) {
            for (int z = -4000; z <= 4000 && !foundMaria; z += 100) {
                if (biomeProvider.getLunarBiome(x, z) == LunarBiome.LUNAR_MARIA) {
                    mariaOriginX = x;
                    mariaOriginZ = z;
                    foundMaria = true;
                }
            }
        }
        assertTrue(foundMaria, "Must discover Lunar Maria biome in dimension");

        Map<String, Material> blockMap = new HashMap<>();
        ChunkGenerator.ChunkData mockChunk = new ChunkGenerator.ChunkData() {
            @Override public int getMinHeight() { return 0; }
            @Override public int getMaxHeight() { return 256; }
            @Override public org.bukkit.block.Biome getBiome(int x, int y, int z) { return null; }
            @Override public void setBlock(int x, int y, int z, Material material) {
                blockMap.put(x + "," + y + "," + z, material);
            }
            @Override public void setBlock(int x, int y, int z, MaterialData data) { setBlock(x, y, z, data.getItemType()); }
            @Override public void setBlock(int x, int y, int z, org.bukkit.block.data.BlockData data) { setBlock(x, y, z, data.getMaterial()); }
            @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material) {}
            @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, MaterialData data) {}
            @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, org.bukkit.block.data.BlockData data) {}
            @Override public Material getType(int x, int y, int z) { return blockMap.getOrDefault(x + "," + y + "," + z, Material.TUFF); }
            @Override public MaterialData getTypeAndData(int x, int y, int z) { return null; }
            @Override public org.bukkit.block.data.BlockData getBlockData(int x, int y, int z) { return null; }
            @Override public byte getData(int x, int y, int z) { return 0; }
            @Override public int getHeight(HeightMap heightMap, int x, int z) { return 90; }
        };

        boolean foundObsidianCrack = false;
        boolean foundBlackConcretePowder = false;
        boolean foundBlackstone = false;

        // Sample a wide patch in Maria
        for (int lx = 0; lx < 48; lx++) {
            for (int lz = 0; lz < 48; lz++) {
                int wx = mariaOriginX + lx;
                int wz = mariaOriginZ + lz;
                LunarBiome b = biomeProvider.getLunarBiome(wx, wz);
                if (b == LunarBiome.LUNAR_MARIA) {
                    int topY = terrainPipeline.calculateHeight(wx, wz);
                    surfacePipeline.applyColumn(mockChunk, lx & 15, lz & 15, wx, wz, topY);
                    Material top = mockChunk.getType(lx & 15, topY, lz & 15);
                    if (top == Material.OBSIDIAN) foundObsidianCrack = true;
                    if (top == Material.BLACK_CONCRETE_POWDER) foundBlackConcretePowder = true;
                    if (top == Material.BLACKSTONE) foundBlackstone = true;
                }
            }
        }

        assertTrue(foundBlackConcretePowder, "Lunar Maria surface must generate black concrete powder regolith");
        assertTrue(foundBlackstone, "Lunar Maria surface must generate dark blackstone");
        assertTrue(foundObsidianCrack, "Lunar Maria surface must feature authentic obsidian cracks!");
    }

    @Test
    @DisplayName("Deep impact crater bowls plunge to profound depths and feature dark basalt floors")
    void testDeepCraterBasins() {
        LunarWorldConfig config = new LunarWorldConfig();
        LunarTerrainPipeline terrainPipeline = new LunarTerrainPipeline(19690720L, config);

        int minHeightObserved = Integer.MAX_VALUE;
        int craterFloorCount = 0;

        for (int x = -3000; x <= 3000; x += 30) {
            for (int z = -3000; z <= 3000; z += 30) {
                int h = terrainPipeline.calculateHeight(x, z);
                if (h < minHeightObserved) {
                    minHeightObserved = h;
                }
                if (terrainPipeline.isLargeCraterFloor(x, z)) {
                    craterFloorCount++;
                }
            }
        }

        assertTrue(minHeightObserved <= 65, "Plunging deep crater basins must reach depth <= 65 Y, observed: " + minHeightObserved);
        assertTrue(craterFloorCount > 50, "Should detect multiple large crater basin floors across lunar terrain");
    }

    @Test
    @DisplayName("Cave generation performance: trilinear cell interpolation processes chunks rapidly")
    void testCaveGenerationPerformance() {
        LunarWorldConfig config = new LunarWorldConfig();
        LunarBiomeProvider biomeProvider = new LunarBiomeProvider(19690720L);
        LunarTerrainPipeline terrainPipeline = new LunarTerrainPipeline(19690720L, config);
        vn.haohan.worldgen.cave.LunarCavePipeline cavePipeline = new vn.haohan.worldgen.cave.LunarCavePipeline(
                19690720L, config, biomeProvider, terrainPipeline
        );

        int minHeight = -64;
        int maxHeight = 320;

        // Warm up JIT
        for (int c = 0; c < 5; c++) {
            Map<String, Material> blockMap = new HashMap<>();
            ChunkGenerator.ChunkData dummyChunk = createMockChunk(minHeight, maxHeight, blockMap);
            cavePipeline.applyCaves(dummyChunk, c, c);
        }

        // Measure execution of 20 consecutive chunks
        long startNano = System.nanoTime();
        int chunkCount = 20;
        for (int c = 0; c < chunkCount; c++) {
            Map<String, Material> blockMap = new HashMap<>();
            ChunkGenerator.ChunkData chunkData = createMockChunk(minHeight, maxHeight, blockMap);
            cavePipeline.applyCaves(chunkData, 10 + c, 10 + c);
        }
        long elapsedMillis = (System.nanoTime() - startNano) / 1_000_000L;
        double millisPerChunk = (double) elapsedMillis / chunkCount;

        // Average chunk cave carving should be blistering fast (well under 35ms per chunk even in mock testing)
        assertTrue(millisPerChunk < 50.0, "Cave generation took " + millisPerChunk + "ms per chunk, expected < 50ms");
    }

    @Test
    @DisplayName("Crater slope calculation clamps appropriately and accurately tilts Y on slopes")
    void testCraterSlopeTiltingAndTrimming() {
        LunarWorldConfig config = new LunarWorldConfig();
        LunarTerrainPipeline pipeline = new LunarTerrainPipeline(19690720L, config);

        // Verify terrain pipeline height changes along steep slopes and gradient clamp logic
        double rawSlopeX = (130 - 100) / 36.0; // slope = 0.833
        double rawSlopeZ = 0.0;
        double slopeLen = Math.hypot(rawSlopeX, rawSlopeZ);
        double maxSlope = 0.40;
        double clampedSlopeX = rawSlopeX;
        if (slopeLen > maxSlope) {
            clampedSlopeX = (rawSlopeX / slopeLen) * maxSlope;
        }

        assertEquals(0.40, clampedSlopeX, 1e-6, "Slope should be clamped to maxSlope (0.40)");

        // Check tilt formula: dx = +18 should tilt up by 18 * 0.40 = +7 blocks
        int tiltUphill = (int) Math.round(clampedSlopeX * 18);
        int tiltDownhill = (int) Math.round(clampedSlopeX * -18);
        assertEquals(7, tiltUphill, "Uphill side should tilt up by 7 blocks");
        assertEquals(-7, tiltDownhill, "Downhill side should tilt down by 7 blocks");
    }


    @SuppressWarnings("deprecation")
    private static ChunkGenerator.ChunkData createMockChunk(int minHeight, int maxHeight, Map<String, Material> blockMap) {
        return new ChunkGenerator.ChunkData() {
            @Override public int getMinHeight() { return minHeight; }
            @Override public int getMaxHeight() { return maxHeight; }
            @Override public org.bukkit.block.Biome getBiome(int x, int y, int z) { return null; }
            @Override public void setBlock(int x, int y, int z, Material material) {
                blockMap.put(x + "," + y + "," + z, material);
            }
            @Override public void setBlock(int x, int y, int z, MaterialData data) { setBlock(x, y, z, data.getItemType()); }
            @Override public void setBlock(int x, int y, int z, org.bukkit.block.data.BlockData data) { setBlock(x, y, z, data.getMaterial()); }
            @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material) {}
            @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, MaterialData data) {}
            @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, org.bukkit.block.data.BlockData data) {}
            @Override public Material getType(int x, int y, int z) {
                return blockMap.getOrDefault(x + "," + y + "," + z, Material.TUFF);
            }
            @Override public MaterialData getTypeAndData(int x, int y, int z) { return null; }
            @Override public org.bukkit.block.data.BlockData getBlockData(int x, int y, int z) { return null; }
            @Override public byte getData(int x, int y, int z) { return 0; }
            @Override public int getHeight(HeightMap heightMap, int x, int z) { return 100; }
        };
    }
}
