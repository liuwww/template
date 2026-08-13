package io.github.liuwww.common.util;

import static org.junit.Assert.*;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

public class CaseInsensitiveMapTest {

    @Test
    public void testPutAndGet() {
        CaseInsensitiveMap<String> map = new CaseInsensitiveMap<>();
        map.put("Name", "John");
        map.put("NAME", "Jane");
        map.put("name", "Bob");

        assertEquals("Bob", map.get("name"));
        assertEquals("Bob", map.get("NAME"));
        assertEquals("Bob", map.get("Name"));
    }

    @Test
    public void testContainsKey() {
        CaseInsensitiveMap<String> map = new CaseInsensitiveMap<>();
        map.put("Test", "value");

        assertTrue(map.containsKey("test"));
        assertTrue(map.containsKey("TEST"));
        assertTrue(map.containsKey("Test"));
        assertFalse(map.containsKey("other"));
        assertFalse(map.containsKey(null));
    }

    @Test
    public void testRemove() {
        CaseInsensitiveMap<String> map = new CaseInsensitiveMap<>();
        map.put("Key", "value");

        assertEquals("value", map.remove("KEY"));
        assertFalse(map.containsKey("key"));
    }

    @Test
    public void testPutAll() {
        CaseInsensitiveMap<String> map = new CaseInsensitiveMap<>();
        Map<String, String> toAdd = new HashMap<>();
        toAdd.put("Name", "John");
        toAdd.put("Age", "30");

        map.putAll(toAdd);

        assertEquals("John", map.get("name"));
        assertEquals("30", map.get("age"));
    }

    @Test
    public void testNullKey() {
        CaseInsensitiveMap<String> map = new CaseInsensitiveMap<>();
        map.put(null, "nullValue");

        assertEquals("nullValue", map.get(null));
        assertTrue(map.containsKey(null));
    }

    @Test
    public void testOverwrite() {
        CaseInsensitiveMap<String> map = new CaseInsensitiveMap<>();
        map.put("key", "value1");
        map.put("KEY", "value2");

        assertEquals("value2", map.get("key"));
        assertEquals(1, map.size());
    }
}
