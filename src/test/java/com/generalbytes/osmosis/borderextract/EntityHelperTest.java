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

import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Test class {@link EntityHelper}.
 * @author Andre Lison, GB General Bytes GmbH, 2012
 */
public class EntityHelperTest {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(EntityHelperTest.class.getSimpleName());


    /**
     * Test method {@link ${SIMPLE_CLASS_NAME}#GetName()}.
     *
     * @throws Exception Test failed.
     */
    @Test
    public void testGetName() throws Exception {
        Entity entity = new Node(1, 1, new Date(), OsmUser.NONE, 1, 1.0, 2.0);

        // no name, no desired lang
        Assert.assertTrue(Strings.isNullOrEmpty(EntityHelper.getName(entity, null)));

        // default name, no desired lang
        entity.getTags().add(new Tag("name", "München"));
        Assert.assertEquals("München", EntityHelper.getName(entity, null));

        // german name, no desired lang
        entity.getTags().clear();
        entity.getTags().add(new Tag("name:de", "München"));
        Assert.assertEquals("München", EntityHelper.getName(entity, null));

        // german name, desired lang is english
        List<String> langs = Lists.newArrayList("en");
        Assert.assertEquals("München", EntityHelper.getName(entity, langs));

        // german + english name, desired lang is english
        entity.getTags().add(new Tag("name:en", "Munich"));
        Assert.assertEquals("Munich", EntityHelper.getName(entity, langs));

        // german + english name, desired lang is german and then english
        langs = Lists.newArrayList("de", "en");
        Assert.assertEquals("München", EntityHelper.getName(entity, langs));

        // german + english name, desired lang is english and then german
        langs = Lists.newArrayList("en", "de");
        Assert.assertEquals("Munich", EntityHelper.getName(entity, langs));

        // german + english name, desired lang is russian
        langs = Lists.newArrayList("ru");
        Assert.assertEquals("München", EntityHelper.getName(entity, langs));

        // german + english + latin name, desired lang is russian, then latin
        langs = Lists.newArrayList("ru", "la");
        entity.getTags().add(new Tag("name:la", "Monachium"));
        Assert.assertEquals("Monachium", EntityHelper.getName(entity, langs));

        // no name at all, but international name
        langs = Lists.newArrayList("ru", "la");
        entity.getTags().clear();
        entity.getTags().add(new Tag("int_name", "Munich"));
        Assert.assertEquals("Munich", EntityHelper.getName(entity, langs));

        // only default name, desired lang russian and latin
        entity.getTags().add(new Tag("name", "München"));
        Assert.assertEquals("München", EntityHelper.getName(entity, langs));

        // ISO 639-2 Code "bad", "Banda languages", desired lang russian and latin
        entity.getTags().add(new Tag("name", "?"));
        langs = Lists.newArrayList("ru", "la");
        Assert.assertEquals("München", EntityHelper.getName(entity, langs));

        // ISO 639-2 Code "bad", "Banda languages", desired lang "bad"
        entity.getTags().add(new Tag("name:bad", "?"));
        langs = Lists.newArrayList("bad");
        Assert.assertEquals("?", EntityHelper.getName(entity, langs));
    }
}
