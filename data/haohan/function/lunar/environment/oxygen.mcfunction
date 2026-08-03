# ═══════════════════════════════════════════════════════════════
# Oxygen System – runs every tick for players in haohan:lunar
# ═══════════════════════════════════════════════════════════════

# Initialize oxygen for players entering the lunar dimension (600 = 10 bubbles × 60 ticks each = 30 seconds).
execute in haohan:lunar positioned 0 0 0 as @a[distance=0..,tag=!hh_lunar_oxygen] run scoreboard players set @s hh_oxygen 600
execute in haohan:lunar positioned 0 0 0 as @a[distance=0..,tag=!hh_lunar_oxygen] run scoreboard players set @s hh_oxygen_dmg 0
execute in haohan:lunar positioned 0 0 0 as @a[distance=0..,tag=!hh_lunar_oxygen] run tag @s add hh_lunar_oxygen

# Decrease oxygen by 1 each tick (only while oxygen remains AND player does NOT have an active tank AND is not in safe structure).
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen=1..}] unless score @s hh_o2tank matches 1.. unless predicate haohan:in_rest_base unless predicate haohan:in_space_station run scoreboard players remove @s hh_oxygen 1

# Suffocation damage when oxygen is depleted (1 HP every 20 ticks ≈ 1 second).
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen=..0}] run scoreboard players add @s hh_oxygen_dmg 1
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen_dmg=20..}] at @s run damage @s 1 minecraft:drown
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen_dmg=20..}] run scoreboard players set @s hh_oxygen_dmg 0

# Display oxygen bar on actionbar every tick.
execute as @a[tag=hh_lunar_oxygen] run function haohan:lunar/environment/oxygen_display

# Reset oxygen when player leaves the lunar dimension.
execute as @a[tag=hh_lunar_oxygen] at @s unless dimension haohan:lunar run function haohan:lunar/environment/reset_oxygen

# Reset active tank values immediately when player is inside rest base or space station (use base oxygen only)
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_rest_base run scoreboard players set @s hh_o2tank 0
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_rest_base run scoreboard players set @s hh_o2tank_tier 0
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_rest_base run tag @s remove hh_o2tank_active

execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_space_station run scoreboard players set @s hh_o2tank 0
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_space_station run scoreboard players set @s hh_o2tank_tier 0
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_space_station run tag @s remove hh_o2tank_active

# ─── Rest Base regen: +100 oxy / 3s (60 ticks) ───
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_rest_base run scoreboard players add @s hh_o2_rb_regen 1
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_rest_base run scoreboard players set @s hh_oxygen_dmg 0
execute as @a[tag=hh_lunar_oxygen,scores={hh_o2_rb_regen=60..}] run function haohan:lunar/environment/regen_rest_base

# ─── Space Station regen: +150 oxy / 2s (40 ticks) ───
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_space_station run scoreboard players add @s hh_o2_ss_regen 1
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_space_station run scoreboard players set @s hh_oxygen_dmg 0
execute as @a[tag=hh_lunar_oxygen,scores={hh_o2_ss_regen=40..}] run function haohan:lunar/environment/regen_space_station

# Charge held oxygen tank in safe structures
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_rest_base run function haohan:lunar/environment/tank_charge
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_space_station run function haohan:lunar/environment/tank_charge

# Reset regen timers when leaving structures.
execute as @a[tag=hh_lunar_oxygen,scores={hh_o2_rb_regen=1..}] at @s unless predicate haohan:in_rest_base run scoreboard players set @s hh_o2_rb_regen 0
execute as @a[tag=hh_lunar_oxygen,scores={hh_o2_ss_regen=1..}] at @s unless predicate haohan:in_space_station run scoreboard players set @s hh_o2_ss_regen 0


# Cap oxygen at max 600.
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen=601..}] run scoreboard players set @s hh_oxygen 600

# Cap tank oxygen at tier capacity.
execute as @a[tag=hh_lunar_oxygen,scores={hh_o2tank_tier=1,hh_o2tank=1001..}] run scoreboard players set @s hh_o2tank 1000
execute as @a[tag=hh_lunar_oxygen,scores={hh_o2tank_tier=2,hh_o2tank=2001..}] run scoreboard players set @s hh_o2tank 2000
execute as @a[tag=hh_lunar_oxygen,scores={hh_o2tank_tier=3,hh_o2tank=4001..}] run scoreboard players set @s hh_o2tank 4000

# Run oxygen tank tick logic.
execute as @a[tag=hh_lunar_oxygen] run function haohan:lunar/item/oxygen_tank/oxygen_tank
