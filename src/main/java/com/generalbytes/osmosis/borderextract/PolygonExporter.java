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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;

/**
 * Defines a exporter interface for a single polygon.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
interface PolygonExporter {
    /**
     * Marshal a polygon to writer.
     * @param data      The full OSM data.
     * @param polygon   Export this polygon.
     * @param writer    Write marshalled polygon to this writer.
     * @throws java.io.IOException  Error writing to writer.
     * @throws IncompleteBorderException Incomplete border encountered.
     */
    void writePolygon(@Nonnull OSMData data, @Nonnull Polygon polygon, @Nonnull Writer writer)
            throws IOException, IncompleteBorderException;
}
