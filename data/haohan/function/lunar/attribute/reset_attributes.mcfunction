attribute @s minecraft:gravity base reset
attribute @s minecraft:safe_fall_distance base reset
execute if entity @s[type=minecraft:player] run attribute @s minecraft:block_break_speed base reset
attribute @s minecraft:attack_knockback base reset
attribute @s minecraft:fall_damage_multiplier base reset
tag @s remove hh_lunar_physic
