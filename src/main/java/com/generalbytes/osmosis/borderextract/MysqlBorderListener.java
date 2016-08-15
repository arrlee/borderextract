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


import java.util.logging.*;
import com.google.common.base.Strings;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.store.NoSuchIndexElementException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;


/**
 * Create SQL code to insert the found border polygons into a mysql-spatial table.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
public class MysqlBorderListener extends KMLBorderListener {

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(MysqlBorderListener.class.getName());

    /**
     * Create the sql files from polygons.
     */
    @Override
    public void complete(OSMData data, String outputFileName) {
        LOG.info("complete");
        Writer writer = null;
        try {
            writer = new FileWriter(outputFileName);
            writeCreateTable(writer);
            for (Relation relation : getTopRelations()) {
                writeSqlForRelation(data, writer, relation);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error occurred", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Error occurred", e);
                }
            }
        }
    }

    /**
     * Write the table-create-statement to the given writer.
     * @param writer Write to this writer.
     * @throws IOException Error while writing to writer.
     */
    private void writeCreateTable(Writer writer) throws IOException {
        writer.write("CREATE TABLE IF NOT EXISTS Borders ("
            + "Name VARCHAR(255),"
            + "Admin_Level TINYINT NOT NULL,"
            + "Border GEOMETRY NOT NULL,"
            + "SPATIAL INDEX(Border)"
          + ") ENGINE=MyISAM;");
    }

    /**
     * Write the sql for a single border-relation to the given writer.
     * @param data          Access to OSM-data.
     * @param writer        Write the SQL to this writer.
     * @param relation      The relation to transform and serialize.
     * @throws IOException Error while writing.
     */
    private void writeSqlForRelation(OSMData data, Writer writer, Relation relation) throws IOException {
        String adminLevelStr = EntityHelper.getTag(relation, "admin_level");
        int adminLevelInt = Strings.isNullOrEmpty(adminLevelStr) ? 0 : Integer.parseInt(adminLevelStr);

        String name = EntityHelper.getName(relation, languages);
        if (Strings.isNullOrEmpty(name)) {
            name = "empty";
        } else {
            name = name.replaceAll("'", "\"");
        }

        try {
            Polygon polygon = getPolygon(relation, data);

            List<Way> ways = new ArrayList<Way>(polygon.getOuterWays().size() + polygon.getInnerWays().size());
            ways.addAll(polygon.getOuterWays());
            ways.addAll(polygon.getInnerWays());

            writer.append("INSERT INTO Borders (Name, Admin_Level, Border) VALUES ('")
                    .append(name).append("', ")
                    .append(Integer.toString(adminLevelInt)).append(", ")
                    .append("GeomFromText('MULTIPOLYGON");

            writer.append("("); // Multipolygon

            boolean hasPreviousWay = false;
            for (Way way : ways) {
                if (hasPreviousWay) {
                    writer.append(", ");
                }
                writer.append("((");
                boolean previousCoord = false;
                Node firstNode = null;
                for (WayNode wayNode : way.getWayNodes()) {
                    try {
                        Node node = data.getNode(wayNode.getNodeId());
                        if (!previousCoord) {
                            firstNode = node;
                        }
                        writeNode(writer, previousCoord, node);
                        previousCoord = true;
                    } catch (NoSuchIndexElementException e) {
                        LOG.warning("Can not get node " + wayNode.getNodeId());
                    }
                }
                if (firstNode != null) {
                    writeNode(writer, true, firstNode);
                }
                writer.append("))"); // polygon
                hasPreviousWay = true;
            }
            writer.append(")"); // Multipolygon
            writer.append("'));"); // GeomFromText + Values
            writer.append('\n');
        } catch (IncompleteBorderException e) {
            LOG.log(Level.SEVERE, "Skipping border of \"" + name + "\"(" + adminLevelInt + ") due to error", e);
        }
    }

    /**
     * Write a node as polygon point to a writer.
     * @param writer        Write to this writer.
     * @param previousCoord If there is a previous coordinate in the same polygon
     *                          (<code>true</code>) or if it is the first coordinate (<code>false</code>).
     * @param node          Write this node.
     * @throws IOException Error writing to the writer.
     */
    private void writeNode(Writer writer, boolean previousCoord, Node node) throws IOException {
        if (previousCoord) {
            writer.append(", ");
        }
        writer.write(Double.toString(node.getLongitude()));
        writer.append(' ');
        writer.write(Double.toString(node.getLatitude()));
    }
}
