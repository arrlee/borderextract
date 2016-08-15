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
import org.junit.Ignore;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.store.IndexedObjectStore;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;

import java.util.Collections;
import java.util.Date;


/**
 * Create some test data and provide it to test classes.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
@Ignore
public class TestDataProvider {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(TestDataProvider.class.getName());

    /**
     * The node store with all nodes.
     */
    private IndexedObjectStore<Node> nodeStore;

    /**
     * The way store containing all ways.
     */
    private IndexedObjectStore<Way> wayStore;

    /**
     * A single border relation.
     */
    private Relation relation;

    public TestDataProvider() {
        createTestData();
    }

    /**
     * Create the test data.
     */
    private void createTestData() {
        // preparation
        nodeStore = new IndexedObjectStore<Node>(
                new SingleClassObjectSerializationFactory(Node.class), "junit-nodes");
        wayStore = new IndexedObjectStore<Way>(
                new SingleClassObjectSerializationFactory(Way.class),  "junit-ways");

        double[] nodeCoords = {
                47.482829, 8.295433,
                47.484622, 8.311698,
                47.477734, 8.321771,
                47.464903, 8.320621,
                47.462052, 8.297045,
                47.469074, 8.284794,
                47.474237, 8.278815,

                47.476120, 8.284164,
                47.474061, 8.288497,
                47.472109, 8.284217,

                47.479688, 8.302621,
                47.479861, 8.306419,
                47.477824, 8.306670,
                47.478041, 8.303481
        };

        for (int i = 0; i < nodeCoords.length; i +=2) {
            nodeStore.add(i / 2 + 1, new Node(i / 2 + 1, 1, new Date(), OsmUser.NONE, 1, nodeCoords[i], nodeCoords[i + 1]));
        }

        Way w1 = new Way(1, 1, new Date(), OsmUser.NONE, 1);
        w1.getWayNodes().add(new WayNode(1));
        w1.getWayNodes().add(new WayNode(2));
        w1.getWayNodes().add(new WayNode(3));
        w1.getWayNodes().add(new WayNode(4));

        Way w2 = new Way(2, 1, new Date(), OsmUser.NONE, 1);
        w2.getWayNodes().add(new WayNode(4));
        w2.getWayNodes().add(new WayNode(5));
        w2.getWayNodes().add(new WayNode(6));
        w2.getWayNodes().add(new WayNode(7));
        w2.getWayNodes().add(new WayNode(1));

        Way w3 = new Way(3, 1, new Date(), OsmUser.NONE, 1);
        w3.getWayNodes().add(new WayNode(7));
        w3.getWayNodes().add(new WayNode(8));
        w3.getWayNodes().add(new WayNode(9));
        w3.getWayNodes().add(new WayNode(10));

        Way w4 = new Way(4, 1, new Date(), OsmUser.NONE, 1);
        w4.getWayNodes().add(new WayNode(11));
        w4.getWayNodes().add(new WayNode(12));
        w4.getWayNodes().add(new WayNode(13));
        w4.getWayNodes().add(new WayNode(14));

        Way w5 = new Way(5, 1, new Date(), OsmUser.NONE, 1);

        Way w2rev = new Way(6, 1, new Date(), OsmUser.NONE, 1);
        w2rev.getWayNodes().addAll(w2.getWayNodes());
        Collections.reverse(w2rev.getWayNodes());

        wayStore.add(1, w1);
        wayStore.add(2, w2);
        wayStore.add(3, w3);
        wayStore.add(4, w4);
        wayStore.add(5, w5);
        wayStore.add(6, w2rev);

        nodeStore.complete();
        wayStore.complete();

        // create a border relation
        relation = new Relation(1, 1, new Date(), OsmUser.NONE, 1);
        relation.getTags().add(new Tag("name", "test name"));

        // --------------------------------------------------------------------------
        // big outer ring with two inner rings
        relation.getMembers().add(new RelationMember(1, EntityType.Way, "outer"));
        relation.getMembers().add(new RelationMember(2, EntityType.Way, "outer"));
        relation.getMembers().add(new RelationMember(3, EntityType.Way, "inner"));
        relation.getMembers().add(new RelationMember(4, EntityType.Way, "inner"));
        relation.getMembers().add(new RelationMember(5, EntityType.Way, "outer")); // empty
        relation.getMembers().add(new RelationMember(7, EntityType.Way, "outer")); // does not exist!

    }

    /**
     * Close all stores and thus delete the test data.
     */
    public void deleteTestData() {
        if (nodeStore != null) {
            nodeStore.release();
            nodeStore = null;
        }
        if (wayStore != null) {
            wayStore.release();
            wayStore = null;
        }
    }

    public IndexedObjectStore<Node> getNodeStore() {
        return nodeStore;
    }

    public IndexedObjectStore<Way> getWayStore() {
        return wayStore;
    }

    public void finalize() {
        deleteTestData();
    }

    /**
     * Get the test border relation.
     * @return The relation.
     */
    public Relation getRelation() {
        return relation;
    }
}
