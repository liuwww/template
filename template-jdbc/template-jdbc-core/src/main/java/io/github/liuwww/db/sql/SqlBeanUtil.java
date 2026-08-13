package io.github.liuwww.db.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.liuwww.db.condition.Conditions;
import io.github.liuwww.db.condition.GroupCondition;
import io.github.liuwww.db.condition.OneCondition;
import io.github.liuwww.db.context.DbContext;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.common.entity.Order;
import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.common.util.StringUtil;
import io.github.liuwww.common.util.DbNameConverter;

/**
 * SqlBean 工具类
 * 
 * @deprecated 请使用 SqlBeanBuilder 接口直接构建 SQL，此类将在后续版本中移除
 */
@Deprecated
public class SqlBeanUtil {
    /** 匹配 SQL 中的 WHERE 关键字（单词边界，不区分大小写） */
    private static final java.util.regex.Pattern WHERE_PATTERN = java.util.regex.Pattern.compile("\\bwhere\\b",
            java.util.regex.Pattern.CASE_INSENSITIVE);

    private static Map<DbType, SqlBeanBuilder> sqlBuilderMap;
    static {
        sqlBuilderMap = new ConcurrentHashMap<DbType, SqlBeanBuilder>();
        sqlBuilderMap.put(DbType.MYSQL, new MySQLSqlBeanBuilder());
        sqlBuilderMap.put(DbType.ORACLE, new OracleSqlBeanBuilder());
        sqlBuilderMap.put(DbType.OTHER, new DefaultSqlBeanBuilder());
    }

    public static void registerBuilder(DbType dbType, SqlBeanBuilder builder) {
        sqlBuilderMap.put(dbType, builder);
    }

    public static SqlBean getInsertSqlBean(Row row) {
        SqlBeanBuilder builder = getSqlBuilder(row.getDbType());
        if (StringUtil.isNotBlank(row.getIdName()) && StringUtil.isNotBlank(row.getIdValue())) {
            return builder.buildInsert(row);
        } else {
            return builder.buildInsert(row, false);
        }
    }

    public static SqlBeanBuilder getSqlBuilder(DbType dbType) {
        SqlBeanBuilder builder = sqlBuilderMap.get(dbType);
        if (builder == null) {
            builder = sqlBuilderMap.get(DbType.OTHER);
        }
        return builder;
    }

    public static SqlBean getSqlBean(String tableName, Map<String, Object> paramMap, Order order) {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, null);
        DbType dbType = tmd != null ? tmd.getDbType() : DbType.OTHER;
        return getSqlBuilder(dbType).buildQuery(tableName, paramMap, order);
    }

    /**
     * 根据表名、条件和排序构建查询 SqlBean（指定表元数据）
     *
     * @param tableName 表名
     * @param paramMap  查询条件
     * @param order     排序对象
     * @param tmd       表元数据
     * @return SqlBean实例
     */
    public static SqlBean getSqlBean(String tableName, Map<String, Object> paramMap, Order order,
            TableMetaData tmd) {
        DbType dbType = tmd != null ? tmd.getDbType() : DbType.OTHER;
        return getSqlBuilder(dbType).buildQuery(tableName, paramMap, order, tmd);
    }

    public static <T> SqlBean getSqlBean(TableEntity<T> entity, Order order) {
        SqlBeanBuilder builder = getSqlBuilder(DbType.MYSQL);
        return builder.buildQuery(entity);
    }

    /**
     * 根据实体对象构建 SqlBean（支持指定表元数据）
     *
     * @param <T>     实体类型
     * @param entity  表实体对象
     * @param order   排序对象
     * @param tmd     表元数据
     * @return SqlBean实例
     */
    public static <T> SqlBean getSqlBean(TableEntity<T> entity, Order order, TableMetaData tmd) {
        DbType dbType = (tmd != null) ? tmd.getDbType() : DbType.MYSQL;
        SqlBeanBuilder builder = getSqlBuilder(dbType);
        return builder.buildQuery(entity, tmd, order);
    }

    /**
     * 根据实体对象构建 SqlBean（支持指定数据库执行器）
     * 
     * @param <T>              实体类型
     * @param entity           表实体对象
     * @param databaseExecutor 数据库执行器
     * @param order            排序对象
     * @return SqlBean实例
     */
    public static <T> SqlBean getSqlBean(TableEntity<T> entity, DatabaseExecutor databaseExecutor, Order order) {
        SqlBeanBuilder builder = getSqlBuilder(DbType.MYSQL);
        SqlBean sqlBean = builder.buildQuery(entity);
        if (databaseExecutor != null) {
            sqlBean.setDatabaseExecutor(databaseExecutor);
        }
        return sqlBean;
    }

    /**
     * 根据实体对象构建 SqlBean（支持指定数据库执行器和表元数据）
     *
     * @param <T>              实体类型
     * @param entity           表实体对象
     * @param databaseExecutor 数据库执行器
     * @param order            排序对象
     * @param tmd              表元数据
     * @return SqlBean实例
     */
    public static <T> SqlBean getSqlBean(TableEntity<T> entity, DatabaseExecutor databaseExecutor, Order order,
            TableMetaData tmd) {
        DbType dbType = (tmd != null) ? tmd.getDbType() : DbType.MYSQL;
        SqlBeanBuilder builder = getSqlBuilder(dbType);
        SqlBean sqlBean = builder.buildQuery(entity, tmd, order);
        if (databaseExecutor != null) {
            sqlBean.setDatabaseExecutor(databaseExecutor);
        }
        return sqlBean;
    }

    /**
     * 获取原始 SQL Bean（从配置中心读取SQL文件）
     * 
     * @param file SQL文件名
     * @param tag  SQL标签
     * @return SqlBean实例
     */
    public static SqlBean getOriginalSqlBean(String file, String tag) {
        // TODO: 从配置中心读取SQL文件并构建SqlBean
        // 当前返回一个空的SqlBean占位
        return new DefaultSqlBean("", new ArrayList<>(), null, DbType.MYSQL, new String[] {}, null);
    }

    /**
     * 根据 Table 对象构建 SqlBean
     * <p>
     * 通过 {@link SqlBeanBuilder#buildQuery(Table)} 构建 SQL，支持 Table 中指定的查询字段列表、
     * 条件列表等功能。当 Table 未指定字段时，将查询表的全部列。
     * </p>
     *
     * @param table 表对象
     * @return SqlBean实例
     */
    public static SqlBean getSqlBean(Table table) {
        TableMetaData tmd = table.getTableMetaData();
        if (tmd == null) {
            tmd = DbContext.getTableMetaData(table.getName(), null);
        }
        if (tmd == null) {
            throw new io.github.liuwww.common.execption.DbException("表【" + table.getName() + "】不存在或没加载！");
        }
        SqlBeanBuilder builder = getSqlBuilder(tmd.getDbType());
        return builder.buildQuery(table);
    }

    /**
     * 根据 Table 对象构建 SqlBean（指定表元数据）
     *
     * @param table 表对象
     * @param tmd   表元数据（当 table 自身未携带时使用）
     * @return SqlBean实例
     */
    public static SqlBean getSqlBean(Table table, TableMetaData tmd) {
        if (tmd == null) {
            tmd = table.getTableMetaData();
        }
        if (tmd == null) {
            throw new io.github.liuwww.common.execption.DbException("表【" + table.getName() + "】不存在或没加载！");
        }
        SqlBeanBuilder builder = getSqlBuilder(tmd.getDbType());
        return builder.buildQuery(table);
    }

    /**
     * 根据表名和指定字段列表构建查询 SqlBean
     * <p>
     * 不需要构建 Table 对象即可指定查询字段。当 fieldList 为 null 或空时，查询全部列。
     * 该方法适用于只需要指定查询字段、不需要预置条件的场景；如需添加条件，
     * 可通过返回的 QueryBean 的 {@code getCompare()} 进行链式条件构建。
     * </p>
     *
     * @param tableName 表名
     * @param fieldList 查询字段列表，为 null 或空时查询全部列
     * @return SqlBean实例
     */
    public static SqlBean getSqlBean(String tableName, List<Field> fieldList) {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, null);
        if (tmd == null) {
            throw new io.github.liuwww.common.execption.DbException("表【" + tableName + "】不存在或没加载！");
        }
        SqlBeanBuilder builder = getSqlBuilder(tmd.getDbType());
        return builder.buildQuery(tableName, fieldList);
    }

    /**
     * 根据表名和指定字段列表构建查询 SqlBean（指定表元数据）
     *
     * @param tableName 表名
     * @param fieldList 查询字段列表，为 null 或空时查询全部列
     * @param tmd       表元数据
     * @return SqlBean实例
     */
    public static SqlBean getSqlBean(String tableName, List<Field> fieldList, TableMetaData tmd) {
        if (tmd == null) {
            throw new io.github.liuwww.common.execption.DbException("表【" + tableName + "】不存在或没加载！");
        }
        SqlBeanBuilder builder = getSqlBuilder(tmd.getDbType());
        return builder.buildQuery(tableName, fieldList, tmd);
    }

    /**
     * 构建查询 SqlBean
     *
     * @param tableName      表名
     * @param condition      查询条件
     * @param orderByFields  排序字段列表
     * @param isAsc          是否升序
     * @return 查询 SqlBean
     */
    public static SqlBean getQuerySqlBean(String tableName, Map<String, Object> condition, List<String> orderByFields,
            boolean isAsc) {
        TableMetaData tmd = DbContext.getTableMetaData(tableName);
        return getQuerySqlBean(tableName, condition, orderByFields, isAsc, tmd);
    }

    /**
     * 构建查询 SqlBean（指定表元数据）
     *
     * @param tableName     表名
     * @param condition     查询条件
     * @param orderByFields 排序字段列表
     * @param isAsc         是否升序
     * @param tmd           表元数据
     * @return 查询 SqlBean
     */
    public static SqlBean getQuerySqlBean(String tableName, Map<String, Object> condition, List<String> orderByFields,
            boolean isAsc, TableMetaData tmd) {
        DbType dbType = tmd != null ? tmd.getDbType() : DbType.OTHER;
        Order order = null;
        if (orderByFields != null && !orderByFields.isEmpty()) {
            order = new Order(orderByFields.get(0), isAsc ? "ASC" : "DESC");
        }
        return getSqlBuilder(dbType).buildQuery(tableName, condition, order, tmd);
    }

    /**
     * 根据 Map 条件构建 COUNT 查询 SqlBean
     *
     * @param tableName 表名
     * @param condition 查询条件 Map（字段名 -> 值）
     * @return COUNT 查询 SqlBean
     */
    public static SqlBean getCountSqlBean(String tableName, Map<String, Object> condition) {
        TableMetaData tmd = DbContext.getTableMetaData(tableName);
        return getCountSqlBean(tableName, condition, tmd);
    }

    /**
     * 根据 Map 条件构建 COUNT 查询 SqlBean（指定表元数据）
     *
     * @param tableName 表名
     * @param condition 查询条件 Map（字段名 -> 值）
     * @param tmd       表元数据
     * @return COUNT 查询 SqlBean
     */
    public static SqlBean getCountSqlBean(String tableName, Map<String, Object> condition, TableMetaData tmd) {
        DbType dbType = tmd != null ? tmd.getDbType() : DbType.OTHER;
        SqlBeanBuilder builder = getSqlBuilder(dbType);
        SqlBean queryBean = builder.buildQuery(tableName, condition, null, tmd);
        String sql = queryBean.getSql();
        java.util.regex.Matcher whereMatcher = WHERE_PATTERN.matcher(sql);
        String countSql;
        if (whereMatcher.find()) {
            countSql = "SELECT COUNT(*) FROM " + tableName + " WHERE "
                    + sql.substring(whereMatcher.end());
        } else {
            countSql = "SELECT COUNT(*) FROM " + tableName;
        }
        return new DefaultSqlBean(countSql, new ArrayList<>(Arrays.asList(queryBean.getParams())), null, dbType,
                queryBean.getTables(), null);
    }

    /**
     * 添加排序子句到 SQL
     * 
     * @param sql   SQL 构建器
     * @param order 排序对象
     * @param tmd   表元数据
     */
    public static void addSqlOrderBy(StringBuilder sql, Order order, TableMetaData tmd) {
        addSqlOrderBy(sql, order, tmd, false);
    }

    /**
     * 添加排序子句到 SQL
     * 
     * @param sql        SQL 构建器
     * @param order      排序对象
     * @param tmd        表元数据
     * @param hasOrderBy 是否已经有 order by
     */
    public static void addSqlOrderBy(StringBuilder sql, Order order, TableMetaData tmd, boolean hasOrderBy) {
        if (order != null && StringUtil.isNotBlank(order.getField())) {
            if (!hasOrderBy) {
                sql.append(" order by ");
            } else {
                sql.append(", ");
            }
            String field = order.getField();
            if (tmd != null) {
                field = DbNameConverter.convertToDb(field);
            }
            sql.append(field);
            if (StringUtil.isNotBlank(order.getSort())) {
                sql.append(" ").append(order.getSort());
            }
        }
    }

    /**
     * 获取条件的 SQL 片段
     * 
     * @param oneCondition 单条件对象
     * @param dbType       数据库类型
     * @return SQL 条件片段
     */
    public static String getConditonSqlFragment(OneCondition oneCondition, DbType dbType) {
        SqlBeanBuilder builder = getSqlBuilder(dbType);
        return builder.buildConditonSqlFragment(oneCondition, dbType);
    }

    /**
     * 获取条件组的 SQL 片段
     * 
     * @param groupCondition 条件组对象
     * @param dbType         数据库类型
     * @return SQL 条件片段
     */
    public static String getConditonSqlFragment(GroupCondition groupCondition, DbType dbType) {
        SqlBeanBuilder builder = getSqlBuilder(dbType);
        return builder.buildConditonSqlFragment(groupCondition, dbType);
    }

    /**
     * 获取插入 SQL 模板
     * 
     * @param tmd    表元数据
     * @param withId 是否包含主键
     * @return SQL 模板字符串
     */
    public static String getInsertSql(TableMetaData tmd, boolean withId) {
        SqlBeanBuilder builder = getSqlBuilder(tmd.getDbType());
        return builder.buildInsertSql(tmd, withId);
    }

    /**
     * 获取更新 SQL Bean
     * 
     * @param row 行数据
     * @return SqlBean 实例
     */
    public static SqlBean getUpdateSqlBean(Row row) {
        SqlBeanBuilder builder = getSqlBuilder(row.getDbType());
        return builder.buildUpdate(row);
    }

    /**
     * 获取批量更新 SQL Bean
     * 
     * @param tableName 表名
     * @param valMap    要更新的字段和值
     * @param paramMap  条件参数
     * @param tmd       表元数据
     * @return SqlBean 实例
     */
    public static SqlBean getUpdateRowsSqlBean(String tableName, Map<String, Object> valMap,
            Map<String, Object> paramMap, TableMetaData tmd) {
        SqlBeanBuilder builder = getSqlBuilder(tmd.getDbType());
        return builder.buildUpdate(tableName, valMap, paramMap, tmd);
    }

    /**
     * 获取删除 SQL Bean
     * 
     * @param row 行数据
     * @return SqlBean 实例
     */
    public static SqlBean getDeleteSqlBean(Row row) {
        SqlBeanBuilder builder = getSqlBuilder(row.getDbType());
        return builder.buildDelete(row);
    }

    /**
     * 获取删除 SQL 模板
     * 
     * @param tmd 表元数据
     * @return 删除 SQL 模板字符串
     */
    public static String getDeleteSql(TableMetaData tmd) {
        SqlBeanBuilder builder = getSqlBuilder(tmd.getDbType());
        return builder.buildDeleteSql(tmd);
    }
}
