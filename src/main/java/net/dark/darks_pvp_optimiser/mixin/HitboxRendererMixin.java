package net.dark.darks_pvp_optimiser.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.dark.darks_pvp_optimiser.client.SpearHitboxes;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityHitboxDebugRenderer.class, priority = 1100)
abstract class HitboxRendererMixin {
    @Unique private static final SpearHitboxes spear$hitboxes = new SpearHitboxes();

    @Unique private static final String CUBOID = "Lnet/minecraft/gizmos/Gizmos;cuboid("
            + "Lnet/minecraft/world/phys/AABB;Lnet/minecraft/gizmos/GizmoStyle;)"
            + "Lnet/minecraft/gizmos/GizmoProperties;";

    @Inject(method = "emitGizmos", at = @At("HEAD"))
    private void spear$beginFrame(double cameraX, double cameraY, double cameraZ, DebugValueAccess debugValues,
                                  Frustum frustum, float partialTick, CallbackInfo callback) {
        spear$hitboxes.beginFrame(partialTick);
    }

    @Inject(method = "emitGizmos", at = @At("RETURN"))
    private void spear$endFrame(CallbackInfo callback) {
        spear$hitboxes.endFrame();
    }

    @ModifyArg(method = "showHitboxes", index = 1, at = @At(value = "INVOKE", target = CUBOID, ordinal = 0))
    private GizmoStyle spear$vanillaBody(AABB body, GizmoStyle lines, @Local(argsOnly = true) Entity entity,
                                         @Local(argsOnly = true) boolean serverHitbox) {
        if (serverHitbox) {
            return lines;
        }
        return spear$hitboxes.styleBody(entity, body, lines);
    }

    // Both Combat Hitboxes renderers submit their optional border before the body.
    @Dynamic
    @ModifyArg(
            method = {"render_1_21_1_boxes", "truewulf$renderEntityBox"},
            index = 1, remap = false, require = 0,
            at = @At(value = "INVOKE", target = CUBOID, ordinal = 1, remap = true)
    )
    private static GizmoStyle spear$combatBody(AABB body, GizmoStyle lines, @Local(argsOnly = true) Entity entity) {
        return spear$hitboxes.styleBody(entity, body, lines);
    }
}
