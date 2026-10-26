package vn.haohan.worldgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HaoHanWorldGenTest {

    @Test
    void testLunarWorldKey() {
        assertEquals("haohan:lunar", HaoHanWorldGenPlugin.LUNAR_WORLD_KEY.toString());
    }

    @Test
    void testWorldEnvironmentNormal() {
        org.bukkit.WorldCreator creator = org.bukkit.WorldCreator.ofKey(HaoHanWorldGenPlugin.LUNAR_WORLD_KEY);
        creator.environment(org.bukkit.World.Environment.NORMAL);
        assertEquals(org.bukkit.World.Environment.NORMAL, creator.environment());
    }
}
