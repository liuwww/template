package io.github.liuwww.db.context;

import javax.sql.DataSource;

import io.github.liuwww.common.execption.DbException;
import io.github.liuwww.db.config.DatabaseConfigCenter;
import io.github.liuwww.db.sql.DbType;

/**
 * 数据库上下文静态门面。
 * <p>
 * 通过 {@link #setCurrentContext(CoreDbContext)} 注入 {@link CoreDbContext} 实例后，
 * 可在任意位置通过静态方法访问配置中心的功能。
 * </p>
 * <p>
 * <b>使用约定：</b>必须在使用前调用 {@link #setCurrentContext(CoreDbContext)} 注入上下文，
 * 否则静态方法将抛出 {@link DbException}。
 * </p>
 *
 * @author liuwww
 * @date 2026-05-07
 */
public class DbContext {

    private static volatile CoreDbContext currentContext;

    public static void setCurrentContext(CoreDbContext context) {
        currentContext = context;
    }

    public static CoreDbContext getCurrentContext() {
        return currentContext;
    }

    /**
     * 获取当前上下文的 {@link CoreDbContext}。
     *
     * @return 当前上下文
     * @throws DbException 如果未设置上下文
     */
    public static CoreDbContext getCoreDbContext() {
        CoreDbContext ctx = currentContext;
        if (ctx == null) {
            throw new DbException("DbContext未初始化，请先调用setCurrentContext设置上下文");
        }
        return ctx;
    }

    /**
     * 获取当前上下文的配置中心。
     *
     * @return 配置中心
     * @throws DbException 如果未设置上下文
     */
    public static DatabaseConfigCenter getConfigCenter() {
        return getCoreDbContext().getConfigCenter();
    }

    /**
     * 获取表元数据。
     *
     * @param tableName 表名
     * @return 表元数据，未找到返回 null
     */
    public static TableMetaData getTableMetaData(String tableName) {
        return getConfigCenter().getTableMetaData(tableName);
    }

    /**
     * 获取指定数据源的表元数据。
     *
     * @param tableName  表名
     * @param dataSource 数据源
     * @return 表元数据，未找到或数据源不匹配返回 null
     */
    public static TableMetaData getTableMetaData(String tableName, DataSource dataSource) {
        return getConfigCenter().getTableMetaData(tableName, dataSource);
    }

    /**
     * 获取表对应的数据库类型。
     *
     * @param tableName  表名
     * @param dataSource 数据源
     * @return 数据库类型，未找到返回 {@link DbType#OTHER}
     */
    public static DbType getDbType(String tableName, DataSource dataSource) {
        TableMetaData tmd = getTableMetaData(tableName, dataSource);
        if (tmd == null) {
            return DbType.OTHER;
        }
        return tmd.getDbType();
    }
}
