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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Test class {@link KMLBorderListener}.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
public class BaseBorderListenerTest extends BaseBorderListener {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(BaseBorderListenerTest.class.getName());

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
     * Test method {@link KMLBorderListener#borderRelationFound(Relation, int)}
     *
     * @throws Exception Test failed.
     */
    @Test
    public void testBorderRelationFound() throws Exception {

        Relation relation = new Relation(1, 1, new Date(), OsmUser.NONE, 1);
        relation.getTags().add(new Tag("name", "test name"));

        // --------------------------------------------------------------------------
        // big outer ring with two inner rings
        relation.getMembers().add(new RelationMember(1, EntityType.Way, "outer"));
        relation.getMembers().add(new RelationMember(2, EntityType.Way, "outer"));
        relation.getMembers().add(new RelationMember(3, EntityType.Way, "inner"));
        relation.getMembers().add(new RelationMember(4, EntityType.Way, "inner"));
        relation.getMembers().add(new RelationMember(5, EntityType.Way, "outer")); // empty

        IndexedObjectStoreReader<Node> nodeStoreReader = dataProvider.getNodeStore().createReader();
        IndexedObjectStoreReader<Way> wayStoreReader = dataProvider.getWayStore().createReader();


        OSMData osmData = new OSMData(nodeStoreReader, wayStoreReader);

        // test 1
        boolean c = this.borderRelationFound(relation, 8);
        Assert.assertEquals(1, getRelations().size());
        Polygon p = getPolygon(relation, osmData);

        Assert.assertTrue(c);
        Assert.assertEquals("test name", p.getName());
        Assert.assertNotNull(p.getInnerWays());
        Assert.assertNotNull(p.getOuterWays());
        Assert.assertEquals(1, p.getOuterWays().size());
        Assert.assertEquals(2, p.getInnerWays().size());
        Assert.assertEquals(7, p.getOuterWays().get(0).getWayNodes().size());
        Assert.assertEquals(4, p.getInnerWays().get(0).getWayNodes().size());
        Assert.assertEquals(4, p.getInnerWays().get(1).getWayNodes().size());


        // --------------------------------------------------------------------------
        // test with nested relations
        Relation parentRelation = new Relation(100, 1, new Date(), OsmUser.NONE, 1);
        parentRelation.getMembers().add(new RelationMember(1, EntityType.Relation, "outer"));
        parentRelation.getTags().add(new Tag("name", "parent relation"));
        p = getPolygon(parentRelation, osmData);

        Assert.assertEquals("parent relation", p.getName());
        Assert.assertNotNull(p.getInnerWays());
        Assert.assertNotNull(p.getOuterWays());
        Assert.assertEquals(1, p.getOuterWays().size());
        Assert.assertEquals(2, p.getInnerWays().size());
        Assert.assertEquals(7, p.getOuterWays().get(0).getWayNodes().size());
        Assert.assertEquals(4, p.getInnerWays().get(0).getWayNodes().size());
        Assert.assertEquals(4, p.getInnerWays().get(1).getWayNodes().size());

        this.complete(osmData, "junit-output");

        // --------------------------------------------------------------------------
        // test with non-existing way
        relation.getMembers().add(new RelationMember(7, EntityType.Way, "outer")); // does not exist!
        getRelations().clear();
        try {
            getPolygon(relation, osmData);
            Assert.fail("Exception expected");
        } catch (IncompleteBorderException e) {
        }

        // --------------------------------------------------------------------------
        // test with ways in the wrong order
        getRelations().clear();
        relation.getMembers().clear();

        relation.getMembers().add(new RelationMember(2, EntityType.Way, "outer")); // 2 before 1!
        relation.getMembers().add(new RelationMember(1, EntityType.Way, "outer"));
        relation.getMembers().add(new RelationMember(3, EntityType.Way, "inner"));
        relation.getMembers().add(new RelationMember(4, EntityType.Way, "inner"));
        relation.getMembers().add(new RelationMember(5, EntityType.Way, "outer")); // empty

        this.complete(osmData, "junit-output");

        // test 2
        c = this.borderRelationFound(relation, 8);
        Assert.assertEquals(1, getRelations().size());
        p = getPolygon(relation, osmData);

        Assert.assertTrue(c);
        Assert.assertEquals("test name", p.getName());
        Assert.assertNotNull(p.getInnerWays());
        Assert.assertNotNull(p.getOuterWays());
        Assert.assertEquals(1, p.getOuterWays().size());
        Assert.assertEquals(2, p.getInnerWays().size());
        Assert.assertEquals(7, p.getOuterWays().get(0).getWayNodes().size());
        Assert.assertEquals(4, p.getInnerWays().get(0).getWayNodes().size());
        Assert.assertEquals(4, p.getInnerWays().get(1).getWayNodes().size());

        // --------------------------------------------------------------------------
        // test with way2 reversed
        getRelations().clear();
        relation.getMembers().clear();

        relation.getMembers().add(new RelationMember(1, EntityType.Way, "outer"));
        relation.getMembers().add(new RelationMember(6, EntityType.Way, "outer")); // reversed node order!
        relation.getMembers().add(new RelationMember(3, EntityType.Way, "inner"));
        relation.getMembers().add(new RelationMember(4, EntityType.Way, "inner"));
        relation.getMembers().add(new RelationMember(5, EntityType.Way, "outer")); // empty

        this.complete(osmData, "junit-output");

        // test 3
        c = this.borderRelationFound(relation, 8);
        Assert.assertEquals(1, getRelations().size());
        p = getPolygon(relation, osmData);

        Assert.assertTrue(c);
        Assert.assertEquals("test name", p.getName());
        Assert.assertNotNull(p.getInnerWays());
        Assert.assertNotNull(p.getOuterWays());
        Assert.assertEquals(1, p.getOuterWays().size());
        Assert.assertEquals(2, p.getInnerWays().size());
        Assert.assertEquals(7, p.getOuterWays().get(0).getWayNodes().size());
        Assert.assertEquals(4, p.getInnerWays().get(0).getWayNodes().size());
        Assert.assertEquals(4, p.getInnerWays().get(1).getWayNodes().size());

        // cleanup
        nodeStoreReader.release();
        wayStoreReader.release();
    }

    @Test
    public void testGetTopRelations() {

        Relation relation1 = new Relation(1, 1, new Date(), OsmUser.NONE, 1);
        Relation relation2 = new Relation(2, 1, new Date(), OsmUser.NONE, 1);
        Relation relation3 = new Relation(3, 1, new Date(), OsmUser.NONE, 1);
        Relation relation4 = new Relation(4, 1, new Date(), OsmUser.NONE, 1);
        Relation relation5 = new Relation(5, 1, new Date(), OsmUser.NONE, 1);
        Relation relation6 = new Relation(6, 1, new Date(), OsmUser.NONE, 1);

        relation2.getMembers().add(new RelationMember(3, EntityType.Relation, ""));
        relation2.getMembers().add(new RelationMember(4, EntityType.Relation, ""));

        relation4.getMembers().add(new RelationMember(5, EntityType.Relation, ""));

        relation6.getMembers().add(new RelationMember(2, EntityType.Relation, ""));

        relation6.getMembers().add(new RelationMember(100, EntityType.Relation, "")); // non-existing relation

        // only top relations are relation1 and relation6
        this.borderRelationFound(relation1, 2);
        this.borderRelationFound(relation2, 2);
        this.borderRelationFound(relation3, 2);
        this.borderRelationFound(relation4, 2);
        this.borderRelationFound(relation5, 2);
        this.borderRelationFound(relation6, 2);

        List<Relation> topRelations = new ArrayList<Relation>(this.getTopRelations());
        Collections.sort(topRelations, new Comparator<Relation>() {
            @Override
            public int compare(Relation r1, Relation r2) {
                return (int) (r1.getId() - r2.getId());
            }
        });

        Assert.assertEquals(2, topRelations.size());
        Assert.assertEquals(1, topRelations.get(0).getId());
        Assert.assertEquals(6, topRelations.get(1).getId());

    }

    /**
     * Signals the listener, that processing is complete.
     *
     * @param data Provides access to osm data.
     */
    @Override
    public void complete(OSMData data, String outputFileName) {
    }
}
