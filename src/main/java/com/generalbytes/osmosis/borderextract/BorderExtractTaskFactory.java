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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parse command line arguments and initialize the plugin.
 * @author Andre Lison, GB General Bytes GmbH
 */
public class BorderExtractTaskFactory extends TaskManagerFactory {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(BorderExtractTaskFactory.class.getName());

    private static final String ARG_OUTPUT_TYPE = "type";
    private static final String ARG_OUTPUT_FILE = "file";
    private static final String ARG_LEVELS = "levels";
    private static final String ARG_LANGUAGE = "lang";
    private static final int MIN_LEVEL = 0;
    private static final int MAX_LEVEL = 11;

	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {

        Map<String, Object[]> listenerMap = new HashMap<String, Object[]>();
        listenerMap.put("kml",   new Object[]{KMLBorderListener.class,   "zip"});
        listenerMap.put("mysql", new Object[]{MysqlBorderListener.class, "sql"});
        listenerMap.put("poly",  new Object[]{PolyBorderListener.class,  "zip"});

        // output type
        String outputType = getStringArgument(taskConfig, ARG_OUTPUT_TYPE, "kml");
        Object[] listenerConf = listenerMap.get(outputType);
        if (listenerConf == null) {
            LOG.severe("Unsupported output type \"" + outputType + "\" found. Aborting.");
            return null;
        }

        // parse levels
        String levelsString = getStringArgument(taskConfig, ARG_LEVELS, "");
        boolean[] levels = parseLevels(levelsString);

        // output file name
        String outputFileName = getStringArgument(taskConfig, ARG_OUTPUT_FILE, "borderextract." + listenerConf[1]);

        // language
        String langs = getStringArgument(taskConfig, ARG_LANGUAGE, "");
        Splitter langSplitter = Splitter.on(",").omitEmptyStrings().trimResults();
        ArrayList<String> languages = Lists.newArrayList(langSplitter.split(langs));

        // create the task
        try {
            BorderExtractTask task = new BorderExtractTask(outputFileName, levels);
            BorderListener listener = (BorderListener) ((Class) listenerConf[0]).newInstance();
            listener.setLanguages(languages);
            task.addBorderListener(listener);

            return new SinkManager(taskConfig.getId(),
                    task,
                    taskConfig.getPipeArgs());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unable to instantiate the \"borderextract\" plugin. Error occurred", e);
        }
        return null;
	}

    /**
     * Extract the configured levels from the string.
     * @param levelsString The string as given by the user on the command line.
     * @return An array of boolean containing a true at each index that is a recognized admin level.
     */
    @SuppressWarnings({"PointlessArithmeticExpression"})
    @VisibleForTesting
    protected boolean[] parseLevels(String levelsString) {
        boolean[] levels = new boolean[MAX_LEVEL - MIN_LEVEL + 1];
        if (!Strings.isNullOrEmpty(levelsString)) {
            Iterable<String> levelList = Splitter.on(",").omitEmptyStrings().trimResults().split(levelsString);
            for (String level : levelList) {
                try {
                    int parsedLevel = Integer.parseInt(level);
                    if (parsedLevel < MIN_LEVEL || parsedLevel > MAX_LEVEL) {
                        LOG.warning("Admin level exceeds range: " + parsedLevel
                                + ". Allowed values: " + MIN_LEVEL + " to " + MAX_LEVEL);
                    } else {
                        levels[parsedLevel] = true;
                    }
                } catch (NumberFormatException nfe) {
                    LOG.warning("Can not parse level \"" + level + "\". Ignoring.");
                }
            }
        }
        return levels;
    }
}
