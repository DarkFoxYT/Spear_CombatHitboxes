package smoke;

import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import net.dark.darks_pvp_optimiser.client.SpearConfig;
import net.dark.darks_pvp_optimiser.client.SpearTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "spear_verification", dist = Dist.CLIENT)
public final class NeoSmoke {
    private int ticksInWorld;

    public NeoSmoke() {
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft game = Minecraft.getInstance();
        if (game.level == null || game.player == null || ++ticksInWorld != 40) {
            return;
        }
        try {
            EntityHitboxDebugRenderer.class.getDeclaredMethods();
            checkModScreen();
            checkSpearReach(game);
            System.out.println("NEOFORGE_VERIFIED: mod icon, settings, renderer, spear and melee range");
            game.stop();
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    private void checkModScreen() {
        var container = ModList.get().getModContainerById("darks_pvp_optimiser").orElseThrow();
        var info = container.getModInfo();
        String logo = info.getLogoFile().orElseThrow();
        if (!logo.equals("icon.png") || SpearConfig.class.getResource("/" + logo) == null) {
            throw new AssertionError("Missing mod menu artwork");
        }
        var factory = IConfigScreenFactory.getForMod(info).orElseThrow();
        var screen = (ClothConfigScreen) factory.createScreen(container, null);
        if (screen.getCategorizedEntries().keySet().stream().noneMatch(name -> name.getString().equals("Spear"))) {
            throw new AssertionError("Missing NeoForge spear settings");
        }
        if (SpearConfig.get().accurateHitbox) {
            throw new AssertionError("Accurate hitboxes should start disabled");
        }
    }

    private void checkSpearReach(Minecraft game) {
        game.player.setPos(0.5, 200, 0.5);
        game.player.setYRot(0);
        game.player.setXRot(0);
        game.player.getInventory().setItem(8, Items.IRON_SPEAR.getDefaultInstance());

        Entity husk = BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> type.getDescriptionId().equals("entity.minecraft.husk"))
                .findFirst().orElseThrow()
                .create(game.level, EntitySpawnReason.COMMAND);
        husk.setPos(0.5, 200, 5);
        husk.setId(123456);
        game.level.addEntity(husk);
        expectColor(0xffff9000);

        husk.setPos(0.5, 200, 3);
        expectColor(0xffff0000);
    }

    private void expectColor(int expected) {
        SpearTarget hit = SpearTarget.find(1);
        if (hit.entity() == null || hit.rgb() != expected) {
            throw new AssertionError("Expected outline " + Integer.toHexString(expected) + ", found " + hit);
        }
    }
}
