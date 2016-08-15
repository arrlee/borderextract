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

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.store.IndexedObjectStoreReader;

import java.util.Date;


/**
 * Test class {@link MysqlBorderListener}.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
public class MysqlBorderListenerTest {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(MysqlBorderListenerTest.class.getName());

    /**
     * Holds test data.
     */
    private static TestDataProvider dataProvider;

    @BeforeClass
    public static void setUp() throws Exception {
        dataProvider = new TestDataProvider();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        dataProvider.deleteTestData();
    }

    /**
     * Test method {@link MysqlBorderListener#complete(OSMData, String)}.
     *
     * @throws Exception Test failed.
     */
    @Test
    public void testComplete() throws Exception {
        Relation relation = dataProvider.getRelation();

        IndexedObjectStoreReader<Node> nodeStoreReader = dataProvider.getNodeStore().createReader();
        IndexedObjectStoreReader<Way> wayStoreReader = dataProvider.getWayStore().createReader();

        OSMData osmData = new OSMData(nodeStoreReader, wayStoreReader);

        // test 1
        MysqlBorderListener listener = new MysqlBorderListener();
        boolean c = listener.borderRelationFound(relation, 8);

        listener.complete(osmData, "junit-kml-out");

        Assert.assertTrue(true);

        // cleanup
        nodeStoreReader.release();
        wayStoreReader.release();
    }
}
