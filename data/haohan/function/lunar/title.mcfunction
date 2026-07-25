execute as @a at @s if dimension haohan:lunar if entity @s[tag=!lunar_visited] run title @s subtitle {"text":"ᴍặᴛ ᴛʀăɴɢ","color":"gray","italic":false,"bold":true}
execute as @a at @s if dimension haohan:lunar if entity @s[tag=!lunar_visited] run title @s title {"text":"🌙", color:"gray", bold:true}
execute as @a at @s if dimension haohan:lunar if entity @s[tag=!lunar_visited] run playsound minecraft:item.trident.thunder master @s ~ ~ ~ 2 0.7
execute as @a at @s if dimension haohan:lunar if entity @s[tag=!lunar_visited] run tag @s add lunar_visited

execute as @a[tag=lunar_visited] at @s unless dimension haohan:lunar run tag @s remove lunar_visited
