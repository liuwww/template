package io.github.liuwww.db.query;

import java.util.List;

import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.db.sql.Field;
import io.github.liuwww.db.sql.SqlBean;
import io.github.liuwww.db.sql.Table;
import io.github.liuwww.db.executor.DatabaseExecutor;

/**
 * 查询构建器工厂接口
 * <p>
 * 用于创建 QueryBean 实例，支持多种查询构建方式：
 * <ul>
 *   <li>基于实体对象的查询</li>
 *   <li>基于表名/视图名的查询</li>
 *   <li>基于 XML 配置 SQL 的查询</li>
 *   <li>基于已有 SqlBean 的查询</li>
 * </ul>
 * </p>
 * 
 * @author liuwww
 * @since 2019-02-21
 */
public interface QueryBeanFactory {

    /**
     * 根据实体对象创建基于表的查询
     * <p>
     * 从实体对象的 tableName() 方法获取表名，自动加载表结构。
     * </p>
     *
     * @param <T>    实体类型
     * @param entity 实体对象，用于确定查询的表
     * @return QueryBean 实例
     */
    <T> QueryBean createQueryBean(TableEntity<T> entity);

    /**
     * 根据实体对象创建基于表的查询，支持指定数据源
     * <p>
     * 在多数据源环境下，可以指定使用哪个数据源执行查询。
     * </p>
     *
     * @param <T>              实体类型
     * @param entity           实体对象
     * @param databaseExecutor 数据库执行器
     * @return QueryBean 实例
     */
    <T> QueryBean createQueryBean(TableEntity<T> entity, DatabaseExecutor databaseExecutor);

    /**
     * 根据 XML 配置的 SQL 创建查询
     * <p>
     * 从 classpath 下的 sql/{file}.xml 文件中读取指定 tag 的 SQL 配置。
     * </p>
     *
     * @param file classpath 下 sql 文件夹中 XML 的文件名，不包含后缀
     * @param tag  XML 中 SQL 所在的标签 name
     * @return QueryBean 实例
     */
    QueryBean createQueryBean(String file, String tag);

    /**
     * 根据 XML 配置的 SQL 创建查询，支持指定数据源
     * <p>
     * 在多数据源环境下，可以指定使用哪个数据源执行查询。
     * </p>
     *
     * @param file             classpath 下 sql 文件夹中 XML 的文件名，不包含后缀
     * @param tag              XML 中 SQL 所在的标签 name
     * @param databaseExecutor 数据库执行器
     * @return QueryBean 实例
     */
    QueryBean createQueryBean(String file, String tag, DatabaseExecutor databaseExecutor);

    /**
     * 根据 XML 配置的 SQL 创建查询，支持传入参数
     *
     * @param file   classpath 下 sql 文件夹中 XML 的文件名，不包含后缀
     * @param tag    XML 中 SQL 所在的标签 name
     * @param params 给定 SQL 中的参数
     * @return QueryBean 实例
     */
    QueryBean createQueryBean(String file, String tag, Object... params);

    /**
     * 根据 XML 配置的 SQL 创建查询，支持传入参数和指定数据源
     *
     * @param file             classpath 下 sql 文件夹中 XML 的文件名，不包含后缀
     * @param tag              XML 中 SQL 所在的标签 name
     * @param databaseExecutor 数据库执行器
     * @param params           给定 SQL 中的参数
     * @return QueryBean 实例
     */
    QueryBean createQueryBean(String file, String tag, DatabaseExecutor databaseExecutor, Object... params);

    /**
     * 根据表名或视图名创建查询
     *
     * @param tableName 表名或视图名
     * @return QueryBean 实例
     */
    QueryBean createQueryBean(String tableName);

    /**
     * 根据表名或视图名创建查询，支持指定数据源
     * <p>
     * 在多数据源环境下，可以指定使用哪个数据源执行查询。
     * </p>
     *
     * @param tableName        表名或视图名
     * @param databaseExecutor 数据库执行器
     * @return QueryBean 实例
     */
    QueryBean createQueryBean(String tableName, DatabaseExecutor databaseExecutor);

    /**
     * 直接根据给定的 SqlBean 创建查询
     * <p>
     * SqlBean 中已包含完整的 SQL 语句和参数。
     * </p>
     *
     * @param sqlBean 已组装好的 SqlBean 对象
     * @return QueryBean 实例
     */
    QueryBean createQueryBean(SqlBean sqlBean);

    /**
     * 根据 Table 对象创建查询，支持带条件和指定查询字段
     * <p>
     * Table 对象中可以指定查询的字段列表。
     * </p>
     *
     * @param table Table 对象
     * @return QueryBean 实例
     */
    QueryBean createQueryBean(Table table);

    /**
     * 根据表名和指定字段创建查询（可变参数）
     * <p>
     * 不需要构建 Table 对象即可指定查询字段。当 fields 为 null 或空时，查询全部列。
     * 如需添加查询条件，可通过返回的 QueryBean 的 {@code getCompare()} 进行链式条件构建。
     * </p>
     *
     * @param tableName 表名
     * @param fields    查询字段（可变参数），为 null 或空时查询全部列
     * @return QueryBean 实例
     */
    QueryBean createQueryBean(String tableName, Field... fields);

    /**
     * 根据表名和指定字段名创建查询（便捷方法，可变参数）
     * <p>
     * 直接传入字段名，无需构建 Field 对象，是最简便的指定查询字段的方式。例如：
     * <pre>
     * factory.select("test_user", "id", "name", "age")
     * </pre>
     * 当 fields 为 null 或空时，查询全部列。
     * 如需指定字段别名，请使用 {@link #createQueryBean(String, Field...)} 方法。
     * </p>
     * <p>
     * 注意：此方法使用独立方法名 {@code select}，以避免与
     * {@link #createQueryBean(String, String)} (file, tag) 的重载冲突。
     * </p>
     *
     * @param tableName 表名
     * @param fields    查询字段名（可变参数），为 null 或空时查询全部列
     * @return QueryBean 实例
     */
    QueryBean select(String tableName, String... fields);

    /**
     * 根据表名和指定字段创建查询，支持指定数据源
     * <p>
     * 在多数据源环境下，可以指定使用哪个数据源执行查询。
     * </p>
     *
     * @param tableName        表名
     * @param databaseExecutor 数据库执行器
     * @param fields           查询字段（可变参数），为 null 或空时查询全部列
     * @return QueryBean 实例
     */
    QueryBean createQueryBean(String tableName, DatabaseExecutor databaseExecutor, Field... fields);
}