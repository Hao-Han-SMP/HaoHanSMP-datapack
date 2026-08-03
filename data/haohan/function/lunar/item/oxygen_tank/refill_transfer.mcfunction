# ═══════════════════════════════════════════════════════════════
# Refill Transfer – transfers calculated oxygen from tank to player
# Called from drain.mcfunction when hh_oxygen < 600
# ═══════════════════════════════════════════════════════════════

# Limit transfer to max 10 per tick
execute if score @s hh_o2tank_new matches 11.. run scoreboard players set @s hh_o2tank_new 10

# Limit transfer to what is available in the tank
# If hh_o2tank_new > hh_o2tank, set hh_o2tank_new = hh_o2tank
execute if score @s hh_o2tank_new > @s hh_o2tank run scoreboard players operation @s hh_o2tank_new = @s hh_o2tank

# Transfer the calculated amount (hh_o2tank_new) from tank to base oxygen
scoreboard players operation @s hh_oxygen += @s hh_o2tank_new
scoreboard players operation @s hh_o2tank -= @s hh_o2tank_new
