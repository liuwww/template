package io.github.liuwww.db.executor;

import java.util.List;
import java.util.Map;

/**
 * 数据库执行器接口，用于解耦Spring JdbcTemplate依赖
 * 
 * @author liuwww
 * @date 2026-03-01
 */
public interface DatabaseExecutor {

    /**
     * 执行更新操作（INSERT, UPDATE, DELETE）
     * 
     * @param sql    SQL语句
     * @param params 参数数组
     * @return 受影响的行数
     */
    int update(String sql, Object[] params);

    /**
     * 批量执行更新操作
     * 
     * @param sql       SQL语句
     * @param batchArgs 批量参数列表
     * @return 每次操作受影响的行数数组
     */
    int[] batchUpdate(String sql, List<Object[]> batchArgs);

    /**
     * 查询返回对象列表
     * 
     * @param <T>    返回类型
     * @param sql    SQL语句
     * @param params 参数数组
     * @param clazz  结果对象类型
     * @return 对象列表
     */
    <T> List<T> query(String sql, Object[] params, Class<T> clazz);

    /**
     * 查询返回Map列表
     * 
     * @param sql    SQL语句
     * @param params 参数数组
     * @return Map列表，每行为一个Map
     */
    List<Map<String, Object>> queryForList(String sql, Object[] params);

    /**
     * 查询返回单个对象
     * 
     * @param <T>    返回类型
     * @param sql    SQL语句
     * @param params 参数数组
     * @param clazz  结果对象类型
     * @return 单个对象，如果没有结果则返回null
     */
    <T> T queryForObject(String sql, Object[] params, Class<T> clazz);

    /**
     * 查询返回单个Map
     * 
     * @param sql    SQL语句
     * @param params 参数数组
     * @return 单个Map，如果没有结果则返回null
     */
    Map<String, Object> queryForMap(String sql, Object[] params);

    /**
     * 执行查询返回结果数量
     * 
     * @param sql    SQL语句
     * @param params 参数数组
     * @return 结果数量
     */
    long queryCount(String sql, Object[] params);

    /**
     * 执行插入操作并返回自动生成的主键
     * <p>
     * 默认不支持，需要实现类覆盖（如 SpringDatabaseExecutor）。
     * </p>
     *
     * @param sql    SQL 语句
     * @param params 参数数组
     * @return 自动生成的主键值
     */
    default String insertWithAutoGenKey(String sql, Object[] params) {
        throw new UnsupportedOperationException(
                "当前 DatabaseExecutor 实现不支持自增主键: " + this.getClass().getName());
    }

    /**
     * 批量执行插入操作并返回自动生成的主键数组
     * <p>
     * 默认不支持，需要实现类覆盖（如 SpringDatabaseExecutor）。
     * </p>
     *
     * @param sql       SQL 语句
     * @param batchArgs 批量参数列表
     * @return 自动生成的主键值数组
     */
    default String[] batchInsertWithAutoGenKey(String sql, List<Object[]> batchArgs) {
        throw new UnsupportedOperationException(
                "当前 DatabaseExecutor 实现不支持批量自增主键: " + this.getClass().getName());
    }
}