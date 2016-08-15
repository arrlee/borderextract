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

import com.google.common.base.Charsets;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.store.NoSuchIndexElementException;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Locale;
import java.util.logging.Logger;


/**
 * Border listener to export polygons to a poly-file.
 * @see <a href="http://www.cs.cmu.edu/~quake/triangle.poly.html">Poly file specification</a>
 * @author Andre Lison, GB General Bytes GmbH, 2012
 */
public class PolyBorderListener extends ZipFileBorderListener implements PolygonExporter {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(PolyBorderListener.class.getName());

    /**
     * Get the export to use for marshalling a polygon.
     *
     * @return The exporter.
     */
    @Override
    public PolygonExporter getExporter() {
        return this;
    }

    /**
     * Get the file suffix to use for a single zip file entry.
     *
     * @return The suffix without the leading point.
     */
    @Override
    public String getEntrySuffix() {
        return "poly";
    }

    /**
     * Marshal a polygon to writer.
     *
     * @param data    The full OSM data.
     * @param polygon Export this polygon.
     * @param writer  Write marshalled polygon to this writer.
     * @throws java.io.IOException Error writing to writer.
     * @throws IncompleteBorderException
     *                             Incomplete border encountered.
     */
    @Override
    public void writePolygon(@Nonnull OSMData data, @Nonnull Polygon polygon, @Nonnull Writer writer)
            throws IOException, IncompleteBorderException {
        writer.append(polygon.getName()).append('\n');

        int wayCounter = 1;
        for (Way outerWay : polygon.getOuterWays()) {
            writeWay(data, outerWay, wayCounter, writer);
            wayCounter++;
        }
        for (Way innerWay : polygon.getInnerWays()) {
            writer.append('!');
            writeWay(data, innerWay, wayCounter, writer);
            wayCounter++;
        }



        writer.append("END").append('\n');
    }

    /**
     * Marshal a single way to a writer.
     * @param data      The complete OSM data.
     * @param way       Marshal this way.
     * @param wayNumber The number of the way.
     * @param writer    Marshal to this writer.
     * @throws IncompleteBorderException Elements of a border are missing in OSM data.
     * @throws java.io.IOException Error writing to the writer.
     */
    private void writeWay(OSMData data, Way way, int wayNumber, Writer writer) throws IOException, IncompleteBorderException {
        writer.append(Integer.toString(wayNumber)).append('\n');

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(bout);

        for (WayNode wayNode : way.getWayNodes()) {
            try {
                Node node = data.getNode(wayNode.getNodeId());

                printStream.printf(Locale.US, "\t%+E %+E\n", node.getLongitude(), node.getLatitude());
            } catch(NoSuchIndexElementException e) {
                throw new IncompleteBorderException("Can not get node " + wayNode.getNodeId());
            }
        }

        printStream.close();
        String coordinates = new String(bout.toByteArray(), Charsets.ISO_8859_1);
        writer.append(coordinates);
        bout.close();

        writer.append("END\n");
    }
}
