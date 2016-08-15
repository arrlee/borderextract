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

import org.openstreetmap.osmosis.core.domain.v0_6.Relation;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Marshal multiple polygons into a single zip file.
 * @author Andre Lison, GB General Bytes GmbH, 2012
 */
public abstract class ZipFileBorderListener extends BaseBorderListener {
    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(ZipFileBorderListener.class.getName());

    /**
     * Marshal collected border data to the output file, which is a zip file.
     */
    @Override
    public void complete(OSMData data, String outputFileName) {
        LOG.info("complete");
        ZipOutputStream zout = null;
        try {
            zout = new ZipOutputStream(new FileOutputStream(outputFileName));
            for (Relation relation : getTopRelations()) {
                writeRelation(data, zout, relation);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error writing output file", e);
        } finally {
            if (zout != null) {
                try {
                    zout.close();
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Error occurred", e);
                }
            }
        }
    }

    /**
     * Marshal a single relation.
     *
     * @param data     Access to OSM-Data.
     * @param zout     Write to this zip file.
     * @param relation The relation to serialize.
     * @throws java.io.IOException Error writing to zip file.
     */
    void writeRelation(OSMData data, ZipOutputStream zout, Relation relation) throws IOException {
        try {
            Polygon polygon = getPolygon(relation, data);

            // create zip entry
            String zipEntryName = polygon.getName().replaceAll("[\\\\/]", "-")
                    + "_" + relation.getId()
                    + "." + getEntrySuffix();
            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            zout.putNextEntry(zipEntry);
            OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(zout));

            // Export the polygon.
            getExporter().writePolygon(data, polygon, writer);

            writer.flush();
            zout.closeEntry();
        } catch (IncompleteBorderException e) {
            String name = EntityHelper.getName(relation, languages);
            LOG.log(Level.SEVERE, "Skipping border of \"" + name + "\" due to error", e);
        }
    }

    /**
     * Get the export to use for marshalling a polygon.
     * @return The exporter.
     */
    public abstract PolygonExporter getExporter();

    /**
     * Get the file suffix to use for a single zip file entry.
     * @return The suffix without the leading point.
     */
    public abstract String getEntrySuffix();
}