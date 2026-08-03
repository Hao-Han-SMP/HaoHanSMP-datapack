# ═══════════════════════════════════════════════════════════════
# Regen Rest Base – refill logic at rest base (+100 / 3s)
# Refills player base oxygen to 600. Tank score is reset upon entering base.
# ═══════════════════════════════════════════════════════════════

# Reset regen timer
scoreboard players set @s hh_o2_rb_regen 0

# Check if player is holding a tank and it is damaged (damage > 0)
scoreboard players set @s hh_o2tank_damage 0
execute if data entity @s SelectedItem.components."minecraft:custom_data".haohan.oxygen_tank run execute store result score @s hh_o2tank_damage run data get entity @s SelectedItem.components."minecraft:damage"

# If damage > 0, repair the tank, show success message, and play sound at player position
execute if score @s hh_o2tank_damage matches 1.. run item modify entity @s weapon.mainhand haohan:repair_tank
execute if score @s hh_o2tank_damage matches 1.. run title @s subtitle [{"text":"🔋 Bình oxy đã được nạp xong!","color":"green"}]
execute if score @s hh_o2tank_damage matches 1.. run title @s title {"text":""}
execute if score @s hh_o2tank_damage matches 1.. at @s run playsound minecraft:block.beacon.power_select master @s ~ ~ ~ 2 1.73

# Refill base oxygen to 600
execute if score @s hh_oxygen matches ..599 run scoreboard players add @s hh_oxygen 100
execute if score @s hh_oxygen matches 601.. run scoreboard players set @s hh_oxygen 600
