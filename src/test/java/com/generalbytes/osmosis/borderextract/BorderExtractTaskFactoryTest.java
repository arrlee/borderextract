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
import org.junit.Assert;
import org.junit.Test;


/**
 * Test class {@link BorderExtractTaskFactory}.
 * @author Andre Lison, GB General Bytes GmbH, 2011
 */
public class BorderExtractTaskFactoryTest extends BorderExtractTaskFactory {

    /**
     * Logger.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger LOG = Logger.getLogger(BorderExtractTaskFactoryTest.class.getName());

    /**
     * Test method {@link BorderExtractTaskFactory#parseLevels(String)}.
     */
    @Test
    public void testParseLevels() {
        Assert.assertArrayEquals(
                conv(new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false}),
                conv(parseLevels(""))
                );
        Assert.assertArrayEquals(
                conv(new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false}),
                conv(parseLevels("a"))
                );
        Assert.assertArrayEquals(
                conv(new boolean[]{false, true, false, false, false, false, false, false, false, false, false, false}),
                conv(parseLevels("1"))
                );
        Assert.assertArrayEquals(
                conv(new boolean[]{false, true, true, false, false, false, false, false, false, false, false, false}),
                conv(parseLevels("1,2"))
                );
        Assert.assertArrayEquals(
                conv(new boolean[]{false, true, true, false, false, false, false, false, false, false, false, false}),
                conv(parseLevels("1,2, 100"))
                );
        Assert.assertArrayEquals(
                conv(new boolean[]{false, true, true, false, false, true, false, false, false, false, false, false}),
                conv(parseLevels("1,2,...,5,100"))
                );
    }


    /**
     * Convert a boolean array to an integer array in order to pass it to assertArrayEquals.
     * Why is it missing in junit?
     */
    private int[] conv(boolean [] b) {
        int[] i = new int[b.length];
        for (int j = 0; j < b.length; j++) {
            i[j] = b[j] ? 1 : 0;
        }
        return i;
    }
}
