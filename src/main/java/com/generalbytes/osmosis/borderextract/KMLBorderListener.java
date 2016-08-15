package com.generalbytes.osmosis.borderextract;

/*
Copyright (C) 2011 by GB General Bytes GmbH, Baden, Switzerland

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


import java.io.*;
import java.util.logging.Logger;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.store.NoSuchIndexElementException;

/**
 * The default border listener for various administrative borders.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
public class KMLBorderListener extends ZipFileBorderListener implements PolygonExporter {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(KMLBorderListener.class.getName());

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
        return "kml";
    }

    /**
     * Write the Polygon KML data.
     * @param data      Access to the full OSM data.
     * @param polygon   Export this polygon.
     * @param writer    Write to this writer.
     * @throws IOException  Error writing to writer.
     * @throws IncompleteBorderException Incomplete border encountered.
     */
    public void writePolygon(OSMData data, Polygon polygon, Writer writer) throws IOException, IncompleteBorderException {
        writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        writer.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:xal=\"urn:oasis:names:tc:ciq:xsdschema:xAL:2.0\">");
        writer.append("<Document>");
        for (Way way : polygon.getOuterWays()) {
            writer.append("<Placemark>");
            writer.append("<name>").append(way.toString()).append("</name>");
            writer.append("<Polygon>");
            writer.append("<extrude>1</extrude>");
            writer.append("<altitudeMode>clampToGround</altitudeMode>");

            writer.append("<outerBoundaryIs>");
            appendBoundary(writer, way, data);
            writer.append("</outerBoundaryIs>");

            if (!polygon.getInnerWays().isEmpty()) {
                writer.append("<innerBoundaryIs>");
                for (Way innerWay : polygon.getInnerWays()) {
                    appendBoundary(writer, innerWay, data);
                }
                writer.append("</innerBoundaryIs>");
            }

            writer.append("</Polygon>");
            writer.append("</Placemark>");
        }
        writer.append("</Document>");
        writer.append("</kml>");
    }

    /**
     * Create a boundary from the given way and append it to the writer.
     * @param osw Append to this writer.
     * @param way Create a boundary from this way.
     * @param data Access to osm data.
     * @return The writer.
     * @throws IncompleteBorderException Thrown when a node can not be found which is referenced by the given way.
     * @throws java.io.IOException Error writing to the writer.
     */
    private Writer appendBoundary(Writer osw, Way way, OSMData data)
            throws IncompleteBorderException, IOException {
        osw.append("<LinearRing><coordinates>");
        for (WayNode wayNode : way.getWayNodes()) {
            try {
                Node node = data.getNode(wayNode.getNodeId());
                osw.append(((Double) node.getLongitude()).toString()).append(",");
                osw.append(((Double) node.getLatitude()).toString()).append(" ");
            } catch(NoSuchIndexElementException e) {
                throw new IncompleteBorderException("Can not get node " + wayNode.getNodeId());
            }
        }
        osw.append("</coordinates></LinearRing>");
        return osw;
    }
}
