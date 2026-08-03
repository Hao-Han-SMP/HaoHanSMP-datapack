scoreboard objectives add hh_motion_y dummy
scoreboard objectives add hh_oxygen dummy
scoreboard objectives add hh_oxygen_dmg dummy
scoreboard objectives add hh_particle_timer dummy

# Reapply current values to already-loaded entities after /reload.
execute in haohan:lunar positioned 0 0 0 run tag @e[distance=0..] remove hh_lunar_physic

scoreboard objectives add hh_oxygen_regen dummy
scoreboard objectives add hh_raycast_steps dummy

# Oxygen Tank system
scoreboard objectives add hh_o2tank dummy
scoreboard objectives add hh_o2tank_use minecraft.used:minecraft.carrot_on_a_stick
scoreboard objectives add hh_o2tank_tier dummy
scoreboard objectives add hh_o2_rb_regen dummy
scoreboard objectives add hh_o2_ss_regen dummy
scoreboard objectives add hh_o2tank_new dummy
scoreboard objectives add hh_o2tank_tier_new dummy
scoreboard objectives add hh_o2tank_damage dummy
scoreboard objectives add hh_o2tank_pct dummy
scoreboard objectives add hh_o2tank_cur_pct dummy
scoreboard objectives add hh_o2tank_charge dummy
scoreboard objectives add hh_const dummy

scoreboard players set #100 hh_const 100
scoreboard players set #200 hh_const 200
scoreboard players set #320 hh_const 320
scoreboard players set #1000 hh_const 1000
scoreboard players set #2000 hh_const 2000
scoreboard players set #4000 hh_const 4000




