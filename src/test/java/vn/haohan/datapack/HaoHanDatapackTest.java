package vn.haohan.datapack;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HaoHanDatapackTest {

    @Test
    void testLunarWorldKey() {
        assertEquals("haohan:lunar", HaoHanDatapackPlugin.LUNAR_WORLD_KEY.toString());
    }
}
