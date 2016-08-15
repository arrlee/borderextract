package com.generalbytes.osmosis.borderextract;

/*
Copyright (C) 2011, 2012 by GB General Bytes GmbH, Baden, Switzerland

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

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.store.NoSuchIndexElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Basic functionality useful for all listeners.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
public abstract class BaseBorderListener implements BorderListener {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(BaseBorderListener.class.getName());

    /**
     * A list of all encountered relations.
     */
    private Map<Long, Relation> relations = new HashMap<Long, Relation>();

    /**
     * Our own tag for transferring the role from the member to the way.
     */
    public static final String ROLE_TAG_NAME = "x-role";

    /**
     * List of desired languages.
     * @see BorderListener#setLanguages(java.util.List).
     */
    protected List<String> languages = null;

    /**
     * Called, when a relation was found that is an administrative border.
     * That clears and possible refills the already collected ways.
     *
     * @param relation   The found relation.
     * @param adminLevel The admin level of the border.
     */
    @Override
    public boolean borderRelationFound(Relation relation, int adminLevel) {
//        LOG.info("borderRelationFound " + relation);
        relations.put(relation.getId(), relation);
        return true;
    }

    /**
     * Return all relations which do not have a parent relation, therefore
     * are on top of the hierarchy.
     * @return A list of top relations.
     */
    protected Collection<Relation> getTopRelations() {

        Comparator<Relation> idComp = new Comparator<Relation>() {
            @Override
            public int compare(Relation o1, Relation o2) {
                if (o1.getId() == o2.getId()) {
                    return 0;
                }
                return o1.getId() < o2.getId() ? -1 : 1;
            }
        };

        // record parents for every relation
        final Multimap<Relation, Relation> relParents = TreeMultimap.create(idComp, idComp);
        for (Relation relation : relations.values()) {
            for (RelationMember relationMember : relation.getMembers()) {
                if (relationMember.getMemberType() == EntityType.Relation) {
                    Relation member = relations.get(relationMember.getMemberId());
                    if (member != null) {
                        relParents.put(member, relation);
                    }
                }
            }
        }

        // return all relations which do not have a parent relation
        return Collections2.filter(relations.values(), new Predicate<Relation>() {
            @Override
            public boolean apply(@Nullable Relation relation) {
                return relParents.get(relation).isEmpty();
            }
        });
    }

    /**
     * Create a polygon from a border relation.
     * @param relation The relation.
     * @param data Access to indexed osm data.
     * @return The polygon.
     * @throws IncompleteBorderException Thrown when a way can not be found which is referenced by the given relation.
     */
    protected Polygon getPolygon(Relation relation, OSMData data) throws IncompleteBorderException {
        String name = EntityHelper.getName(relation, languages);
        if (Strings.isNullOrEmpty(name)) {
            name = "rel-" + relation.getId();
        }

//        System.out.println("Constructing polygon for '" + name + "'");

        List<Way> sortedWays = sortContainedWays(relation, data);

        Polygon polygon = new Polygon(name);

        Way thisWay = null;
        Role role = Role.OUTER;

        for (Way way : sortedWays) {
            Role relRole = Role.toRole(EntityHelper.getTag(way, ROLE_TAG_NAME));
            List<WayNode> wayNodes = way.getWayNodes();
            WayNode lastWayNode = null, firstWayNode = null;
            if (thisWay != null) {
                lastWayNode = thisWay.getWayNodes().get(thisWay.getWayNodes().size() - 1);
                firstWayNode = wayNodes.get(0);
            }
            boolean wayContinues =
                    thisWay != null
                    && equals(firstWayNode, lastWayNode)
                    && relRole == role;

            if (thisWay == null || !wayContinues) {

                // remove start node, if it is the same as the end node
                if (thisWay != null && !thisWay.getWayNodes().isEmpty()) {
                    List<WayNode> thisWayNodes = thisWay.getWayNodes();
                    if (equals(thisWayNodes.get(0), thisWayNodes.get(thisWayNodes.size() - 1))) {
                        thisWayNodes.remove(0);
                    }
                }

                // create new way and add it to the right list
                thisWay = new Way(new CommonEntityData(way.getId(), way.getVersion(), way.getTimestamp(), way.getUser(), way.getChangesetId()));
                if (relRole == Role.OUTER) {
                    polygon.getOuterWays().add(thisWay);
                } else {
                    polygon.getInnerWays().add(thisWay);
                }
            }
            thisWay.getWayNodes().addAll(
                    wayContinues
                        ? wayNodes.subList(1, wayNodes.size())
                        : wayNodes);

            role = relRole;

        }

        return polygon;
    }

    /**
     * Resolve all ways which are directly or
     * indirectly (through other relations) referenced by the given relation.
     * @param relation The relation.
     * @param ways     All found ways are added to this list.
     * @param data     The OSM data.
     * @return The given ways-list.
     * @throws IncompleteBorderException Error while resolving ways or relations.
     */
    private List<Way> resolveWays(Relation relation, List<Way> ways, OSMData data) throws IncompleteBorderException {
        for (RelationMember member : relation.getMembers()) {
            try {
                switch (member.getMemberType()) {
                    case Relation:
                        Relation nestedRelation = relations.get(member.getMemberId());
                        if (nestedRelation != null) {
                            resolveWays(nestedRelation, ways, data);
                        }
                        break;
                    case Way:
                        Way way = data.getWay(member.getMemberId());
                        List<WayNode> wayNodes = way.getWayNodes();
                        if (!wayNodes.isEmpty()) {
                            way.getTags().add(new Tag(ROLE_TAG_NAME, Strings.nullToEmpty(member.getMemberRole())));
                            ways.add(way);
                        }
                        break;
                    default:
                        break;
                }
            } catch (NoSuchIndexElementException e) {
                throw new IncompleteBorderException("Can not find way " + member.getMemberId()
                        + " for relation " + relation.getId()
                        + " (" + EntityHelper.getName(relation, languages) + ")");
            }
        }
        return ways;
    }

    /**
     * Get all ways sorted such that subsequent ways form a closed polygon, if possible.
     * @param relation Sort ways referenced by this relation.
     * @param data  Access to osm-data.
     * @return All referenced ways sorted.
     * @throws IncompleteBorderException An way referenced by the relation is missing.
     */
    protected List<Way> sortContainedWays(Relation relation, OSMData data) throws IncompleteBorderException {

//        System.out.println("---------------------------------------------");
//        System.out.println("Sorting relation " + relation.getId());

        // first retrieve all ways
        List<Way> ways = new ArrayList<Way>();
        resolveWays(relation, ways, data);

//        System.out.println("Found " + ways.size() + " ways");

        // sort ways, such that they connect to a continuous path, if possible
        List<Way> sortedWays = new ArrayList<Way>(ways.size());
        if (!ways.isEmpty()) {
            sortedWays.add(ways.get(0));
            ways.remove(0);
        }
        while (!ways.isEmpty()) {
            Way lastWay = sortedWays.get(sortedWays.size() - 1);
            Role lastRole = Role.toRole(EntityHelper.getTag(lastWay, ROLE_TAG_NAME));
//            System.out.println("\nREL " + relation.getId() + ": Lastway=" + lastWay.getId() + ": role [" + lastRole + "], coord=" + lastNode.getLatitude() + " " + lastNode.getLongitude());

            boolean foundNext = false;
            WayNode lastNode = lastWay.getWayNodes().get(lastWay.getWayNodes().size() - 1);

            // search next way connecting to the end node with its start node
            for (int i = 0; !foundNext && i < ways.size(); i++) {
                Way way2 = ways.get(i);
                Role role2 = Role.toRole(EntityHelper.getTag(way2, ROLE_TAG_NAME));
//                System.out.println("Way2=" + way2.getId() + ": role [" + role2 + "]");

                if (role2 == lastRole) {
                    List<WayNode> wayNodes = way2.getWayNodes();
//                    System.out.println(" last node id=" + lastNode
//                            + ": way2 (" + wayNodes.size() + " nodes):"
//                            + wayNodes.get(0).getNodeId() + " - "
//                            + wayNodes.get(wayNodes.size() - 1).getNodeId());
                    if (equals(wayNodes.get(0), lastNode)) {
//                        System.out.println(" found first - Ways[last: " + lastWay.getId() + ", next: "
//                                + way2.getId() + "], Node " + lastNode);
                        foundNext = true;
                        sortedWays.add(way2);
                        ways.remove(i);
                    } else if (equals(wayNodes.get(wayNodes.size() - 1), lastNode)) {
//                        System.out.println(" found last - Ways[last: " + lastWay.getId() + ", next: "
//                                + way2.getWayNodes() + "], Node " + lastNode);
                        foundNext = true;
                        Collections.reverse(wayNodes);
                        sortedWays.add(way2);
                        ways.remove(i);
                    }
                }
            }

            if (!foundNext) {
                Way way2 = ways.remove(0);
//                System.out.println(" Did not find next border, adding anyways - Ways[last: " + lastWay.getId() + ", next: "
//                        + way2.getId() + "]");
                sortedWays.add(way2);
            }

//            Way addedWay = sortedWays.get(sortedWays.size() - 1);
//            System.out.println(" Added " + addedWay +  ": role [" + Role.toRole(EntityHelper.getTag(addedWay, ROLE_TAG_NAME)) + "]");
        }

//        System.out.println("-------------- Ready");
        return sortedWays;
    }

    /**
     * Set the languages to query for, when retrieving border names.
     *
     * @param languages An array of language codes, such as <code>{"en", "de"}</code>.
     */
    @Override
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    /**
     * Represents the role of a boundary way/relation.
     */
    private static enum Role {

        /**
         * Inner border, thus a hole.
         */
        INNER,

        /**
         * Outer border, thus the polygon.
         */
        OUTER;

        /**
         * Get the role by the tag value of the tag "role".
         * @param role The value of tag "role".
         * @return The parsed role.
         */
        @SuppressWarnings({"ConstantConditions"})
        public static Role toRole(@Nullable String role) {
            if (Strings.isNullOrEmpty(role)
                    || role.equalsIgnoreCase("outer")
                    || role.equalsIgnoreCase("exclave")) {
                return OUTER;
            }
            return INNER;
        }
    }

    /**
     * Test, whether two nodes equal.
     * @param a Node a.
     * @param b Node b.
     * @return <code >true</code>, if the node ids of the nodes equal, <code >false</code> otherwise.
     */
    private boolean equals(@Nonnull WayNode a, @Nonnull WayNode b) {
        return a.getNodeId() == b.getNodeId();
    }

    /**
     * Get all recorded relations.
     * @return A list of all relations.
     */
    public Collection<Relation> getRelations() {
        return relations.values();
    }
}
