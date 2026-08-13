package io.github.liuwww.common.execption;

import static org.junit.Assert.*;

import org.junit.Test;

public class DbExceptionTest {

    @Test
    public void testDefaultConstructor() {
        DbException ex = new DbException();
        assertNull(ex.getMessage());
    }

    @Test
    public void testMessageConstructor() {
        DbException ex = new DbException("test message");
        assertEquals("test message", ex.getMessage());
    }

    @Test
    public void testThrowableConstructor() {
        RuntimeException cause = new RuntimeException("cause");
        DbException ex = new DbException(cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testErrorCodeConstructor() {
        DbException ex = new DbException("ERR001", "error message");
        assertEquals("ERR001", ex.getErrorCode());
        assertEquals("error message", ex.getMessage());
    }

    @Test
    public void testWithSql() {
        DbException ex = new DbException("test");
        ex.withSql("SELECT * FROM test", new Object[]{"param1", 123});

        assertEquals("SELECT * FROM test", ex.getSql());
        assertArrayEquals(new Object[]{"param1", 123}, ex.getParams());
    }

    @Test
    public void testWithTable() {
        DbException ex = new DbException("test");
        ex.withTable("users");

        assertEquals("users", ex.getTableName());
    }

    @Test
    public void testWithErrorCode() {
        DbException ex = new DbException("test");
        ex.withErrorCode("ERR002");

        assertEquals("ERR002", ex.getErrorCode());
    }

    @Test
    public void testToStringWithAllFields() {
        DbException ex = new DbException("ERR001", "test error");
        ex.withSql("SELECT * FROM users", new Object[]{"id", 1});
        ex.withTable("users");

        String str = ex.toString();
        assertTrue(str.contains("[ERR001]"));
        assertTrue(str.contains("test error"));
        assertTrue(str.contains("table: users"));
        assertTrue(str.contains("sql: SELECT * FROM users"));
        assertTrue(str.contains("params:"));
    }

    @Test
    public void testToStringWithCause() {
        RuntimeException cause = new RuntimeException("cause message");
        DbException ex = new DbException("error", cause);

        String str = ex.toString();
        assertTrue(str.contains("cause: cause message"));
    }

    @Test
    public void testFluentApi() {
        String result = new DbException("ERR001", "base error")
                .withSql("SELECT 1", null)
                .withTable("test")
                .withErrorCode("ERR002")
                .getErrorCode();

        assertEquals("ERR002", result);
    }
}
