package net.dark.darks_pvp_optimiser.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = "darks_pvp_optimiser", dist = Dist.CLIENT)
public final class SpearNeoForge {
    public SpearNeoForge(ModContainer container) {
        if (ModList.get().isLoaded("cloth_config")) {
            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (mod, parent) -> SpearSettings.standaloneScreen(parent));
        }
    }
}
