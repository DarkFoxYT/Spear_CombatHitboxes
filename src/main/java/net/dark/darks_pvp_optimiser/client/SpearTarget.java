package net.dark.darks_pvp_optimiser.client;

import net.dark.darks_pvp_optimiser.mixin.PlayerTargetAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public record SpearTarget(Entity entity, int rgb) {
    public static final SpearTarget NONE = new SpearTarget(null, 0);

    public static AttackRange hotbarRange() {
        Minecraft game = Minecraft.getInstance();
        LocalPlayer player = game.player;
        if (player == null || player.isSpectator() || game.getCameraEntity() != player) {
            return null;
        }

        AttackRange heldSpear = rangeOfSpear(player.getMainHandItem());
        if (heldSpear != null) {
            return heldSpear;
        }

        Inventory inventory = player.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (slot == selectedSlot) {
                continue;
            }

            AttackRange reach = rangeOfSpear(inventory.getItem(slot));
            if (reach != null) {
                return reach;
            }
        }
        return null;
    }

    private static AttackRange rangeOfSpear(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(ItemTags.SPEARS)) {
            return null;
        }
        return stack.get(DataComponents.ATTACK_RANGE);
    }

    public static SpearTarget find(float partialTick) {
        return find(partialTick, hotbarRange());
    }

    public static SpearTarget find(float partialTick, AttackRange spearRange) {
        Minecraft game = Minecraft.getInstance();
        LocalPlayer player = game.player;
        if (spearRange == null || player == null || player.isSpectator() || game.getCameraEntity() != player) {
            return NONE;
        }
        return pick(player, spearRange, partialTick, null);
    }

    static SpearTarget pick(LocalPlayer player, AttackRange spearRange, float partialTick, HitResult crosshairHit) {
        AttackRange activeReach = player.getActiveItem().get(DataComponents.ATTACK_RANGE);
        HitResult meleeHit = crosshairHit;
        if (activeReach != null || meleeHit == null) {
            meleeHit = PlayerTargetAccessor.regularTarget(player, player.blockInteractionRange(),
                    player.entityInteractionRange(), partialTick);
        }
        if (meleeHit instanceof EntityHitResult hit) {
            return livingTarget(hit, SpearConfig.get().meleeColor);
        }

        HitResult spearHit = crosshairHit;
        if (spearHit == null || !spearRange.equals(activeReach)) {
            spearHit = spearRange.getClosesetHit(player, partialTick, EntitySelector.CAN_BE_PICKED);
        }
        if (spearHit instanceof EntityHitResult hit) {
            return livingTarget(hit, SpearConfig.get().spearColor);
        }
        return NONE;
    }

    private static SpearTarget livingTarget(EntityHitResult hit, int outlineColor) {
        if (hit.getEntity() instanceof LivingEntity living) {
            return new SpearTarget(living, outlineColor);
        }
        return NONE;
    }

    public int color(Entity rendered, int fallbackColor) {
        return rendered == entity ? rgb : fallbackColor;
    }
}
