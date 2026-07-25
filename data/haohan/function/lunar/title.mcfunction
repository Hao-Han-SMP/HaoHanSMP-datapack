execute in haohan:lunar positioned 0 0 0 as @a[distance=0..,tag=!lunar_visited] at @s run function haohan:lunar/show_title

execute as @a[tag=lunar_visited] unless dimension haohan:lunar run tag @s remove lunar_visited
