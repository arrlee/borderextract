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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.store.IndexedObjectStore;
import org.openstreetmap.osmosis.core.store.IndexedObjectStoreReader;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Collect all nodes and ways and calls the listeners after completion.
 * 
 * @author Andre Lison, GB General Bytes GmbH
 */
public class BorderExtractTask implements Sink, EntityProcessor {

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(BorderExtractTask.class.getName());


    /**
     * Store all ways indexed by their ids.
     */
    protected final IndexedObjectStore<Way> allWays;

    /**
     * Store all nodes indexed by their ids.
     */
    protected IndexedObjectStore<Node> allNodes;

    /**
     * Called, when a border is found and when the entire data has streamed through
     * this plugin (call to {@link #complete()}.
     */
    private List<BorderListener> listeners = new ArrayList<BorderListener>();

    /**
     * Output result to this file.
     */
    private String outputFileName;

    /**
     * Recognized admin levels.
     */
    private boolean[] adminLevels;

    /**
     * Create a new task and initialize the object stores.
     * @param outputFileName Write the result to this file.
     * @param adminLevels Recognized admin levels. The array contains a true at all indexes which
     *                      are recognized.
     */
    public BorderExtractTask(String outputFileName, boolean[] adminLevels) {
        this.outputFileName = outputFileName;
        this.adminLevels = adminLevels;

        allNodes = new IndexedObjectStore<Node>(new SingleClassObjectSerializationFactory(Node.class), "nodes");
		allWays  = new IndexedObjectStore<Way>(new SingleClassObjectSerializationFactory(Way.class), "ways");
    }


	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// Ask the entity container to invoke the appropriate processing method
		// for the entity type.
		entityContainer.process(this);
	}


	/**
     * Does nothing.
	 */
	public void process(BoundContainer boundContainer) {
	}

	/**
	 * Collect all nodes.
	 */
	public void process(NodeContainer container) {
		Node nd = container.getEntity();
		allNodes.add(nd.getId(), nd);
	}


	/**
	 * Collect all ways.
	 */
	public void process(WayContainer container) {
		Way way = container.getEntity();
        allWays.add(way.getId(), way);
	}


	/**
     * Collection all relations tagged with <code>boundary=administrative</code>.
	 * @param container Contains a relation.
	 */
	public void process(RelationContainer container) {
        Relation relation = container.getEntity();

        if ("administrative".equals(EntityHelper.getTag(relation, "boundary")) // recommended
            || "boundary".equals(EntityHelper.getTag(relation, "type")) // france and others
            || !Strings.isNullOrEmpty(EntityHelper.getTag(relation, "border_type"))) { // france and others
            String adminLevel = EntityHelper.getTag(relation, "admin_level");
            try {
                int adminLevelInt = Integer.parseInt(adminLevel);
                if (adminLevelInt >= 0 && adminLevelInt < adminLevels.length && adminLevels[adminLevelInt]) {
                    for (BorderListener listener : listeners) {
                        boolean ret = listener.borderRelationFound(relation, adminLevelInt);
                        if (!ret) {
                            break;
                        }
                    }
                }
            } catch (NumberFormatException nfe) {
                LOG.warning("Unrecognized admin level \""
                        + adminLevel
                        + "\" at " + relation
                        + ", name: \'" + EntityHelper.getTag(relation, "name") + "\'");
            }
        }
	}


	/**
	 * Complete the task, thus write out the borders.
	 */
	public void complete() {
        LOG.info("Plugin complete");
		// all nodes and ways are collected now
		allNodes.complete();
		allWays.complete();


        IndexedObjectStoreReader<Node> allNodesReader = allNodes.createReader();
        IndexedObjectStoreReader<Way> allWaysReader = allWays.createReader();

        // process borders
        OSMData osmData = new OSMData(allNodesReader, allWaysReader);
        for (BorderListener listener : listeners) {
            listener.complete(osmData, outputFileName);
        }


        allNodesReader.release();
        allWaysReader.release();
	}

    /**
	 * {@inheritDoc}
	 */
	public void release() {
		if (allNodes != null) {
			allNodes.release();
		}
		if (allWays != null) {
			allWays.release();
		}
	}

    /**
     * Add a border listener.
     * @param listener The listener.
     */
    public void addBorderListener(BorderListener listener) {
        listeners.add(listener);
    }

    @Override
    public void initialize(Map<String, Object> stringObjectMap) {
    }
}
