# Remove the looking_at_ore tag first
tag @s remove hh_looking_at_ore

# Start the raycast from the player's eyes
execute anchored eyes positioned ^ ^ ^0.2 run function haohan:lunar/block/anorthosite_ore/raycast

# Update mining attributes based on tag and mainhand item
execute if entity @s[tag=hh_looking_at_ore] if items entity @s weapon.mainhand minecraft:netherite_pickaxe run function haohan:lunar/block/anorthosite_ore/apply_slow_mining
execute if entity @s[tag=hh_looking_at_ore] unless items entity @s weapon.mainhand minecraft:netherite_pickaxe run function haohan:lunar/block/anorthosite_ore/apply_no_mining
execute unless entity @s[tag=hh_looking_at_ore] run function haohan:lunar/block/anorthosite_ore/reset_mining_attributes
