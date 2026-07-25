scoreboard objectives add hh_motion_y dummy
scoreboard objectives add hh_oxygen dummy
scoreboard objectives add hh_oxygen_dmg dummy

# Reapply current values to already-loaded entities after /reload.
execute in haohan:lunar positioned 0 0 0 run tag @e[distance=0..] remove hh_lunar_physic
