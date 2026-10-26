package vn.haohan.worldgen.populator;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import vn.haohan.worldgen.LunarBiome;
import vn.haohan.worldgen.LunarBiomeProvider;
import vn.haohan.worldgen.config.LunarWorldConfig;

import java.util.Random;
import java.util.Set;

/**
 * Ore Populator for the Lunar dimension.
 * Faithfully implements the 4 custom lunar ores as defined in the datapack:
 * - Kreep Basalt (note 21)
 * - Pyroxene Ore (note 22)
 * - Ilmenite Ore (note 23)
 * - Anorthosite Ore (note 24)
 * (All structures are handled by LunarStructurePopulator).
 */
public final class LunarPopulator extends BlockPopulator {

    private final LunarWorldConfig config;
    private final LunarBiomeProvider biomeProvider;

    private static final Set<Material> KREEP_TARGETS = Set.of(Material.BLACKSTONE, Material.OBSIDIAN, Material.DEEPSLATE);
    private static final Set<Material> PYROXENE_TARGETS = Set.of(Material.TUFF, Material.ANDESITE, Material.STONE, Material.DIORITE);
    private static final Set<Material> ILMENITE_TARGETS = Set.of(Material.TUFF, Material.DEEPSLATE, Material.COBBLED_DEEPSLATE);
    private static final Set<Material> ANORTHOSITE_TARGETS = Set.of(Material.DEEPSLATE, Material.TUFF, Material.SMOOTH_QUARTZ);

    private static volatile BlockData kreepBasaltData;
    private static volatile BlockData pyroxeneOreData;
    private static volatile BlockData ilmeniteOreData;
    private static volatile BlockData anorthositeOreData;

    public LunarPopulator(LunarWorldConfig config, LunarBiomeProvider biomeProvider) {
        this.config = config;
        this.biomeProvider = biomeProvider;
    }

    private void ensureOreDataInitialized() {
        if (kreepBasaltData != null || Bukkit.getServer() == null) return;
        try {
            kreepBasaltData = createOreBlockData(21);
            pyroxeneOreData = createOreBlockData(22);
            ilmeniteOreData = createOreBlockData(23);
            anorthositeOreData = createOreBlockData(24);
        } catch (Exception ignored) {
        }
    }

    private static BlockData createOreBlockData(int notePitch) {
        NoteBlock nb = (NoteBlock) Material.NOTE_BLOCK.createBlockData();
        nb.setInstrument(Instrument.PLING);
        nb.setNote(new Note(notePitch));
        nb.setPowered(true);
        return nb;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        ensureOreDataInitialized();

        int originX = chunkX << 4;
        int originZ = chunkZ << 4;
        LunarBiome biome = biomeProvider.getLunarBiome(originX + 8, originZ + 8);

        // 1. Kreep Basalt (note 21): Maria, Giant Crystals, Giant Crystal Outskirts (Y: 5 to 60, size: 4, count: 3)
        if (config.isKreepBasaltEnabled() && isKreepBiome(biome)) {
            generateOreBlob(limitedRegion, random, originX, originZ, 3, 4, 5, 60, kreepBasaltData, KREEP_TARGETS);
        }

        // 2. Pyroxene Ore (note 22): Maria, Terrae, Crystal Craters (Y: 10 to 130, size: 7, count: 6)
        if (config.isPyroxeneEnabled() && isPyroxeneBiome(biome)) {
            generateOreBlob(limitedRegion, random, originX, originZ, 6, 7, 10, 130, pyroxeneOreData, PYROXENE_TARGETS);
        }

        // 3. Ilmenite Ore (note 23): Maria, Craters (Y: 5 to 110, size: 9, count: 8)
        if (config.isIlmeniteEnabled() && isIlmeniteBiome(biome)) {
            generateOreBlob(limitedRegion, random, originX, originZ, 8, 9, 5, 110, ilmeniteOreData, ILMENITE_TARGETS);
        }

        // 4. Anorthosite Ore (note 24): Terrae, Craters, Bright Patches (Y: 50 to 170, size: 5, count: 4)
        if (config.isAnorthositeEnabled() && isAnorthositeBiome(biome)) {
            generateOreBlob(limitedRegion, random, originX, originZ, 4, 5, 50, 170, anorthositeOreData, ANORTHOSITE_TARGETS);
        }
    }

    private void generateOreBlob(LimitedRegion region, Random random, int originX, int originZ,
                                 int count, int size, int minY, int maxY,
                                 BlockData oreBlockData, Set<Material> targetMaterials) {
        if (oreBlockData == null) return;

        int radius = Math.max(1, (int) Math.round(Math.cbrt(size)));
        int rSq = radius * radius;

        for (int i = 0; i < count; i++) {
            int cx = originX + random.nextInt(16);
            int cz = originZ + random.nextInt(16);
            int cy = minY + random.nextInt(Math.max(1, maxY - minY));

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx * dx + dy * dy + dz * dz <= rSq) {
                            int tx = cx + dx;
                            int ty = cy + dy;
                            int tz = cz + dz;
                            if (region.isInRegion(tx, ty, tz)) {
                                Material current = region.getType(tx, ty, tz);
                                if (targetMaterials.contains(current) && random.nextInt(100) < 75) {
                                    region.setBlockData(tx, ty, tz, oreBlockData);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isKreepBiome(LunarBiome biome) {
        return biome == LunarBiome.LUNAR_MARIA
                || biome == LunarBiome.LUNAR_GIANT_CRYSTALS
                || biome == LunarBiome.LUNAR_GIANT_CRYSTAL_OUTSKIRTS;
    }

    private static boolean isPyroxeneBiome(LunarBiome biome) {
        return biome == LunarBiome.LUNAR_MARIA
                || biome == LunarBiome.LUNAR_TERRAE
                || biome == LunarBiome.LUNAR_CRYSTAL_CRATERS;
    }

    private static boolean isIlmeniteBiome(LunarBiome biome) {
        return biome == LunarBiome.LUNAR_MARIA
                || biome == LunarBiome.LUNAR_CRATERS;
    }

    private static boolean isAnorthositeBiome(LunarBiome biome) {
        return biome == LunarBiome.LUNAR_TERRAE
                || biome == LunarBiome.LUNAR_CRATERS
                || biome == LunarBiome.LUNAR_BRIGHT_PATCHES;
    }
}
