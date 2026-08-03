# ═══════════════════════════════════════════════════════════════
# Regen Rest Base – refill logic at rest base (+100 / 3s)
# Refills player base oxygen to 600. Tank score is reset upon entering base.
# ═══════════════════════════════════════════════════════════════

# Reset regen timer
scoreboard players set @s hh_o2_rb_regen 0

# Refill base oxygen to 600
execute if score @s hh_oxygen matches ..599 run scoreboard players add @s hh_oxygen 100
execute if score @s hh_oxygen matches 601.. run scoreboard players set @s hh_oxygen 600
