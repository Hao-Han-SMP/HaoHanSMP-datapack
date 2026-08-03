# ═══════════════════════════════════════════════════════════════
# Oxygen Tank System – runs every tick for lunar players
# ═══════════════════════════════════════════════════════════════

# Automatically ensure active tag when tank has fuel
execute as @s[scores={hh_o2tank=1..}] run tag @s add hh_o2tank_active

# Detect right-click on oxygen tank (player must be holding an oxygen tank item)
execute as @s[scores={hh_o2tank_use=1..}] run function haohan:lunar/item/oxygen_tank/activate

# Reset use counter
scoreboard players set @s hh_o2tank_use 0

# Drain oxygen from active tank into base oxygen bar
execute as @s[scores={hh_o2tank=1..}] run function haohan:lunar/item/oxygen_tank/drain

# Notification when tank is fully depleted (reaches exactly 0)
execute as @s[scores={hh_o2tank=0},tag=hh_o2tank_active] run title @s subtitle [{"text":"⚠ Bình oxy đã cạn!","color":"red"}]
execute as @s[scores={hh_o2tank=0},tag=hh_o2tank_active] run title @s title {"text":""}
execute as @s[scores={hh_o2tank=0},tag=hh_o2tank_active] at @s run playsound minecraft:entity.player.breath master @s ~ ~ ~ 2 0.4
execute as @s[scores={hh_o2tank=0},tag=hh_o2tank_active] run scoreboard players set @s hh_o2tank_tier 0
execute as @s[scores={hh_o2tank=0},tag=hh_o2tank_active] run tag @s remove hh_o2tank_active
