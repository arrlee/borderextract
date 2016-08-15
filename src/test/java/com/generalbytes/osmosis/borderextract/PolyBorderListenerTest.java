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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.store.IndexedObjectStoreReader;

import java.io.StringWriter;


/**
 * Test {@link PolyBorderListener}.
 * @author Andre Lison, GB General Bytes GmbH, 2012
 */
public class PolyBorderListenerTest {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(PolyBorderListenerTest.class.getName());


    /**
     * Test method {@link PolyBorderListener#writePolygon(OSMData, Polygon, java.io.Writer)}.
     * @throws Exception Test failed.
     */
    @Test
    public void testWritePolygon() throws Exception {

        TestDataProvider testDataProvider = new TestDataProvider();
        IndexedObjectStoreReader<Node> nodesReader = testDataProvider.getNodeStore().createReader();
        IndexedObjectStoreReader<Way> waysReader = testDataProvider.getWayStore().createReader();

        try {

            // Preparation
            OSMData osmData = new OSMData(nodesReader, waysReader);

            BaseBorderListener baseBorderListener = new BaseBorderListener() {
                @Override
                public void complete(OSMData data, String outputFileName) {
                }
            };
            PolyBorderListener listener = new PolyBorderListener();
            Relation relation = testDataProvider.getRelation();
            Polygon polygon;

            relation.getMembers().remove(5); // remove non-existing way-member
            polygon = baseBorderListener.getPolygon(relation, osmData);
            StringWriter writer = new StringWriter();

            // test execution
            listener.writePolygon(osmData, polygon, writer);
            writer.close();

            // verify
            String expected =
                    "test name\n" +
                    "1\n" +
                    "\t+8.311698E+00 +4.748462E+01\n" +
                    "\t+8.321771E+00 +4.747773E+01\n" +
                    "\t+8.320621E+00 +4.746490E+01\n" +
                    "\t+8.297045E+00 +4.746205E+01\n" +
                    "\t+8.284794E+00 +4.746907E+01\n" +
                    "\t+8.278815E+00 +4.747424E+01\n" +
                    "\t+8.295433E+00 +4.748283E+01\n" +
                    "END\n" +
                    "!2\n" +
                    "\t+8.278815E+00 +4.747424E+01\n" +
                    "\t+8.284164E+00 +4.747612E+01\n" +
                    "\t+8.288497E+00 +4.747406E+01\n" +
                    "\t+8.284217E+00 +4.747211E+01\n" +
                    "END\n" +
                    "!3\n" +
                    "\t+8.302621E+00 +4.747969E+01\n" +
                    "\t+8.306419E+00 +4.747986E+01\n" +
                    "\t+8.306670E+00 +4.747782E+01\n" +
                    "\t+8.303481E+00 +4.747804E+01\n" +
                    "END\n" +
                    "END\n";
            Assert.assertEquals(expected, writer.toString());

        } finally {
            nodesReader.release();
            waysReader.release();
            testDataProvider.getNodeStore().release();
            testDataProvider.getWayStore().release();
        }
    }
}
