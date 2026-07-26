# ═══════════════════════════════════════════════════════════════
# Oxygen System – runs every tick for players in haohan:lunar
# ═══════════════════════════════════════════════════════════════

# Initialize oxygen for players entering the lunar dimension (600 = 10 bubbles × 60 ticks each = 30 seconds).
execute in haohan:lunar positioned 0 0 0 as @a[distance=0..,tag=!hh_lunar_oxygen] run scoreboard players set @s hh_oxygen 600
execute in haohan:lunar positioned 0 0 0 as @a[distance=0..,tag=!hh_lunar_oxygen] run scoreboard players set @s hh_oxygen_dmg 0
execute in haohan:lunar positioned 0 0 0 as @a[distance=0..,tag=!hh_lunar_oxygen] run tag @s add hh_lunar_oxygen

# Decrease oxygen by 1 each tick (only while oxygen remains).
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen=1..}] run scoreboard players remove @s hh_oxygen 1

# Suffocation damage when oxygen is depleted (1 HP every 20 ticks ≈ 1 second).
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen=..0}] run scoreboard players add @s hh_oxygen_dmg 1
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen_dmg=20..}] at @s run damage @s 1 minecraft:drown
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen_dmg=20..}] run scoreboard players set @s hh_oxygen_dmg 0

# Display oxygen bar on actionbar every tick.
execute as @a[tag=hh_lunar_oxygen] run function haohan:lunar/oxygen_display

# Reset oxygen when player leaves the lunar dimension.
execute as @a[tag=hh_lunar_oxygen] at @s unless dimension haohan:lunar run function haohan:lunar/reset_oxygen

# Regenerate oxygen in rest base every 5 ticks.
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_rest_base run scoreboard players add @s hh_oxygen_regen 1
execute as @a[tag=hh_lunar_oxygen] at @s if predicate haohan:in_rest_base run scoreboard players set @s hh_oxygen_dmg 0
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen_regen=5..}] run scoreboard players add @s hh_oxygen 15
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen_regen=5..}] run scoreboard players set @s hh_oxygen_regen 0
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen_regen=1..}] at @s unless predicate haohan:in_rest_base run scoreboard players set @s hh_oxygen_regen 0
execute as @a[tag=hh_lunar_oxygen,scores={hh_oxygen=601..}] run scoreboard players set @s hh_oxygen 600
