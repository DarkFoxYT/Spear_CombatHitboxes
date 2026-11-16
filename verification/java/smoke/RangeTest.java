package smoke;

import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.ColorEntry;
import me.shedaniel.clothconfig2.gui.entries.FloatListEntry;
import net.dark.darks_pvp_optimiser.client.SpearConfig;
import net.dark.darks_pvp_optimiser.client.SpearModMenu;
import net.dark.darks_pvp_optimiser.client.SpearTarget;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RangeTest implements FabricClientGameTest {
    private static final int GREEN = 0xff00ff00;
    private static final int ORANGE = 0xffff9000;
    private static final int RED = 0xffff0000;
    private static final int WHITE = 0xffffffff;
    private static final String MOVE_HUSK = "tp @e[type=husk,limit=1,sort=nearest] ";

    private Entity husk;

    @Override
    public void runTest(ClientGameTestContext context) {
        context.setScreen(this::configureSpear);
        context.takeScreenshot("spear-settings");
        context.setScreen(() -> null);

        try (var world = context.worldBuilder().create()) {
            var server = world.getServer();
            server.runCommand("fill -3 199 -2 3 199 12 stone");
            server.runCommand("fill -3 200 -2 3 204 12 air");
            server.runCommand("difficulty normal");
            server.runCommand("tp @p 0.5 200 0.5 0 0");
            server.runCommand("gamemode survival @p");
            server.runCommand("item replace entity @p hotbar.8 with minecraft:iron_spear");
            server.runCommand("summon husk 0.5 200 5.0 {NoAI:1b,IsBaby:0b,Silent:1b,Invulnerable:1b}");
            context.waitTicks(30);
            checkTarget(context, GREEN, "unselected spear with custom color");
            context.runOnClient(game -> {
                husk = SpearTarget.find(1).entity();
                if (!game.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
                    game.debugEntries.toggleStatus(DebugScreenEntries.ENTITY_HITBOXES);
                }
            });

            checkDrawing(context, GREEN, false);
            context.takeScreenshot("custom-spear-width");
            context.runOnClient(game -> {
                SpearConfig.get().spearColor = ORANGE;
                SpearConfig.get().accurateHitbox = true;
            });
            checkDrawing(context, ORANGE, true);
            context.takeScreenshot("accurate-spear-hitbox");

            context.runOnClient(game -> SpearConfig.get().spearColor = 0);
            checkDrawing(context, 0, true);
            context.runOnClient(game -> SpearConfig.get().spearColor = ORANGE);

            server.runCommand("setblock 0 201 2 stone");
            checkTarget(context, 0, "wall blocks spear");
            server.runCommand("setblock 0 201 2 air");
            server.runCommand("item replace entity @p hotbar.0 with minecraft:diamond_spear");
            checkTarget(context, ORANGE, "selected spear");
            server.runCommand(MOVE_HUSK + "0.5 200 3.0");
            checkTarget(context, RED, "regular melee has priority");
            checkDrawing(context, RED, true);
            server.runCommand(MOVE_HUSK + "0.88 200 3.0");
            checkTarget(context, ORANGE, "only the spear margin is under the crosshair");
            checkDrawing(context, ORANGE, true);

            server.runCommand(MOVE_HUSK + "0.5 200 10.0");
            checkTarget(context, 0, "outside spear reach");
            server.runCommand(MOVE_HUSK + "0.5 200 5.0");
            checkTarget(context, ORANGE, "back in spear reach");
            server.runCommand("clear @p");
            checkTarget(context, 0, "empty hotbar");
        }
    }

    private ClothConfigScreen configureSpear() {
        SpearConfig settings = SpearConfig.get();
        if (settings.accurateHitbox) {
            throw new AssertionError("Accurate hitboxes should start disabled");
        }

        var factory = new SpearModMenu().getModConfigScreenFactory();
        var screen = (ClothConfigScreen) factory.create(null);
        var categories = screen.getCategorizedEntries();
        var entries = categories.entrySet().stream()
                .filter(category -> category.getKey().getString().equals("Spear"))
                .findFirst().orElseThrow().getValue();
        if (entries.size() != 6) {
            throw new AssertionError("Missing spear settings");
        }
        ((ColorEntry) entries.get(0)).setValue(GREEN);
        ((FloatListEntry) entries.get(2)).setValue("5.0");
        screen.saveAll(false);
        if (settings.spearColor != GREEN || settings.lineWidth != 5) {
            throw new AssertionError("The Spear category did not save its target style");
        }
        var reopened = (ClothConfigScreen) factory.create(null);
        if (reopened.getCategorizedEntries().size() != categories.size()) {
            throw new AssertionError("Mod Menu reopened a different settings screen");
        }
        screen.selectedCategoryIndex = categories.size() - 1;
        return screen;
    }

    private void checkTarget(ClientGameTestContext context, int color, String scenario) {
        context.waitTicks(6);
        try {
            context.waitFor(game -> SpearTarget.find(1).rgb() == color, 100);
        } catch (AssertionError timeout) {
            context.runOnClient(game -> {
                throw new AssertionError(scenario + " at " + game.player.position()
                        + " looking " + game.player.getLookAngle() + " with " + SpearTarget.hotbarRange()
                        + " found " + SpearTarget.find(1) + ", crosshair " + game.hitResult
                        + ", nearby " + game.level.getEntities(game.player, game.player.getBoundingBox().inflate(12)), timeout);
            });
        }
        System.out.println("Passed " + scenario);
    }

    private void checkDrawing(ClientGameTestContext context, int color, boolean showMargin) {
        context.runOnClient(game -> DrawCapture.start());
        context.waitTicks(5);
        context.runOnClient(game -> {
            List<DrawCapture.Box> boxes = DrawCapture.finish();
            AABB body = husk.getBoundingBox();
            AABB margin = body.inflate(SpearTarget.hotbarRange().hitboxMargin());
            int bodyColor = showMargin ? WHITE : color;
            float bodyWidth = showMargin ? 2.5f : 5f;
            long bodyCount = boxes.stream().filter(box -> box.bounds().equals(body)
                    && box.style().stroke() == bodyColor && box.style().strokeWidth() == bodyWidth).count();
            if (bodyCount == 0) {
                throw new AssertionError("Missing body " + body + " with stroke " + Integer.toHexString(bodyColor)
                        + " at width " + bodyWidth + ". Captured " + boxes.stream().distinct().limit(12).toList());
            }

            var outer = boxes.stream().filter(box -> box.bounds().equals(margin)).toList();
            long expected = showMargin && color != 0 ? bodyCount : 0;
            if (outer.size() != expected || outer.stream().anyMatch(box ->
                    box.style().stroke() != color || box.style().strokeWidth() != 5)) {
                throw new AssertionError("Expected " + expected + " spear outlines at " + margin + ", got " + outer);
            }

            boolean hasEyeLine = boxes.stream().anyMatch(box -> box.bounds().minX == body.minX
                    && box.bounds().minZ == body.minZ && Math.abs(box.bounds().getYsize() - 0.02) < 0.001
                    && box.style().stroke() == RED && box.style().strokeWidth() == 2.5f);
            if (!hasEyeLine) {
                throw new AssertionError("The eye-height line lost its original style");
            }
        });
    }
}
