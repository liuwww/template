package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.Row;

public class RowTest {

    @Test
    public void testRowSettersAndGetters() {
        Row row = new Row();
        row.setTableName("test_user");
        row.setIdName("id");
        row.setIdValue("123");
        row.setDbType(DbType.MYSQL);
        row.setIsEffective(true);

        assertEquals("test_user", row.getTableName());
        assertEquals("id", row.getIdName());
        assertEquals("123", row.getIdValue());
        assertEquals(DbType.MYSQL, row.getDbType());
        assertTrue(row.getIsEffective());
    }

    @Test
    public void testRowValueMap() {
        Row row = new Row();
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("name", "张三");
        valueMap.put("age", 25);
        row.setRowValueMap(valueMap);

        assertEquals(valueMap, row.getRowValueMap());
        assertEquals("张三", row.getRowValueMap().get("name"));
        assertEquals(25, row.getRowValueMap().get("age"));
    }

    @Test
    public void testAdditionalMap() {
        Row row = new Row();
        Map<String, Object> additionalMap = new HashMap<>();
        additionalMap.put("create_time", "2024-01-01");
        row.setAdditionalMap(additionalMap);

        assertEquals(additionalMap, row.getAdditionalMap());
    }

    @Test
    public void testIdValuesAndIdNames() {
        Row row = new Row();
        String[] idValues = {"1", "2"};
        String[] idNames = {"id1", "id2"};
        row.setIdValues(idValues);
        row.setIdNames(idNames);

        assertArrayEquals(idValues, row.getIdValues());
        assertArrayEquals(idNames, row.getIdNames());
    }

    @Test
    public void testToString() {
        Row row = new Row();
        row.setTableName("test_user");
        row.setIdName("id");
        row.setIdValue("123");
        row.setIsEffective(true);

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("name", "张三");
        row.setRowValueMap(valueMap);

        String str = row.toString();
        assertNotNull(str);
        assertTrue(str.contains("test_user"));
        assertTrue(str.contains("123"));
        assertTrue(str.contains("fieldCount=1"));
    }
}
