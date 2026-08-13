package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.sql.Types;

import org.junit.Test;
import io.github.liuwww.db.sql.Column;
import io.github.liuwww.db.sql.SqlDataType;

public class ColumnTest {

    @Test
    public void testColumnSettersAndGetters() {
        Column column = new Column();
        column.setColumnName("user_id");
        column.setDataTypeName("VARCHAR");
        column.setDataType(Types.VARCHAR);
        column.setNullable(true);
        column.setRemarks("用户ID");
        column.setColumnSize(32);

        assertEquals("userId", column.getName());
        assertEquals("user_id", column.getColumnName());
        assertEquals(SqlDataType.VARCHAR, column.getSqlDataType());
        assertEquals(Types.VARCHAR, column.getDataType());
        assertTrue(column.isNullable());
        assertEquals("用户ID", column.getRemarks());
        assertEquals(32, column.getColumnSize());
    }

    @Test
    public void testSetColumnNameAutoConvertsToCamelCase() {
        Column column = new Column();
        column.setColumnName("user_id");

        assertEquals("user_id", column.getColumnName());
        assertEquals("userId", column.getName());
    }

    @Test
    public void testSetColumnNameComplex() {
        Column column = new Column();
        column.setColumnName("user_profile_data");

        assertEquals("userProfileData", column.getName());
    }

    @Test
    public void testIsNumberColumnBigInt() {
        Column column = new Column();
        column.setDataType(Types.BIGINT);
        assertTrue(column.isNumberColumn());
    }

    @Test
    public void testIsNumberColumnDecimal() {
        Column column = new Column();
        column.setDataType(Types.DECIMAL);
        assertTrue(column.isNumberColumn());
    }

    @Test
    public void testIsNumberColumnDouble() {
        Column column = new Column();
        column.setDataType(Types.DOUBLE);
        assertTrue(column.isNumberColumn());
    }

    @Test
    public void testIsNumberColumnFloat() {
        Column column = new Column();
        column.setDataType(Types.FLOAT);
        assertTrue(column.isNumberColumn());
    }

    @Test
    public void testIsNumberColumnInteger() {
        Column column = new Column();
        column.setDataType(Types.INTEGER);
        assertTrue(column.isNumberColumn());
    }

    @Test
    public void testIsNumberColumnNumeric() {
        Column column = new Column();
        column.setDataType(Types.NUMERIC);
        assertTrue(column.isNumberColumn());
    }

    @Test
    public void testIsNumberColumnVarchar() {
        Column column = new Column();
        column.setDataType(Types.VARCHAR);
        assertFalse(column.isNumberColumn());
    }

    @Test
    public void testIsNumberColumnDate() {
        Column column = new Column();
        column.setDataType(Types.DATE);
        assertFalse(column.isNumberColumn());
    }

    @Test
    public void testToString() {
        Column column = new Column();
        column.setColumnName("user_id");
        column.setDataType(Types.VARCHAR);
        column.setColumnSize(32);
        column.setNullable(false);

        String str = column.toString();
        assertNotNull(str);
        assertTrue(str.contains("user_id"));
        assertTrue(str.contains("12"));
    }

    @Test
    public void testDefaultValues() {
        Column column = new Column();

        assertNull(column.getName());
        assertNull(column.getColumnName());
        assertNull(column.getSqlDataType());
        assertEquals(0, column.getDataType());
        assertFalse(column.isNullable());
        assertNull(column.getRemarks());
        assertEquals(0, column.getColumnSize());
    }
}
