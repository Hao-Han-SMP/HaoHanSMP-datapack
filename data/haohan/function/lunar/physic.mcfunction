# Apply attributes only once when an entity enters the lunar dimension.
execute in haohan:lunar positioned 0 0 0 as @e[distance=0..,tag=!hh_lunar_physic] run function haohan:lunar/apply_attributes

# Only tagged entities need to be checked for leaving the lunar dimension.
execute as @e[tag=hh_lunar_physic] unless dimension haohan:lunar run function haohan:lunar/reset_attributes

# Items and physics blocks use built-in gravity instead of minecraft:gravity.
execute in haohan:lunar positioned 0 0 0 as @e[distance=0..,type=#haohan:lunar_gravity] unless data entity @s {OnGround:1b} run function haohan:lunar/apply_falling_gravity
