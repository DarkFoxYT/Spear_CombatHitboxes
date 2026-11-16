package net.dark.darks_pvp_optimiser.client;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

public final class SpearModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        FabricLoader loader = FabricLoader.getInstance();
        if (!loader.isModLoaded("cloth-config")) {
            return null;
        }

        return parent -> {
            if (loader.isModLoaded("combat-hitboxes")) {
                Screen combatSettings = ModMenu.getConfigScreen("combat-hitboxes", parent);
                if (combatSettings != null) {
                    return combatSettings;
                }
            }
            return SpearSettings.standaloneScreen(parent);
        };
    }
}
