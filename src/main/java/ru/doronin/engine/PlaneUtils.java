package ru.doronin.engine;

import java.util.List;

/**
 * Created by Anton Doronin on 19.02.2017.
 * Вспомогательные методы для работы с плоскостями
 */
public class PlaneUtils {

    /**
     * Функция выборки лучшей плоскости на основе набора многоугольников (импирический подход, опубликованный в книге
     * Real-time collision detection, Christer Ericson 2005
     *
     * @param polygons - набор многоугольников
     * @return - многоугольник, лежащий на выбранной плоскости
     */
    public static Polygon choosePlane(List<Polygon> polygons) {
        final double k = 0.8;
        Polygon chosen = polygons.get(0);
        double bestScore = Double.MAX_VALUE;
        for (int i = 0; i < polygons.size(); i++) {
            int numFront = 0, numBehind = 0, numSpanning = 0;
            Plane plane = polygons.get(i).plane;
            for (int j = 0; j < polygons.size(); j++) {
                if (i == j) {
                    continue;
                }
                switch (plane.classifyPolygon(polygons.get(j))) {
                    case Plane.COPLANAR:
                    case Plane.FRONT:
                        numFront++;
                        break;
                    case Plane.BACK:
                        numBehind++;
                        break;
                    case Plane.SPANNING:
                        numSpanning++;
                        break;
                }
                double score = k * numSpanning + (1.0 - k) * Math.abs(numFront - numBehind);
                if (score < bestScore) {
                    bestScore = score;
                    chosen = polygons.get(j);
                }
            }
        }
        return chosen;
    }
}
