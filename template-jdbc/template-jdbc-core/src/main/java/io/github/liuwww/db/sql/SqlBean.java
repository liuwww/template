package io.github.liuwww.db.sql;

import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.executor.DatabaseExecutor;

/**
 * SQL 数据载体接口
 * <p>
 * 封装 SQL 语句、参数、数据库类型、表元数据等信息，
 * 用于在 DAO 层和执行器之间传递 SQL 执行所需的完整信息。
 * </p>
 * 
 * @author liuwww
 * @since 2017-05-11
 */
public interface SqlBean {

    /**
     * 获取数据库执行器
     *
     * @return 数据库执行器
     */
    DatabaseExecutor getDatabaseExecutor();

    /**
     * 设置数据库执行器
     *
     * @param databaseExecutor 数据库执行器
     */
    void setDatabaseExecutor(DatabaseExecutor databaseExecutor);

    /**
     * 获取 SQL 语句
     *
     * @return SQL 语句，使用 ? 作为参数占位符
     */
    String getSql();

    /**
     * 设置 SQL 语句
     *
     * @param sql SQL 语句
     */
    void setSql(String sql);

    /**
     * 获取参数数组
     *
     * @return 参数数组，按顺序对应 SQL 中的 ? 占位符
     */
    Object[] getParams();

    /**
     * 设置参数数组
     *
     * @param params 参数数组
     */
    void setParams(Object[] params);

    /**
     * 获取排序表达式
     *
     * @return 排序表达式，如 "create_time DESC" 或 "name ASC, age DESC"
     */
    String getOrderBy();

    /**
     * 设置排序表达式
     *
     * @param orderBy 排序表达式
     */
    void setOrderBy(String orderBy);

    /**
     * 获取数据库类型
     *
     * @return 数据库类型
     */
    DbType getDbType();

    /**
     * 设置数据库类型
     *
     * @param dbType 数据库类型
     */
    void setDbType(DbType dbType);

    /**
     * 获取涉及的表名数组
     *
     * @return 表名数组
     */
    String[] getTables();

    /**
     * 设置涉及的表名数组
     *
     * @param tables 表名数组
     */
    void setTables(String[] tables);

    /**
     * 获取表元数据
     *
     * @return 表元数据，包含表结构信息
     */
    TableMetaData getTableMetaData();

    /**
     * 设置表元数据
     *
     * @param tmd 表元数据
     */
    void setTableMetaData(TableMetaData tmd);

    /**
     * 设置分组表达式
     *
     * @param groupBy 分组表达式，如 "department, status"
     */
    void setGroupBy(String groupBy);

    /**
     * 获取分组表达式
     *
     * @return 分组表达式
     */
    String getGroupBy();
}