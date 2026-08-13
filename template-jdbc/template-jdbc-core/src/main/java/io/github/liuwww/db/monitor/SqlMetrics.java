package io.github.liuwww.db.monitor;

import java.util.Map;

/**
 * SQL 执行监控指标接口
 * <p>
 * 提供 SQL 执行次数、耗时、慢查询、错误数以及按表维度的统计指标。
 * 实现可基于 {@link io.github.liuwww.common.util.SqlExecutionLogger} 的内置采集器，
 * 也可对接外部监控系统（如 Micrometer / Prometheus）。
 * </p>
 *
 * @author liuwww
 */
public interface SqlMetrics {

    /**
     * 记录一次 SQL 执行
     *
     * @param sql     SQL 语句
     * @param costMs  执行耗时（毫秒）
     * @param success 是否成功
     */
    void recordExecution(String sql, long costMs, boolean success);

    /**
     * 获取总执行次数
     *
     * @return 总执行次数
     */
    long getTotalExecutionCount();

    /**
     * 获取总执行耗时（毫秒）
     *
     * @return 总执行耗时
     */
    long getTotalExecutionTime();

    /**
     * 获取慢查询次数
     *
     * @return 慢查询次数
     */
    long getSlowExecutionCount();

    /**
     * 获取执行失败次数
     *
     * @return 失败次数
     */
    long getErrorExecutionCount();

    /**
     * 获取按表维度的执行统计
     *
     * @return 表名 -> 该表的执行次数
     */
    Map<String, Long> getTableExecutionStats();

    /**
     * 清除所有统计指标
     */
    void clear();
}
