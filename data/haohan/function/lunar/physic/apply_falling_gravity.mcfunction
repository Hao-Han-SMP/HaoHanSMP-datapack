# Vanilla applies 0.04 gravity to items and falling blocks.
# Add 0.033372 before entity physics so the remaining gravity is 0.006628,
# matching the lunar ratio used by minecraft:gravity (0.013256 / 0.08).
execute store result score @s hh_motion_y run data get entity @s Motion[1] 1000000
scoreboard players add @s hh_motion_y 33372
execute store result entity @s Motion[1] double 0.000001 run scoreboard players get @s hh_motion_y
