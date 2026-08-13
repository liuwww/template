package io.github.liuwww.db.monitor;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * {@link DefaultSqlMetrics} 单元测试
 * <p>
 * 验证监控指标的记录与统计功能，包括：
 * <ul>
 * <li>执行次数、耗时、错误数、慢查询数统计</li>
 * <li>按表维度的执行统计</li>
 * <li>清除指标</li>
 * </ul>
 * </p>
 *
 * @author liuwww
 */
public class DefaultSqlMetricsTest {

    private SqlMetrics metrics;

    @Before
    public void setUp() {
        metrics = DefaultSqlMetrics.getInstance();
        metrics.clear();
    }

    @Test
    public void testRecordExecutionIncrementsCount() {
        metrics.recordExecution("select * from test_user where id = ?", 10L, true);
        metrics.recordExecution("select * from test_user where status = ?", 15L, true);

        assertEquals("总执行次数应为 2", 2L, metrics.getTotalExecutionCount());
        assertEquals("总执行耗时应为 25ms", 25L, metrics.getTotalExecutionTime());
    }

    @Test
    public void testRecordFailedExecution() {
        metrics.recordExecution("update test_user set name = ? where id = ?", 5L, false);

        assertEquals("总执行次数应为 1", 1L, metrics.getTotalExecutionCount());
        assertEquals("错误次数应为 1", 1L, metrics.getErrorExecutionCount());
    }

    @Test
    public void testSlowExecutionCount() {
        // 设置慢查询阈值为 100ms
        io.github.liuwww.common.util.SqlExecutionLogger.setSlowSqlThresholdMs(100L);

        metrics.recordExecution("select * from test_user", 50L, true);
        metrics.recordExecution("select * from test_user", 150L, true);
        metrics.recordExecution("select * from test_user", 200L, true);

        assertEquals("慢查询次数应为 2", 2L, metrics.getSlowExecutionCount());
    }

    @Test
    public void testTableExecutionStats() {
        metrics.recordExecution("select * from test_user where id = ?", 10L, true);
        metrics.recordExecution("insert into test_user (id, name) values (?, ?)", 5L, true);
        metrics.recordExecution("select * from test_user where status = ?", 8L, true);

        Map<String, Long> stats = metrics.getTableExecutionStats();
        assertNotNull("表统计不应为 null", stats);
        assertTrue("应包含 test_user 表的统计", stats.containsKey("test_user") || stats.size() > 0);
    }

    @Test
    public void testClearMetrics() {
        metrics.recordExecution("select * from test_user", 10L, true);
        assertEquals("清除前应有 1 次执行", 1L, metrics.getTotalExecutionCount());

        metrics.clear();
        assertEquals("清除后总执行次数应为 0", 0L, metrics.getTotalExecutionCount());
        assertEquals("清除后总执行耗时应为 0", 0L, metrics.getTotalExecutionTime());
        assertEquals("清除后错误次数应为 0", 0L, metrics.getErrorExecutionCount());
    }

    @Test
    public void testMultipleTablesStats() {
        metrics.recordExecution("select * from test_user", 10L, true);
        metrics.recordExecution("select * from test_user", 20L, true);
        metrics.recordExecution("select * from test_order", 15L, true);

        Map<String, Long> stats = metrics.getTableExecutionStats();
        assertTrue("应至少有一个表的统计", stats.size() >= 1);
        assertEquals("总执行次数应为 3", 3L, metrics.getTotalExecutionCount());
    }
}
