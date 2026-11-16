package net.dark.darks_pvp_optimiser.client;

import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.phys.AABB;

public final class SpearHitboxes {
    private static final int WHITE = 0xffffffff;

    private float partialTick;
    private AttackRange spearRange;
    private SpearTarget target;
    private GizmoStyle marginLines;

    public void beginFrame(float partialTick) {
        this.partialTick = partialTick;
        spearRange = SpearTarget.hotbarRange();
        target = null;
        marginLines = null;
    }

    public void endFrame() {
        spearRange = null;
        target = null;
    }

    public GizmoStyle styleBody(Entity entity, AABB body, GizmoStyle bodyLines) {
        if (spearRange == null || !(entity instanceof LivingEntity)) {
            return bodyLines;
        }

        Minecraft game = Minecraft.getInstance();
        if (entity == game.player || !EntitySelector.CAN_BE_PICKED.test(entity)) {
            return bodyLines;
        }

        SpearConfig settings = SpearConfig.get();
        if (target == null) {
            target = SpearTarget.pick(game.player, spearRange, partialTick, game.hitResult);
            if (settings.accurateHitbox && spearRange.hitboxMargin() > 0) {
                marginLines = GizmoStyle.stroke(settings.marginColor, settings.marginLineWidth);
            }
        }

        boolean underCrosshair = target.entity() == entity;
        if (marginLines == null) {
            if (underCrosshair) {
                return new GizmoStyle(target.rgb(), settings.lineWidth, bodyLines.fill());
            }
            return bodyLines;
        }

        GizmoStyle outerLines = marginLines;
        if (underCrosshair) {
            outerLines = GizmoStyle.stroke(target.rgb(), settings.lineWidth);
        }
        if ((outerLines.stroke() >>> 24) != 0) {
            Gizmos.cuboid(body.inflate(spearRange.hitboxMargin()), outerLines);
        }

        if (bodyLines.stroke() == WHITE) {
            return bodyLines;
        }
        return new GizmoStyle(WHITE, bodyLines.strokeWidth(), bodyLines.fill());
    }
}
