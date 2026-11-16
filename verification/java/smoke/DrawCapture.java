package smoke;

import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class DrawCapture {
    public record Box(AABB bounds, GizmoStyle style) {}

    private static List<Box> boxes;

    public static void start() {
        boxes = new ArrayList<>();
    }

    public static void record(AABB bounds, GizmoStyle style) {
        if (boxes != null) {
            boxes.add(new Box(bounds, style));
        }
    }

    public static List<Box> finish() {
        var captured = boxes;
        boxes = null;
        return captured;
    }
}
