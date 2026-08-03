# ═══════════════════════════════════════════════════════════════
# Oxygen Tank Drain – supplies oxygen from active tank to base oxygen
# Runs every tick when hh_o2tank >= 1
# ═══════════════════════════════════════════════════════════════

# 1. Consume 1 oxygen from the active tank for breathing every tick
scoreboard players remove @s hh_o2tank 1

# 2. Refill base oxygen to 600 if it falls below 600
# Calculate needed oxygen: hh_o2tank_new = 600 - hh_oxygen
scoreboard players set @s hh_o2tank_new 600
scoreboard players operation @s hh_o2tank_new -= @s hh_oxygen

# Transfer from tank to base oxygen if player is not fully saturated
execute if score @s hh_o2tank_new matches 1.. run function haohan:lunar/item/oxygen_tank/refill_transfer

# 3. Reset suffocation damage counter while tank is active
scoreboard players set @s hh_oxygen_dmg 0
