/**
 * Node.java
 * <p>
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */
package ru.doronin.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.doronin.engine.PlaneUtils.choosePlane;

/**
 Содержит узел BSP - дерева
 */
public class Node {

    /**
     * Полигоны
     */
    private List<Polygon> polygons;
    /**
     * Плоскость
     */
    private Plane plane;
    /**
     * Переднее поддерево
     */
    private Node front;
    /**
     * Заднее поддерево
     */
    private Node back;

    public Node(List<Polygon> polygons) {
        this.polygons = new ArrayList<>();
        if (polygons != null) {
            this.build(polygons);
        }
    }

    public Node() {
        this(null);
    }

    @Override
    public Node clone() {
        Node node = new Node();
        node.plane = this.plane == null ? null : this.plane.clone();
        node.front = this.front == null ? null : this.front.clone();
        node.back = this.back == null ? null : this.back.clone();

        Stream<Polygon> polygonStream;

        if (polygons.size() > 200) {
            polygonStream = polygons.parallelStream();
        } else {
            polygonStream = polygons.stream();
        }

        node.polygons = polygonStream.
                map(p -> p.clone()).collect(Collectors.toList());

        return node;
    }

    /**
     * Меняет местами заполненное и пустое пространство
     */
    public void invert() {

        Stream<Polygon> polygonStream;

        if (polygons.size() > 200) {
            polygonStream = polygons.parallelStream();
        } else {
            polygonStream = polygons.stream();
        }

        polygonStream.forEach((polygon) -> {
            polygon.flip();
        });

        if (this.plane == null && !polygons.isEmpty()) {
            this.plane = polygons.get(0).plane.clone();
        } else if (this.plane == null && polygons.isEmpty()) {
            throw new RuntimeException("Please fix me! I don't know what to do?");
        }

        this.plane.flip();

        if (this.front != null) {
            this.front.invert();
        }
        if (this.back != null) {
            this.back.invert();
        }
        Node temp = this.front;
        this.front = this.back;
        this.back = temp;
    }

    /**
     * Рекурсивно исключает из переданного списка полигоны, попадающие в текущее дерево
     * Возвращает список оставшихся
     */
    private List<Polygon>
    clipPolygons(List<Polygon> polygons) {

        if (this.plane == null) {
            return new ArrayList<>(polygons);
        }

        List<Polygon> frontP = new ArrayList<>();
        List<Polygon> backP = new ArrayList<>();

        for (Polygon polygon : polygons) {
            this.plane.splitPolygon(polygon, frontP, backP);
        }
        if (this.front != null) {
            frontP = this.front.clipPolygons(frontP);
        }
        if (this.back != null) {
            backP = this.back.clipPolygons(backP);
        } else {
            backP = new ArrayList<>(0);
        }

        frontP.addAll(backP);
        return frontP;
    }

    /**
     * Исключает из текущего дерева многоугольники, попавшие в переданное дерево
     */
    public void clipTo(Node bsp) {
        this.polygons = bsp.clipPolygons(this.polygons);
        if (this.front != null) {
            this.front.clipTo(bsp);
        }
        if (this.back != null) {
            this.back.clipTo(bsp);
        }
    }

    /**
     * Возвращает все полигоны текущего дерева
     */
    public List<Polygon> allPolygons() {
        List<Polygon> localPolygons = new ArrayList<>(this.polygons);
        if (this.front != null) {
            localPolygons.addAll(this.front.allPolygons());
        }
        if (this.back != null) {
            localPolygons.addAll(this.back.allPolygons());
        }

        return localPolygons;
    }


    /**
     * Построение дерева на основе переданного списка полигонов
     */
    public final void build(List<Polygon> polygons) {

        if (polygons.isEmpty()) return;

        if (this.plane == null) {
            Polygon chosen = choosePlane(polygons);
            this.polygons.add(chosen);
            this.plane = chosen.plane.clone();
            polygons.remove(chosen);
        }

        List<Polygon> frontP = new ArrayList<>();
        List<Polygon> backP = new ArrayList<>();

        polygons.stream().forEach((polygon) -> this.plane.splitPolygon(polygon, frontP, backP));

        if (frontP.size() > 0) {
            if (this.front == null) {
                this.front = new Node();
            }
            this.front.build(frontP);
        }
        if (backP.size() > 0) {
            if (this.back == null) {
                this.back = new Node();
            }
            this.back.build(backP);
        }
    }
}
