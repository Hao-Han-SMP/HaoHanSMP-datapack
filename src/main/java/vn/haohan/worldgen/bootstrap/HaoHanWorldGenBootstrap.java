package vn.haohan.worldgen.bootstrap;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;

/**
 * Bootstrap entry point for HaoHan-WorldGen.
 */
public final class HaoHanWorldGenBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(final BootstrapContext context) {
        context.getLogger().info("Initializing {} v{} bootstrap...",
                context.getConfiguration().getName(),
                context.getConfiguration().getVersion());
    }
}
