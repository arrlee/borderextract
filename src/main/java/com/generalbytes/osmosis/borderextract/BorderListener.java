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


import org.openstreetmap.osmosis.core.domain.v0_6.Relation;

import java.util.List;

/**
 * Listens for border relations.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
public interface BorderListener {

    /**
     * Called, when a relation was found that is an administrative border.
     * @param relation   The found relation.
     * @param adminLevel The admin level of the border.
     * @return <code>true</code>, if subsequent listeners should be called or if processing is stopped.
     */
    boolean borderRelationFound(Relation relation, int adminLevel);

    /**
     * Signals the listener, that processing is complete.
     * @param data           Provides access to osm data.
     * @param outputFileName Write result to this file.
     */
    void complete(OSMData data, String outputFileName);

    /**
     * Set the languages to query for, when retrieving border names.
     * @param languages An array of language codes, such as <code>{"en", "de"}</code>.
     */
    void setLanguages(List<String> languages);
}
