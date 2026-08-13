package io.github.liuwww.db.sql;

import java.util.List;
import java.util.Map;

import io.github.liuwww.common.entity.Order;
import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.db.condition.Conditions;
import io.github.liuwww.db.condition.GroupCondition;
import io.github.liuwww.db.condition.OneCondition;
import io.github.liuwww.db.context.TableMetaData;

/**
 * SQL 构建器接口
 * <p>
 * 根据不同数据库类型构建 SQL 语句。
 * 每种数据库都有对应的实现类，处理各自特有的 SQL 语法。
 * </p>
 * 
 * @author liuwww
 * @since 2017-05-11
 */
public interface SqlBeanBuilder {

    /**
     * 构建查询 SQL
     * <p>
     * 根据 Table 对象构建查询 SQL，Table 中可以指定查询的字段列表。
     * </p>
     *
     * @param table Table 对象
     * @return SqlBean 实例
     */
    SqlBean buildQuery(Table table);

    /**
     * 根据实体对象构建查询 SQL
     * <p>
     * 实体对象中非空字段将作为 WHERE 条件进行等值匹配。
     * </p>
     *
     * @param <T>    实体类型
     * @param entity 实体对象
     * @return SqlBean 实例
     */
    <T> SqlBean buildQuery(TableEntity<T> entity);

    /**
     * 根据实体对象构建查询 SQL（支持排序）
     *
     * @param <T>    实体类型
     * @param entity 实体对象
     * @param tmd    表元数据
     * @param order  排序信息
     * @return SqlBean 实例
     */
    <T> SqlBean buildQuery(TableEntity<T> entity, TableMetaData tmd, Order order);

    /**
     * 构建插入 SQL
     * <p>
     * 根据 Row 对象构建 INSERT 语句。
     * </p>
     *
     * @param row 行数据
     * @return SqlBean 实例
     */
    SqlBean buildInsert(Row row);

    /**
     * 构建插入 SQL
     *
     * @param row    行数据
     * @param withId 是否包含主键字段
     * @return SqlBean 实例
     */
    SqlBean buildInsert(Row row, boolean withId);

    /**
     * 构建更新 SQL
     * <p>
     * 根据 Row 对象构建 UPDATE 语句，以主键作为 WHERE 条件。
     * </p>
     *
     * @param row 行数据
     * @return SqlBean 实例
     */
    SqlBean buildUpdate(Row row);

    /**
     * 构建删除 SQL
     * <p>
     * 根据 Row 对象构建 DELETE 语句，以主键作为 WHERE 条件。
     * </p>
     *
     * @param row 行数据
     * @return SqlBean 实例
     */
    SqlBean buildDelete(Row row);

    /**
     * 构建查询 SQL（基于已有 SqlBean）
     * <p>
     * 在已有 SqlBean 的基础上添加查询条件。
     * </p>
     *
     * @param bean     已有 SqlBean
     * @param paramMap 查询参数
     * @return SqlBean 实例
     */
    SqlBean buildQuery(SqlBean bean, Map<String, Object> paramMap);

    /**
     * 构建查询 SQL（基于条件集合）
     * <p>
     * 在已有 SqlBean 的基础上添加 Conditions 条件。
     * </p>
     *
     * @param bean       已有 SqlBean
     * @param conditions 条件集合
     * @return SqlBean 实例
     */
    SqlBean buildQuery(SqlBean bean, Conditions conditions);

    /**
     * 构建单条件 SQL 片段
     *
     * @param oneCondition 单条件对象
     * @param dbType       数据库类型
     * @return SQL 条件片段
     */
    String buildConditonSqlFragment(OneCondition oneCondition, DbType dbType);

    /**
     * 构建条件组 SQL 片段
     *
     * @param groupCondition 条件组对象
     * @param dbType         数据库类型
     * @return SQL 条件片段
     */
    String buildConditonSqlFragment(GroupCondition groupCondition, DbType dbType);

    /**
     * 构建查询 SQL（基于表名和参数）
     *
     * @param tableName 表名
     * @param paramMap  查询参数
     * @param order     排序信息
     * @return SqlBean 实例
     */
    SqlBean buildQuery(String tableName, Map<String, Object> paramMap, Order order);

    /**
     * 构建查询 SQL（基于表名、参数和指定表元数据）
     *
     * @param tableName 表名
     * @param paramMap  查询参数
     * @param order     排序信息
     * @param tmd       表元数据
     * @return SqlBean 实例
     */
    SqlBean buildQuery(String tableName, Map<String, Object> paramMap, Order order, TableMetaData tmd);

    /**
     * 构建查询 SQL（基于表名和指定字段列表）
     * <p>
     * 当 fieldList 为 null 或空时，查询表的全部列；否则只查询 fieldList 中指定的字段。
     * 字段将通过表元数据进行校验，并支持别名（通过 {@link Field} 的 alias 属性指定）。
     * </p>
     *
     * @param tableName 表名
     * @param fieldList 查询字段列表，为 null 或空时查询全部列
     * @return SqlBean 实例
     */
    SqlBean buildQuery(String tableName, List<Field> fieldList);

    /**
     * 构建查询 SQL（基于表名、指定字段列表和表元数据）
     *
     * @param tableName 表名
     * @param fieldList 查询字段列表，为 null 或空时查询全部列
     * @param tmd       表元数据
     * @return SqlBean 实例
     */
    SqlBean buildQuery(String tableName, List<Field> fieldList, TableMetaData tmd);

    /**
     * 构建更新 SQL（基于参数映射）
     *
     * @param tableName 表名
     * @param valMap    要更新的字段和值
     * @param paramMap  条件参数
     * @param tmd       表元数据
     * @return SqlBean 实例
     */
    SqlBean buildUpdate(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap, TableMetaData tmd);

    /**
     * 获取条件字段名
     * <p>
     * 根据字段名获取对应的数据库列名，如果字段无效则返回 null。
     * </p>
     *
     * @param field 字段名
     * @param tmd   表元数据
     * @return 数据库列名，如果字段无效则返回 null
     */
    String getConditionField(String field, TableMetaData tmd);

    /**
     * 构建删除 SQL 模板
     * <p>
     * 返回根据主键删除记录的 SQL 模板。
     * </p>
     *
     * @param tmd 表元数据
     * @return 删除 SQL 模板
     */
    String buildDeleteSql(TableMetaData tmd);

    /**
     * 构建插入 SQL 模板（不包含主键）
     *
     * @param tmd 表元数据
     * @return 插入 SQL 模板
     */
    String buildInsertSql(TableMetaData tmd);

    /**
     * 构建插入 SQL 模板
     *
     * @param tmd    表元数据
     * @param withId 是否包含主键字段
     * @return 插入 SQL 模板
     */
    String buildInsertSql(TableMetaData tmd, boolean withId);
}