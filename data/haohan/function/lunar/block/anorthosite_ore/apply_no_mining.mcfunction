# Remove the slow_mining attribute modifier first
attribute @s minecraft:block_break_speed modifier remove haohan:slow_mining
# Add the no mining attribute modifier (-100% break speed, completely unbreakable)
attribute @s minecraft:block_break_speed modifier add haohan:no_mining -1.0 add_multiplied_total
