/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.doronin.engine.samples;

import ru.doronin.engine.CSG;
import ru.doronin.engine.Cube;
import ru.doronin.engine.FileUtil;
import ru.doronin.engine.Transform;

import java.io.IOException;
import java.nio.file.Paths;

import static ru.doronin.engine.Transform.unity;

/**
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MoebiusStairs {

    private double n = 45;
    private double twists = 1;
    private double tilt = 0;

    public MoebiusStairs resolution(double n) {
        this.n = n;
        return this;
    }

    public MoebiusStairs twists(double twists) {
        this.twists = twists;
        return this;
    }

    public CSG toCSG() {

        CSG result = null;

        CSG firstCube = null;
        CSG prevCube = null;

        for (int i = 1; i <= n * 2; i++) {

            double deg = i * 360.0 / n;

            Transform rot1 = unity().rotZ(deg);

            Transform translate1 = unity().translate(
                    -20 + 5 * sin(i * 360.0 * (twists + .5) / n),
                    0,
                    8 * cos(i * 360 * (twists + .5) / n));

            Transform rot2 = unity().rotX(90 - tilt);

            Transform finalTransform = rot1.apply(translate1).apply(rot2);

            CSG cube = new Cube(
                    3 + abs(8.0 * cos(30 + (twists + 0.5) * deg)),
                    4,
                    4).toCSG();

            cube = cube.transformed(finalTransform);

            if (i == 1) {
                firstCube = cube;
            }

            if (result == null) {
                result = cube;
            }

            if (prevCube != null) {
                CSG union = cube.hull(prevCube);
                result = result.union(union);
            }

            if (i == n * 2) {
                CSG union = firstCube.hull(prevCube);
                result = result.union(union);
            }

            prevCube = cube;
        }
        return result.transformed(unity().translateZ(8 + 4 / 2));
    }

    private static double sin(double deg) {
        return Math.sin(Math.toRadians(deg));
    }

    private static double cos(double deg) {
        return Math.cos(Math.toRadians(deg));
    }

    private double abs(double value) {
        return Math.abs(value);
    }

    public static void main(String[] args) throws IOException {

        MoebiusStairs moebiusStairs = new MoebiusStairs();
        CSG csg = moebiusStairs.toCSG();

        FileUtil.write(Paths.get("moebius-stairs.stl"), csg.toStlString());
        csg.toObj().toFiles(Paths.get("moebius-stairs.obj"));

    }
}
