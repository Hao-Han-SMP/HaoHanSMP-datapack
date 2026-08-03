# ═══════════════════════════════════════════════════════════════
# Oxygen Tank Activation – called when player right-clicks with an oxygen tank
# ═══════════════════════════════════════════════════════════════

# Initialize temporary scores
scoreboard players set @s hh_o2tank_new 0
scoreboard players set @s hh_o2tank_tier_new 0
scoreboard players set @s hh_o2tank_damage 0

# 1. Player must be in dimension haohan:lunar
execute at @s unless dimension haohan:lunar run title @s actionbar [{"text":"⚠ Bình oxy chỉ dùng tại Mặt Trăng!","color":"red"}]
execute at @s unless dimension haohan:lunar run return 0

# 2. Player must NOT be inside safe regen areas (rest_base or space_station)
execute at @s if predicate haohan:in_rest_base run return 0
execute at @s if predicate haohan:in_space_station run return 0

# Retrieve the tank tier directly from the item's custom NBT (resolves NBT order and match issues!)
execute store result score @s hh_o2tank_tier_new run data get entity @s SelectedItem.components."minecraft:custom_data".haohan.oxygen_tank_tier

# If no tank tier was found (or command failed), tier_new remains 0, stop execution
execute if score @s hh_o2tank_tier_new matches 0 run return 0

# Read current damage of the item in mainhand
execute store result score @s hh_o2tank_damage run data get entity @s SelectedItem.components."minecraft:damage"

# Small tank calculation: capacity 1000
execute if score @s hh_o2tank_tier_new matches 1 if score @s hh_o2tank_damage matches ..999 run scoreboard players set @s hh_o2tank_new 1000
execute if score @s hh_o2tank_tier_new matches 1 if score @s hh_o2tank_damage matches ..999 run scoreboard players operation @s hh_o2tank_new -= @s hh_o2tank_damage

# Medium tank calculation: capacity 2000
execute if score @s hh_o2tank_tier_new matches 2 if score @s hh_o2tank_damage matches ..1999 run scoreboard players set @s hh_o2tank_new 2000
execute if score @s hh_o2tank_tier_new matches 2 if score @s hh_o2tank_damage matches ..1999 run scoreboard players operation @s hh_o2tank_new -= @s hh_o2tank_damage

# Large tank calculation: capacity 4000
execute if score @s hh_o2tank_tier_new matches 3 if score @s hh_o2tank_damage matches ..3999 run scoreboard players set @s hh_o2tank_new 4000
execute if score @s hh_o2tank_tier_new matches 3 if score @s hh_o2tank_damage matches ..3999 run scoreboard players operation @s hh_o2tank_new -= @s hh_o2tank_damage

# If tank is already fully empty (damage >= capacity) or invalid, stop execution
execute if score @s hh_o2tank_new matches ..0 run return 0

# Set the mainhand item to empty state (fully damaged)
execute if score @s hh_o2tank_tier_new matches 1 run item modify entity @s weapon.mainhand haohan:set_empty_small
execute if score @s hh_o2tank_tier_new matches 2 run item modify entity @s weapon.mainhand haohan:set_empty_medium
execute if score @s hh_o2tank_tier_new matches 3 run item modify entity @s weapon.mainhand haohan:set_empty_large

# If player already has an active tank, show switch message
execute if entity @s[tag=hh_o2tank_active] run title @s subtitle [{"text":"🔋 Đã chuyển sang bình oxy mới","color":"yellow"}]
execute if entity @s[tag=hh_o2tank_active] run title @s title {"text":""}

# If player does not have an active tank, show activation message
execute unless entity @s[tag=hh_o2tank_active] run title @s subtitle [{"text":"🔋 Bình oxy đã được kích hoạt","color":"green"}]
execute unless entity @s[tag=hh_o2tank_active] run title @s title {"text":""}

# Apply new values
scoreboard players operation @s hh_o2tank = @s hh_o2tank_new
scoreboard players operation @s hh_o2tank_tier = @s hh_o2tank_tier_new
tag @s add hh_o2tank_active

# Play breath sound upon activation at the player's position
execute at @s run playsound minecraft:entity.player.breath master @s ~ ~ ~ 2 0.4
