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

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import javax.annotation.Nonnull;


/**
 * Easy access to tags of osm-entities.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
public class EntityHelper {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(EntityHelper.class.getName());

    /**
     * To detect a name tag and extract the language.
     */
    private static Pattern nameTagPattern = Pattern.compile("name(:([a-zA-Z]{2,3}))?");

    /**
     * Tag "name".
     */
    private static final String NAME_TAG = "name";

    /**
     * Get the value of a tag by key.
     * @return the tag-value for the given key
     * @param node the way to get the tag from
     * @param key the key of the tag
     */
    public static String getTag(final Entity node, final String key) {
        for (Tag tag : node.getTags()) {
            if (tag.getKey().equals(key)) {
                return tag.getValue();
            }
        }
        return null;
    }

    /**
     * Get the name of the given entity in the desired language(s) or in
     * the default language, if no other language matches.
     * @param entity    Get the name of this entity.
     * @param languages Desired languages. Can be <code>null</code>.
     * @return The name or <code>null</code>, if none was found.
     */
    public static String getName(@Nonnull final Entity entity, final List<String> languages) {
        String bestMatch = getTag(entity, NAME_TAG);
        int bestRank = Integer.MAX_VALUE;

        for (Tag tag : entity.getTags()) {
            Matcher nameMatcher = nameTagPattern.matcher(tag.getKey());
            if (nameMatcher.matches()) {
                if (Strings.isNullOrEmpty(bestMatch)) {
                    bestMatch = tag.getValue();
                }
                String lang = Strings.nullToEmpty(nameMatcher.group(2));
                for (int i = 0; languages != null && i < languages.size(); i++) {
                    if (lang.equalsIgnoreCase(languages.get(i)) && bestRank > i) {
                        bestMatch = tag.getValue();
                        bestRank = i;
                    }
                }
            }
        }

        return Strings.isNullOrEmpty(bestMatch)
                ? getTag(entity, "int_name")
                : bestMatch;
    }
}
