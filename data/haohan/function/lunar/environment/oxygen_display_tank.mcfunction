# ═══════════════════════════════════════════════════════════════
# Oxygen Actionbar Display – With Active Tank
# Shows bubbles + tank remaining: ● ● ● ○ ○  (+1234)
# ═══════════════════════════════════════════════════════════════

# 10/10 bubbles (541-600)
execute if score @s hh_oxygen matches 541.. run title @s actionbar [{"text":"● ● ● ● ● ● ● ● ● ●","color":"blue"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]

# 9/10 bubbles (481-540)
execute if score @s hh_oxygen matches 481..540 run title @s actionbar [{"text":"● ● ● ● ● ● ● ● ● ","color":"blue"},{"text":"○","color":"dark_gray"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]

# 8/10 bubbles (421-480)
execute if score @s hh_oxygen matches 421..480 run title @s actionbar [{"text":"● ● ● ● ● ● ● ● ","color":"blue"},{"text":"○ ○","color":"dark_gray"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]

# 7/10 bubbles (361-420)
execute if score @s hh_oxygen matches 361..420 run title @s actionbar [{"text":"● ● ● ● ● ● ● ","color":"blue"},{"text":"○ ○ ○","color":"dark_gray"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]

# 6/10 bubbles (301-360)
execute if score @s hh_oxygen matches 301..360 run title @s actionbar [{"text":"● ● ● ● ● ● ","color":"blue"},{"text":"○ ○ ○ ○","color":"dark_gray"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]

# 5/10 bubbles (241-300)
execute if score @s hh_oxygen matches 241..300 run title @s actionbar [{"text":"● ● ● ● ● ","color":"blue"},{"text":"○ ○ ○ ○ ○","color":"dark_gray"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]

# 4/10 bubbles (181-240)
execute if score @s hh_oxygen matches 181..240 run title @s actionbar [{"text":"● ● ● ● ","color":"blue"},{"text":"○ ○ ○ ○ ○ ○","color":"dark_gray"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]

# 3/10 bubbles (121-180)
execute if score @s hh_oxygen matches 121..180 run title @s actionbar [{"text":"● ● ● ","color":"blue"},{"text":"○ ○ ○ ○ ○ ○ ○","color":"dark_gray"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]

# 2/10 bubbles (61-120)
execute if score @s hh_oxygen matches 61..120 run title @s actionbar [{"text":"● ● ","color":"blue"},{"text":"○ ○ ○ ○ ○ ○ ○ ○","color":"dark_gray"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]

# 1/10 bubbles (1-60)
execute if score @s hh_oxygen matches 1..60 run title @s actionbar [{"text":"● ","color":"blue"},{"text":"○ ○ ○ ○ ○ ○ ○ ○ ○","color":"dark_gray"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]

# 0/10 bubbles (depleted)
execute if score @s hh_oxygen matches ..0 run title @s actionbar [{"text":"○ ○ ○ ○ ○ ○ ○ ○ ○ ○","color":"dark_gray"},{"text":"  (+","color":"green"},{"score":{"name":"*","objective":"hh_o2tank"},"color":"green"},{"text":")","color":"green"}]
