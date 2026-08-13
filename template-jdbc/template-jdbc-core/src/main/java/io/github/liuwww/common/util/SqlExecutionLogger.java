package io.github.liuwww.common.util;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlExecutionLogger {

    private static final Logger logger = LoggerFactory.getLogger("SQL_EXECUTION");

    private static volatile long slowSqlThresholdMs = 1000;

    private static final Map<String, TableMetrics> tableMetrics = new ConcurrentHashMap<>();

    public static void setSlowSqlThresholdMs(long thresholdMs) {
        slowSqlThresholdMs = thresholdMs;
    }

    public static void logExecution(String sql, Object[] params, long costMs, boolean success) {
        if (logger.isDebugEnabled()) {
            logger.debug("SQL: {}, Params: {}, Cost: {}ms, Success: {}",
                    sql, params == null ? "[]" : Arrays.toString(params), costMs, success);
        }
        recordMetrics(sql, costMs, success);
    }

    public static void logSlowSql(String sql, Object[] params, long costMs) {
        if (logger.isWarnEnabled()) {
            logger.warn("Slow SQL detected: {}, Params: {}, Cost: {}ms",
                    sql, params == null ? "[]" : Arrays.toString(params), costMs);
        }
    }

    public static void logExecution(String sql, Object[] params, long costMs) {
        logExecution(sql, params, costMs, true);
        if (costMs > slowSqlThresholdMs) {
            logSlowSql(sql, params, costMs);
        }
    }

    private static void recordMetrics(String sql, long costMs, boolean success) {
        String tableName = extractTableName(sql);
        if (tableName == null) {
            return;
        }

        TableMetrics metrics = tableMetrics.computeIfAbsent(tableName, k -> new TableMetrics());

        metrics.totalCount.incrementAndGet();
        metrics.totalCostMs.addAndGet(costMs);
        if (!success) {
            metrics.errorCount.incrementAndGet();
        }
        if (costMs > slowSqlThresholdMs) {
            metrics.slowCount.incrementAndGet();
        }
    }

    private static String extractTableName(String sql) {
        if (sql == null || sql.isEmpty()) {
            return null;
        }
        String upperSql = sql.toUpperCase(java.util.Locale.ROOT).trim();
        if (upperSql.startsWith("SELECT")) {
            int fromIndex = upperSql.indexOf("FROM");
            if (fromIndex > 0) {
                int start = fromIndex + 5;
                int spaceIndex = upperSql.indexOf(' ', start);
                // 表名可能在 SQL 末尾（如 "select * from test_user"）
                int endIndex = spaceIndex > start ? spaceIndex : upperSql.length();
                if (endIndex > start) {
                    return sql.substring(start, endIndex).trim();
                }
            }
        } else if (upperSql.startsWith("INSERT INTO")) {
            int intoIndex = upperSql.indexOf("INTO");
            int spaceIndex = upperSql.indexOf(' ', intoIndex + 4);
            int parenIndex = upperSql.indexOf('(', intoIndex + 4);
            int endIndex = spaceIndex > 0 && parenIndex > 0 ? Math.min(spaceIndex, parenIndex)
                    : (spaceIndex > 0 ? spaceIndex : parenIndex);
            if (endIndex > intoIndex) {
                return sql.substring(intoIndex + 4, endIndex).trim();
            }
        } else if (upperSql.startsWith("UPDATE")) {
            int spaceIndex = upperSql.indexOf(' ', 7);
            int setIndex = upperSql.indexOf(" SET");
            int endIndex = setIndex > 0 ? Math.min(spaceIndex, setIndex) : spaceIndex;
            if (endIndex > 7) {
                return sql.substring(7, endIndex).trim();
            }
        } else if (upperSql.startsWith("DELETE FROM")) {
            int fromIndex = upperSql.indexOf("FROM") + 4;
            int spaceIndex = upperSql.indexOf(' ', fromIndex);
            int endIndex = spaceIndex > fromIndex ? spaceIndex : upperSql.length();
            if (endIndex > fromIndex) {
                return sql.substring(fromIndex, endIndex).trim();
            }
        }
        return null;
    }

    public static Map<String, TableMetrics> getTableMetrics() {
        return tableMetrics;
    }

    public static void clearMetrics() {
        tableMetrics.clear();
    }

    public static class TableMetrics {
        public final AtomicLong totalCount = new AtomicLong(0);
        public final AtomicLong totalCostMs = new AtomicLong(0);
        public final AtomicLong errorCount = new AtomicLong(0);
        public final AtomicLong slowCount = new AtomicLong(0);

        public long getTotalCount() {
            return totalCount.get();
        }

        public long getTotalCostMs() {
            return totalCostMs.get();
        }

        public long getErrorCount() {
            return errorCount.get();
        }

        public long getSlowCount() {
            return slowCount.get();
        }

        public long getAverageCostMs() {
            long count = totalCount.get();
            return count > 0 ? totalCostMs.get() / count : 0;
        }
    }
}
