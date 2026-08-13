package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import org.junit.Test;
import io.github.liuwww.common.util.StringUtil;

/**
 * StringUtil工具类测试
 */
public class StringUtilTest {

    @Test
    public void testGetFileSuffix() {
        assertEquals("txt", StringUtil.getFileSuffix("test.txt"));
        assertEquals("java", StringUtil.getFileSuffix("Test.java"));
        assertEquals("gz", StringUtil.getFileSuffix("archive.tar.gz"));
    }

    @Test
    public void testGetFileSuffixWithNull() {
        assertNull(StringUtil.getFileSuffix(null));
    }

    @Test
    public void testGetFileSuffixWithEmpty() {
        assertNull(StringUtil.getFileSuffix(""));
    }

    @Test
    public void testGetFileSuffixWithBlank() {
        assertNull(StringUtil.getFileSuffix("   "));
    }

    @Test
    public void testGetFileSuffixWithoutExtension() {
        assertEquals("", StringUtil.getFileSuffix("testfile"));
    }

    @Test
    public void testGetFileSuffixStartingWithDot() {
        assertEquals("", StringUtil.getFileSuffix(".txt"));
    }

    @Test
    public void testIsBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank("   "));
        assertFalse(StringUtil.isBlank("test"));
    }

    @Test
    public void testIsNotBlank() {
        assertFalse(StringUtil.isNotBlank(null));
        assertFalse(StringUtil.isNotBlank(""));
        assertFalse(StringUtil.isNotBlank("   "));
        assertTrue(StringUtil.isNotBlank("test"));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty("test"));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(StringUtil.isNotEmpty(null));
        assertFalse(StringUtil.isNotEmpty(""));
        assertTrue(StringUtil.isNotEmpty("test"));
    }

    @Test
    public void testTrim() {
        assertEquals("test", StringUtil.trim("  test  "));
        assertNull(StringUtil.trim(null));
    }

    @Test
    public void testEquals() {
        assertTrue(StringUtil.equals("test", "test"));
        assertFalse(StringUtil.equals("test", "other"));
        assertTrue(StringUtil.equals(null, null));
        assertFalse(StringUtil.equals(null, "test"));
    }

    @Test
    public void testCapitalize() {
        assertEquals("Test", StringUtil.capitalize("test"));
        assertEquals("Test", StringUtil.capitalize("Test"));
        assertEquals("TEST", StringUtil.capitalize("TEST"));
        assertEquals("Test", StringUtil.capitalize("test"));
        assertNull(StringUtil.capitalize(null));
        assertEquals("", StringUtil.capitalize(""));
    }

    @Test
    public void testCapitalizeWithChinese() {
        assertEquals("张三", StringUtil.capitalize("张三"));
    }

    @Test
    public void testEqualsIgnoreCase() {
        assertTrue(StringUtil.equalsIgnoreCase("test", "TEST"));
        assertTrue(StringUtil.equalsIgnoreCase("test", "Test"));
        assertFalse(StringUtil.equalsIgnoreCase("test", "other"));
        assertTrue(StringUtil.equalsIgnoreCase(null, null));
        assertFalse(StringUtil.equalsIgnoreCase(null, "test"));
    }

    @Test
    public void testDefaultIfBlank() {
        assertEquals("default", StringUtil.defaultIfBlank(null, "default"));
        assertEquals("default", StringUtil.defaultIfBlank("", "default"));
        assertEquals("default", StringUtil.defaultIfBlank("   ", "default"));
        assertEquals("test", StringUtil.defaultIfBlank("test", "default"));
    }

    @Test
    public void testDefaultIfEmpty() {
        assertEquals("default", StringUtil.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtil.defaultIfEmpty("", "default"));
        assertEquals("   ", StringUtil.defaultIfEmpty("   ", "default"));
        assertEquals("test", StringUtil.defaultIfEmpty("test", "default"));
    }
}
