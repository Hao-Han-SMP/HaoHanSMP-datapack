# Remove the no_mining attribute modifier first
attribute @s minecraft:block_break_speed modifier remove haohan:no_mining
# Add the slow mining attribute modifier (-98% break speed)
attribute @s minecraft:block_break_speed modifier add haohan:slow_mining -0.974 add_multiplied_total
