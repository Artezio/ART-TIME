package com.artezio.arttime.utils;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringUtilTest {

    @Test
    public void testSplitByComma() {
        String code1 = "code1";
        String code2 = "code2";
        String codeToSplit = code1 + "," + code2;

        List<String> actual = StringUtil.splitByComma(codeToSplit);

        assertEquals(2, actual.size());
        assertTrue(actual.contains(code1));
        assertTrue(actual.contains(code2));
    }

    @Test
    public void testSplitByComma_spaceInside() {
        String code1 = "code1";
        String code2 = "code2";
        String codeToSplit = code1 + "  ,  " + code2;

        List<String> actual = StringUtil.splitByComma(codeToSplit);

        assertEquals(2, actual.size());
        assertTrue(actual.contains(code1));
        assertTrue(actual.contains(code2));
    }

}
