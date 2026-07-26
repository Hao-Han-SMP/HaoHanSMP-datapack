attribute @s minecraft:gravity base set 0.013256
attribute @s minecraft:safe_fall_distance base set 18
execute if entity @s[type=minecraft:player] run attribute @s minecraft:block_break_speed base set 0.8
attribute @s minecraft:attack_knockback base set 0.75
attribute @s minecraft:fall_damage_multiplier base set 0.2
tag @s add hh_lunar_physic
