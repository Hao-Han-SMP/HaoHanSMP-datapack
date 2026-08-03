# ═══════════════════════════════════════════════════════════════
# Regen Space Station – refill logic at space station (+150 / 2s)
# Refills player base oxygen to 600. Tank score is reset upon entering station.
# ═══════════════════════════════════════════════════════════════

# Reset regen timer
scoreboard players set @s hh_o2_ss_regen 0

# Refill base oxygen to 600
execute if score @s hh_oxygen matches ..599 run scoreboard players add @s hh_oxygen 150
execute if score @s hh_oxygen matches 601.. run scoreboard players set @s hh_oxygen 600
