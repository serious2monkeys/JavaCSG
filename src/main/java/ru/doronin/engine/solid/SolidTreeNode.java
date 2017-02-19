package ru.doronin.engine.solid;

import ru.doronin.engine.Plane;
import ru.doronin.engine.Polygon;

import java.util.ArrayList;
import java.util.List;

import static ru.doronin.engine.PlaneUtils.choosePlane;

/**
 * Created by Anton Doronin on 18.02.2017.
 * Узел BSP-дерева, не хранящий геометрии,
 * но имеющий определнный флаг заполненности пространства
 */
public class SolidTreeNode {
    private Plane plane;
    private boolean solid = false;
    private SolidTreeNode front, back;

    public SolidTreeNode(List<Polygon> polygons) {
        if (polygons != null) {
            build(polygons);
        }
    }

    public SolidTreeNode() {

    }

    public boolean isSolid() {
        return solid;
    }

    public final void build(List<Polygon> polygons) {
        if (polygons.isEmpty()) {
            return;
        }
        if (this.plane == null) {
            Polygon chosen = choosePlane(polygons);
            this.plane = chosen.plane.clone();
            polygons.remove(chosen);
        }

        List<Polygon> frontP = new ArrayList<>();
        List<Polygon> backP = new ArrayList<>();

        polygons.stream().forEach((polygon) -> this.plane.splitPolygon(polygon, frontP, backP));

        if (this.front == null) {
            this.front = new SolidTreeNode(frontP);
        }
        if (frontP.size() > 0) {
            this.front.build(frontP);
        }
        if (this.back == null) {
            this.back = new SolidTreeNode(backP);
        }
        if (backP.size() > 0) {
            this.back.build(backP);
        } else {
            this.back.solid = true;
        }
    }
}
