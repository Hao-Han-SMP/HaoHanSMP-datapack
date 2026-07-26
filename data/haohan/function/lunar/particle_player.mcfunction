# Terrae: firefly effect
execute if biome ~ ~ ~ haohan:lunar_terrae run particle minecraft:firefly ~ ~1 ~ 10 3 10 0.01 4 force @s

# Maria: black firework effect (using black dust to represent black sparks)
execute if biome ~ ~ ~ haohan:lunar_maria run particle minecraft:dust{color:[0.0,0.0,0.0],scale:1.5} ~ ~1 ~ 8 2.5 8 0.01 4 force @s

# Crater: glow effect
execute if biome ~ ~ ~ haohan:lunar_craters run particle minecraft:glow ~ ~1 ~ 8 2.5 8 0.03 2 force @s

# Crystal biomes: Purple and White dust particles (with blue/white for outskirts)
# 1. Lunar Crystal Craters (Amethyst theme - purple and white)
execute if biome ~ ~ ~ haohan:lunar_crystal_craters run particle minecraft:dust{color:[0.72,0.45,0.96],scale:1.5} ~ ~1 ~ 8 2.5 8 0.01 3 force @s
execute if biome ~ ~ ~ haohan:lunar_crystal_craters run particle minecraft:dust{color:[1.00,1.00,1.00],scale:1.5} ~ ~1 ~ 8 2.5 8 0.01 1 force @s

# 2. Lunar Giant Crystals (Giant crystal spires - purple, magenta, and white)
execute if biome ~ ~ ~ haohan:lunar_giant_crystals run particle minecraft:dust{color:[0.70,0.20,0.90],scale:1.5} ~ ~1 ~ 8 2.5 8 0.01 2 force @s
execute if biome ~ ~ ~ haohan:lunar_giant_crystals run particle minecraft:dust{color:[0.95,0.26,0.73],scale:1.5} ~ ~1 ~ 8 2.5 8 0.01 1 force @s
execute if biome ~ ~ ~ haohan:lunar_giant_crystals run particle minecraft:dust{color:[1.00,1.00,1.00],scale:1.5} ~ ~1 ~ 8 2.5 8 0.01 1 force @s

# 3. Lunar Giant Crystal Outskirts (Outskirts - light blue/cyan and white)
execute if biome ~ ~ ~ haohan:lunar_giant_crystal_outskirts run particle minecraft:dust{color:[0.40,0.75,1.00],scale:1.5} ~ ~1 ~ 8 2.5 8 0.01 2 force @s
execute if biome ~ ~ ~ haohan:lunar_giant_crystal_outskirts run particle minecraft:dust{color:[0.13,0.75,0.75],scale:1.5} ~ ~1 ~ 8 2.5 8 0.01 1 force @s
execute if biome ~ ~ ~ haohan:lunar_giant_crystal_outskirts run particle minecraft:dust{color:[1.00,1.00,1.00],scale:1.5} ~ ~1 ~ 8 2.5 8 0.01 1 force @s
