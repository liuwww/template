package io.github.liuwww.common.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SqlExecutionLoggerTest {

    @Before
    public void setUp() {
        SqlExecutionLogger.clearMetrics();
        SqlExecutionLogger.setSlowSqlThresholdMs(100);
    }

    @After
    public void tearDown() {
        SqlExecutionLogger.clearMetrics();
    }

    @Test
    public void testLogExecution() {
        SqlExecutionLogger.logExecution("INSERT INTO test_table VALUES(1)", new Object[] {}, 50, true);
        assertFalse(SqlExecutionLogger.getTableMetrics().isEmpty());
    }

    @Test
    public void testLogSlowSql() {
        SqlExecutionLogger.logSlowSql("SELECT * FROM large_table", new Object[] {}, 200);
    }

    @Test
    public void testClearMetrics() {
        SqlExecutionLogger.logExecution("SELECT * FROM users", new Object[] {}, 50, true);
        SqlExecutionLogger.clearMetrics();
        assertTrue(SqlExecutionLogger.getTableMetrics().isEmpty());
    }

    @Test
    public void testSetSlowSqlThreshold() {
        SqlExecutionLogger.setSlowSqlThresholdMs(200);
    }

    @Test
    public void testLogExecutionWithNullParams() {
        SqlExecutionLogger.logExecution("SELECT 1", null, 10, true);
    }

    @Test
    public void testLogExecutionWithError() {
        SqlExecutionLogger.logExecution("SELECT * FROM users", new Object[] {}, 50, false);
    }

    @Test
    public void testTableMetricsStructure() {
        SqlExecutionLogger.TableMetrics metrics = new SqlExecutionLogger.TableMetrics();
        assertEquals(0, metrics.getTotalCount());
        assertEquals(0, metrics.getTotalCostMs());
        assertEquals(0, metrics.getErrorCount());
        assertEquals(0, metrics.getSlowCount());
        assertEquals(0, metrics.getAverageCostMs());
    }
}
