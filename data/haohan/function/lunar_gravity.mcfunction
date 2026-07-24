execute as @a at @s if dimension haohan:lunar run attribute @s minecraft:gravity base set 0.013256
execute as @a at @s if dimension haohan:lunar run attribute @s minecraft:safe_fall_distance base set 18
execute as @a at @s unless dimension haohan:lunar run attribute @s minecraft:gravity base reset
execute as @a at @s unless dimension haohan:lunar run attribute @s minecraft:safe_fall_distance base reset