execute as @e at @s if dimension haohan:lunar run attribute @s minecraft:gravity base set 0.013256
execute as @e at @s if dimension haohan:lunar run attribute @s minecraft:safe_fall_distance base set 18
execute as @e at @s if dimension haohan:lunar run attribute @s minecraft:block_break_speed base set 0.8
execute as @e at @s if dimension haohan:lunar run attribute @s minecraft:attack_knockback base set 0.75

execute as @a at @s if dimension haohan:lunar run particle minecraft:firefly ~ ~1 ~ 10 3 10 0.01 3 force
execute as @a at @s if dimension haohan:lunar run particle minecraft:glow ~ ~1 ~ 8 2.5 8 0.03 1 force

execute as @e at @s unless dimension haohan:lunar run attribute @s minecraft:gravity base reset
execute as @e at @s unless dimension haohan:lunar run attribute @s minecraft:safe_fall_distance base reset
execute as @e at @s unless dimension haohan:lunar run attribute @s minecraft:block_break_speed base reset
execute as @e at @s unless dimension haohan:lunar run attribute @s minecraft:attack_knockback base reset