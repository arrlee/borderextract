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

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.store.IndexedObjectStoreReader;

/**
 * Encapsulate the access to collected Openstreetmap data.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
public class OSMData {

    /**
     * Read nodes from object store.
     */
    private IndexedObjectStoreReader<Node> nodesReader;

    /**
     * Read ways from object store.
     */
    private IndexedObjectStoreReader<Way> waysReader;

    /**
     * Create a new osm data context.
     * @param nodesReader For retrieving a node by id.
     * @param waysReader  For retrieving a way by id.
     */
    public OSMData(IndexedObjectStoreReader<Node> nodesReader, IndexedObjectStoreReader<Way> waysReader) {
        this.nodesReader = nodesReader;
        this.waysReader = waysReader;
    }

    /**
     * Get a node by id.
     * @param nodeId The nodes id.
     * @return The node.
     */
    public Node getNode(long nodeId) {
        return nodesReader.get(nodeId);
    }

    /**
     * Get a way by id.
     * @param wayId The ways id.
     * @return The way.
     */
    public Way getWay(long wayId) {
        return waysReader.get(wayId);
    }

}
