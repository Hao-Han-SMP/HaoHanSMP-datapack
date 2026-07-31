# If the block is the custom ore, tag the player and stop raycasting
execute if block ~ ~ ~ note_block[note=24] run tag @s add hh_looking_at_ore

# Otherwise, continue the raycast forward along the line of sight (up to 5 blocks)
execute unless block ~ ~ ~ note_block[note=24] positioned ^ ^ ^0.4 if entity @s[distance=..5] run function haohan:lunar/block/anorthosite_ore/raycast
