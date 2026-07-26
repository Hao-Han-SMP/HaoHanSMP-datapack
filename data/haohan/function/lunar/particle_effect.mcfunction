# Increment global particle timer
scoreboard players add #global hh_particle_timer 1

# Execute player particle logic every 8 ticks (~2.5 times per second)
execute if score #global hh_particle_timer matches 8.. run scoreboard players set #global hh_particle_timer 0
execute if score #global hh_particle_timer matches 0 as @a at @s if dimension haohan:lunar run function haohan:lunar/particle_player
