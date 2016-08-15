package com.generalbytes.osmosis.borderextract;

/*
Copyright (C) 2012 by GB General Bytes GmbH, Baden, Switzerland

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/


import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.ArrayList;
import java.util.List;


/**
 * A polygon consists of a series of outer and inner ways, describing one or more areas.
 */
class Polygon {
    /**
     * Outer ways of the polygon, aka contour.
     */
    private List<Way> outerWays = new ArrayList<Way>();

    /**
     * Inner ways of the polygons, aka holes.
     */
    private List<Way> innerWays = new ArrayList<Way>();

    /**
     * A name of the polygon.
     */
    private String name;

    /**
     * Create a new polygon.
     * @param name The name of the area enclosed by the polygon.
     */
    public Polygon(String name) {
        this.name = name;
    }

    /**
     * Get the holes of this polygon.
     * @return The holes or an empty list.
     */
    public List<Way> getInnerWays() {
        return innerWays;
    }

    /**
     * Get the outer contour of this polygon.
     * @return The contour or an empty list.
     */
    public List<Way> getOuterWays() {
        return outerWays;
    }

    /**
     * Get the name of the enclosed area.
     * @return The name.
     */
    public String getName() {
        return name;
    }
}
