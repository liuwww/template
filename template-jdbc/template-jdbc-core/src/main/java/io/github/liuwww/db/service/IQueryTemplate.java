package io.github.liuwww.db.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.query.QueryBeanFactory;

/**
 * 查询模板接口
 * <p>
 * 提供统一的数据库查询操作，支持实体对象和 Map 两种返回方式。
 * 通过读取表结构配置与给定的条件重组 SQL 语句，执行查询并返回结果。
 * </p>
 * 
 * @author liuwww
 * @since 2017-04-27
 */
public interface IQueryTemplate extends QueryBeanFactory {

    /**
     * 根据实体对象的字段值进行等值查询，返回结果列表
     * <p>
     * 实体对象中非空字段将作为 WHERE 条件进行等值匹配。
     * </p>
     *
     * @param <T>    实体类型
     * @param entity 实体对象，非空字段将作为查询条件
     * @return 匹配的实体对象列表
     */
    <T> List<T> getBeanList(TableEntity<T> entity);

    /**
     * 根据实体对象的字段值进行等值查询，返回单个结果
     * <p>
     * 实体对象中非空字段将作为 WHERE 条件进行等值匹配。
     * 如果有多条记录匹配，返回第一条。
     * </p>
     *
     * @param <T>    实体类型
     * @param entity 实体对象，非空字段将作为查询条件
     * @return 匹配的实体对象，如果没有匹配记录则返回 null
     */
    <T> T getBean(TableEntity<T> entity);

    /**
     * 根据实体对象的条件进行分页查询
     * <p>
     * 实体对象中非空字段将作为 WHERE 条件进行等值匹配。
     * 返回的 Page 对象中 rows 的泛型类型为实体类型。
     * </p>
     *
     * @param <T>      实体类型
     * @param entity   实体对象，非空字段将作为查询条件
     * @param page     分页信息，包含页码、每页大小、排序字段等
     * @return 分页结果，rows 中为实体对象
     */
    <T> Page getPage(TableEntity<T> entity, Page page);

    /**
     * 按照表或视图进行分页查询，返回 Map 列表
     * <p>
     * paramMap 中的参数将作为查询条件。
     * 返回的 Page 对象中 rows 的泛型类型为 Map 类型。
     * </p>
     *
     * @param tableName 表名或视图名
     * @param paramMap  查询参数，键为实体字段名或数据库列名
     * @return 分页结果，rows 中为 Map 对象
     */
    Page getPage(String tableName, Map<String, Object> paramMap);

    /**
     * 按照表或视图进行分页查询，支持自定义分页参数
     * <p>
     * paramMap 中的参数将作为查询条件。
     * 表的话，字段可以是数据库字段或者实体字段；
     * 视图的话，必须是视图的字段。
     * </p>
     *
     * @param tableName 表名或视图名
     * @param pageInfo  分页信息，包含页码、每页大小、排序字段等
     * @param paramMap  查询参数
     * @return 分页结果
     */
    Page getPage(String tableName, Page pageInfo, Map<String, Object> paramMap);

    /**
     * 根据条件查询，返回 Map 列表
     * <p>
     * paramMap 中的参数将作为 WHERE 条件进行等值匹配。
     * </p>
     *
     * @param tableName 表名或视图名
     * @param paramMap  查询参数
     * @return 匹配的 Map 列表，每行为一个 Map
     */
    List<Map<String, Object>> getMapList(String tableName, Map<String, Object> paramMap);

    /**
     * 根据实体对象的条件查询记录数量
     * <p>
     * 实体对象中非空字段将作为 WHERE 条件进行等值匹配。
     * </p>
     *
     * @param <T>    实体类型
     * @param entity 实体对象，非空字段将作为查询条件
     * @return 匹配的记录数量
     */
    <T> long getCount(TableEntity<T> entity);

    /**
     * 根据条件查询记录数量
     * <p>
     * paramMap 中的参数将作为 WHERE 条件进行等值匹配。
     * </p>
     *
     * @param tableName 表名或视图名
     * @param condition 查询条件
     * @return 匹配的记录数量
     */
    long getCount(String tableName, Map<String, Object> condition);

    /**
     * 根据主键查询单条记录，返回 Map 对象
     *
     * @param tableName 表名
     * @param id        主键值
     * @return 匹配的 Map 对象，如果没有匹配记录则返回 null
     */
    Map<String, Object> getMap(String tableName, Serializable id);

    /**
     * 根据主键查询单条记录，返回实体对象
     *
     * @param <T>       实体类型
     * @param tableName 表名
     * @param id        主键值
     * @param clazz     实体对象的 Class
     * @return 匹配的实体对象，如果没有匹配记录则返回 null
     */
    <T> T getBean(String tableName, Serializable id, Class<T> clazz);
}