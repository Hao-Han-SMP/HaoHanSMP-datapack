# ═══════════════════════════════════════════════════════════════
# Tank Charging Logic – handles charging oxygen tanks when held in safe structures
# Small (Tier 1): 5s (100 ticks)
# Medium (Tier 2): 10s (200 ticks)
# Large (Tier 3): 16s (320 ticks)
# Shows live percentage charged on actionbar
# ═══════════════════════════════════════════════════════════════

# Reset states if not holding a damaged oxygen tank
scoreboard players set @s hh_o2tank_damage 0
scoreboard players set @s hh_o2tank_tier_new 0
execute if data entity @s SelectedItem.components."minecraft:custom_data".haohan.oxygen_tank run execute store result score @s hh_o2tank_damage run data get entity @s SelectedItem.components."minecraft:damage"
execute if data entity @s SelectedItem.components."minecraft:custom_data".haohan.oxygen_tank run execute store result score @s hh_o2tank_tier_new run data get entity @s SelectedItem.components."minecraft:custom_data".haohan.oxygen_tank_tier

# Reset charge timer if not holding a damaged tank
execute unless score @s hh_o2tank_damage matches 1.. run scoreboard players set @s hh_o2tank_charge 0
execute unless score @s hh_o2tank_damage matches 1.. run return 0

# Increment charge progress tick counter
scoreboard players add @s hh_o2tank_charge 1

# ── 1. Calculate current percentage based on initial item damage ──
# start_capacity = max_capacity - initial_damage
scoreboard players set @s hh_o2tank_cur_pct 0
execute if score @s hh_o2tank_tier_new matches 1 run scoreboard players set @s hh_o2tank_cur_pct 1000
execute if score @s hh_o2tank_tier_new matches 2 run scoreboard players set @s hh_o2tank_cur_pct 2000
execute if score @s hh_o2tank_tier_new matches 3 run scoreboard players set @s hh_o2tank_cur_pct 4000
scoreboard players operation @s hh_o2tank_cur_pct -= @s hh_o2tank_damage
# Convert to base percentage: (start_capacity * 100) / max_capacity
scoreboard players operation @s hh_o2tank_cur_pct *= #100 hh_const
execute if score @s hh_o2tank_tier_new matches 1 run scoreboard players operation @s hh_o2tank_cur_pct /= #1000 hh_const
execute if score @s hh_o2tank_tier_new matches 2 run scoreboard players operation @s hh_o2tank_cur_pct /= #2000 hh_const
execute if score @s hh_o2tank_tier_new matches 3 run scoreboard players operation @s hh_o2tank_cur_pct /= #4000 hh_const

# ── 2. Add added charge percentage (charge_ticks / max_ticks * remaining_pct) ──
# Calculate progress % (charge_ticks * 100 / max_ticks)
scoreboard players operation @s hh_o2tank_pct = @s hh_o2tank_charge
scoreboard players operation @s hh_o2tank_pct *= #100 hh_const
execute if score @s hh_o2tank_tier_new matches 1 run scoreboard players operation @s hh_o2tank_pct /= #100 hh_const
execute if score @s hh_o2tank_tier_new matches 2 run scoreboard players operation @s hh_o2tank_pct /= #200 hh_const
execute if score @s hh_o2tank_tier_new matches 3 run scoreboard players operation @s hh_o2tank_pct /= #320 hh_const

# Convert progress % to remaining capacity percentage gained: added_pct = progress_pct * (100 - base_pct) / 100
scoreboard players set #temp hh_const 100
scoreboard players operation #temp hh_const -= @s hh_o2tank_cur_pct
scoreboard players operation @s hh_o2tank_pct *= #temp hh_const
scoreboard players operation @s hh_o2tank_pct /= #100 hh_const

# Total current percentage = base_pct + added_pct
scoreboard players operation @s hh_o2tank_cur_pct += @s hh_o2tank_pct
execute if score @s hh_o2tank_cur_pct matches 100.. run scoreboard players set @s hh_o2tank_cur_pct 100

# ── 3. Small Tank (Tier 1, 5s = 100 ticks) ──
execute if score @s hh_o2tank_tier_new matches 1 run title @s actionbar [{"text":"⚡ Đang nạp bình oxy nhỏ... ","color":"yellow"},{"score":{"name":"@s","objective":"hh_o2tank_cur_pct"},"color":"green"},{"text":"%","color":"green"}]
execute if score @s hh_o2tank_tier_new matches 1 if score @s hh_o2tank_charge matches 100.. run item modify entity @s weapon.mainhand haohan:repair_tank
execute if score @s hh_o2tank_tier_new matches 1 if score @s hh_o2tank_charge matches 100.. run title @s subtitle [{"text":"🔋 Bình oxy đã được nạp xong!","color":"green"}]
execute if score @s hh_o2tank_tier_new matches 1 if score @s hh_o2tank_charge matches 100.. run title @s title {"text":""}
execute if score @s hh_o2tank_tier_new matches 1 if score @s hh_o2tank_charge matches 100.. at @s run playsound minecraft:block.beacon.power_select master @s ~ ~ ~ 2 1.73
execute if score @s hh_o2tank_tier_new matches 1 if score @s hh_o2tank_charge matches 100.. run scoreboard players set @s hh_o2tank_charge 0

# ── 4. Medium Tank (Tier 2, 10s = 200 ticks) ──
execute if score @s hh_o2tank_tier_new matches 2 run title @s actionbar [{"text":"⚡ Đang nạp bình oxy vừa... ","color":"yellow"},{"score":{"name":"@s","objective":"hh_o2tank_cur_pct"},"color":"green"},{"text":"%","color":"green"}]
execute if score @s hh_o2tank_tier_new matches 2 if score @s hh_o2tank_charge matches 200.. run item modify entity @s weapon.mainhand haohan:repair_tank
execute if score @s hh_o2tank_tier_new matches 2 if score @s hh_o2tank_charge matches 200.. run title @s subtitle [{"text":"🔋 Bình oxy đã được nạp xong!","color":"green"}]
execute if score @s hh_o2tank_tier_new matches 2 if score @s hh_o2tank_charge matches 200.. run title @s title {"text":""}
execute if score @s hh_o2tank_tier_new matches 2 if score @s hh_o2tank_charge matches 200.. at @s run playsound minecraft:block.beacon.power_select master @s ~ ~ ~ 2 1.73
execute if score @s hh_o2tank_tier_new matches 2 if score @s hh_o2tank_charge matches 200.. run scoreboard players set @s hh_o2tank_charge 0

# ── 5. Large Tank (Tier 3, 16s = 320 ticks) ──
execute if score @s hh_o2tank_tier_new matches 3 run title @s actionbar [{"text":"⚡ Đang nạp bình oxy lớn... ","color":"yellow"},{"score":{"name":"@s","objective":"hh_o2tank_cur_pct"},"color":"green"},{"text":"%","color":"green"}]
execute if score @s hh_o2tank_tier_new matches 3 if score @s hh_o2tank_charge matches 320.. run item modify entity @s weapon.mainhand haohan:repair_tank
execute if score @s hh_o2tank_tier_new matches 3 if score @s hh_o2tank_charge matches 320.. run title @s subtitle [{"text":"🔋 Bình oxy đã được nạp xong!","color":"green"}]
execute if score @s hh_o2tank_tier_new matches 3 if score @s hh_o2tank_charge matches 320.. run title @s title {"text":""}
execute if score @s hh_o2tank_tier_new matches 3 if score @s hh_o2tank_charge matches 320.. at @s run playsound minecraft:block.beacon.power_select master @s ~ ~ ~ 2 1.73
execute if score @s hh_o2tank_tier_new matches 3 if score @s hh_o2tank_charge matches 320.. run scoreboard players set @s hh_o2tank_charge 0
