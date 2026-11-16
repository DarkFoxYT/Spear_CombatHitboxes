package smoke.mixin;

import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smoke.DrawCapture;

@Mixin(Gizmos.class)
abstract class CaptureMixin {
    @Inject(method = "cuboid(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/gizmos/GizmoStyle;)Lnet/minecraft/gizmos/GizmoProperties;",
            at = @At("HEAD"))
    private static void capture(AABB bounds, GizmoStyle style, CallbackInfoReturnable<?> callback) {
        DrawCapture.record(bounds, style);
    }
}
