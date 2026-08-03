# ═══════════════════════════════════════════════════════════════
# Oxygen Actionbar Display – dispatcher
# Routes to normal display or tank-augmented display
# ═══════════════════════════════════════════════════════════════

# Sound effect when a bubble is lost (exact thresholds: 540, 480, 420, 360, 300, 240, 180, 120, 60, 0)
execute if score @s hh_oxygen matches 540 at @s run playsound minecraft:block.bubble_column.bubble_pop master @s ~ ~ ~ 1.5 1
execute if score @s hh_oxygen matches 480 at @s run playsound minecraft:block.bubble_column.bubble_pop master @s ~ ~ ~ 1.5 1
execute if score @s hh_oxygen matches 420 at @s run playsound minecraft:block.bubble_column.bubble_pop master @s ~ ~ ~ 1.5 1
execute if score @s hh_oxygen matches 360 at @s run playsound minecraft:block.bubble_column.bubble_pop master @s ~ ~ ~ 1.5 1
execute if score @s hh_oxygen matches 300 at @s run playsound minecraft:block.bubble_column.bubble_pop master @s ~ ~ ~ 1.5 1
execute if score @s hh_oxygen matches 240 at @s run playsound minecraft:block.bubble_column.bubble_pop master @s ~ ~ ~ 1.5 1
execute if score @s hh_oxygen matches 180 at @s run playsound minecraft:block.bubble_column.bubble_pop master @s ~ ~ ~ 1.5 1
execute if score @s hh_oxygen matches 120 at @s run playsound minecraft:block.bubble_column.bubble_pop master @s ~ ~ ~ 1.5 1
execute if score @s hh_oxygen matches 60 at @s run playsound minecraft:block.bubble_column.bubble_pop master @s ~ ~ ~ 1.5 1
execute if score @s hh_oxygen matches 1 at @s run playsound minecraft:block.bubble_column.bubble_pop master @s ~ ~ ~ 1.5 1

# Dispatch to appropriate display
execute if score @s hh_o2tank matches 1.. run function haohan:lunar/environment/oxygen_display_tank
execute unless score @s hh_o2tank matches 1.. run function haohan:lunar/environment/oxygen_display_normal
