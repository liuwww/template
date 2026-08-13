package io.github.liuwww.db.monitor;

import java.util.HashMap;
import java.util.Map;

import io.github.liuwww.common.util.SqlExecutionLogger;
import io.github.liuwww.common.util.SqlExecutionLogger.TableMetrics;

/**
 * 基于 {@link SqlExecutionLogger} 的默认 {@link SqlMetrics} 实现
 * <p>
 * 通过 SqlExecutionLogger 内置的表级指标采集器提供统计数据。
 * 该实现是无状态的单例，所有指标由 SqlExecutionLogger 的静态字段维护。
 * </p>
 *
 * @author liuwww
 */
public class DefaultSqlMetrics implements SqlMetrics {

    private static final DefaultSqlMetrics INSTANCE = new DefaultSqlMetrics();

    /**
     * 获取单例实例
     *
     * @return 单例实例
     */
    public static DefaultSqlMetrics getInstance() {
        return INSTANCE;
    }

    @Override
    public void recordExecution(String sql, long costMs, boolean success) {
        SqlExecutionLogger.logExecution(sql, null, costMs, success);
    }

    @Override
    public long getTotalExecutionCount() {
        long total = 0;
        for (TableMetrics metrics : SqlExecutionLogger.getTableMetrics().values()) {
            total += metrics.getTotalCount();
        }
        return total;
    }

    @Override
    public long getTotalExecutionTime() {
        long total = 0;
        for (TableMetrics metrics : SqlExecutionLogger.getTableMetrics().values()) {
            total += metrics.getTotalCostMs();
        }
        return total;
    }

    @Override
    public long getSlowExecutionCount() {
        long total = 0;
        for (TableMetrics metrics : SqlExecutionLogger.getTableMetrics().values()) {
            total += metrics.getSlowCount();
        }
        return total;
    }

    @Override
    public long getErrorExecutionCount() {
        long total = 0;
        for (TableMetrics metrics : SqlExecutionLogger.getTableMetrics().values()) {
            total += metrics.getErrorCount();
        }
        return total;
    }

    @Override
    public Map<String, Long> getTableExecutionStats() {
        Map<String, Long> stats = new HashMap<>();
        for (Map.Entry<String, TableMetrics> entry : SqlExecutionLogger.getTableMetrics().entrySet()) {
            stats.put(entry.getKey(), entry.getValue().getTotalCount());
        }
        return stats;
    }

    @Override
    public void clear() {
        SqlExecutionLogger.clearMetrics();
    }
}
