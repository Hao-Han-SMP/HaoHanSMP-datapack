# ═══════════════════════════════════════════════════════════════
# Oxygen Actionbar Display – 10 bubbles (● full, ○ empty)
# Each bubble = 60 oxygen points, total = 600 (30 seconds)
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

# 10/10 bubbles (541-600)
execute if score @s hh_oxygen matches 541.. run title @s actionbar [{"text":"● ● ● ● ● ● ● ● ● ●","color":"blue"}]

# 9/10 bubbles (481-540)
execute if score @s hh_oxygen matches 481..540 run title @s actionbar [{"text":"● ● ● ● ● ● ● ● ● ","color":"blue"},{"text":"○","color":"dark_gray"}]

# 8/10 bubbles (421-480)
execute if score @s hh_oxygen matches 421..480 run title @s actionbar [{"text":"● ● ● ● ● ● ● ● ","color":"blue"},{"text":"○ ○","color":"dark_gray"}]

# 7/10 bubbles (361-420)
execute if score @s hh_oxygen matches 361..420 run title @s actionbar [{"text":"● ● ● ● ● ● ● ","color":"blue"},{"text":"○ ○ ○","color":"dark_gray"}]

# 6/10 bubbles (301-360)
execute if score @s hh_oxygen matches 301..360 run title @s actionbar [{"text":"● ● ● ● ● ● ","color":"blue"},{"text":"○ ○ ○ ○","color":"dark_gray"}]

# 5/10 bubbles (241-300)
execute if score @s hh_oxygen matches 241..300 run title @s actionbar [{"text":"● ● ● ● ● ","color":"blue"},{"text":"○ ○ ○ ○ ○","color":"dark_gray"}]

# 4/10 bubbles (181-240)
execute if score @s hh_oxygen matches 181..240 run title @s actionbar [{"text":"● ● ● ● ","color":"blue"},{"text":"○ ○ ○ ○ ○ ○","color":"dark_gray"}]

# 3/10 bubbles (121-180)
execute if score @s hh_oxygen matches 121..180 run title @s actionbar [{"text":"● ● ● ","color":"blue"},{"text":"○ ○ ○ ○ ○ ○ ○","color":"dark_gray"}]

# 2/10 bubbles (61-120)
execute if score @s hh_oxygen matches 61..120 run title @s actionbar [{"text":"● ● ","color":"blue"},{"text":"○ ○ ○ ○ ○ ○ ○ ○","color":"dark_gray"}]

# 1/10 bubbles (1-60)
execute if score @s hh_oxygen matches 1..60 run title @s actionbar [{"text":"● ","color":"blue"},{"text":"○ ○ ○ ○ ○ ○ ○ ○ ○","color":"dark_gray"}]

# 0/10 bubbles (depleted)
execute if score @s hh_oxygen matches ..0 run title @s actionbar [{"text":"○ ○ ○ ○ ○ ○ ○ ○ ○ ○","color":"dark_gray"}]
