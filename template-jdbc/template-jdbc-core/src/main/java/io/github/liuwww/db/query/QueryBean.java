package io.github.liuwww.db.query;

import java.util.List;
import java.util.Map;

import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.query.conditon.QueryBeanCompare;
import io.github.liuwww.db.sql.SqlBean;

/**
 * 查询构建器接口
 * <p>
 * 提供流式的查询构建方式，支持条件组合、排序、分页等功能。
 * 通过 getCompare() 获取条件构建器来添加查询条件。
 * </p>
 * 
 * @author liuwww
 * @since 2017-05-25
 */
public interface QueryBean {

    /**
     * 执行查询，返回单个实体对象
     * <p>
     * 根据已设置的条件和排序执行查询，返回第一条匹配的记录。
     * </p>
     *
     * @param <T>   实体类型
     * @param clazz 实体对象的 Class
     * @return 匹配的实体对象，如果没有匹配记录则返回 null
     */
    <T> T getBean(Class<T> clazz);

    /**
     * 执行查询，返回实体对象列表
     *
     * @param <T>   实体类型
     * @param clazz 实体对象的 Class
     * @return 匹配的实体对象列表
     */
    <T> List<T> getBeanList(Class<T> clazz);

    /**
     * 执行查询，返回最多 maxNum 条实体对象
     * <p>
     * maxNum 大于 0 时生效，查询最多 maxNum 条数据；
     * 0 或 0 以下查询所有数据。
     * </p>
     *
     * @param <T>    实体类型
     * @param clazz  实体对象的 Class
     * @param maxNum 最大返回记录数
     * @return 匹配的实体对象列表
     */
    <T> List<T> getBeanList(Class<T> clazz, int maxNum);

    /**
     * 执行查询，返回单个 Map 对象
     * <p>
     * 根据已设置的条件和排序执行查询，返回第一条匹配的记录。
     * </p>
     *
     * @return 匹配的 Map 对象，每行为一个 Map，如果没有匹配记录则返回 null
     */
    Map<String, Object> getMap();

    /**
     * 执行查询，返回 Map 列表
     *
     * @return 匹配的 Map 列表
     */
    List<Map<String, Object>> getMapList();

    /**
     * 执行查询，返回最多 maxNum 条 Map 对象
     * <p>
     * maxNum 大于 0 时生效，查询最多 maxNum 条数据；
     * 0 或 0 以下查询所有数据。
     * </p>
     *
     * @param maxNum 最大返回记录数
     * @return 匹配的 Map 列表
     */
    List<Map<String, Object>> getMapList(int maxNum);

    /**
     * 执行分页查询，返回 Map 类型的分页结果
     *
     * @param page 分页信息，包含页码、每页大小、排序字段等
     * @return 分页结果，rows 中为 Map 对象
     */
    Page getPage(Page page);

    /**
     * 执行分页查询，返回实体类型的分页结果
     *
     * @param <T>   实体类型
     * @param page  分页信息，包含页码、每页大小、排序字段等
     * @param clazz 实体对象的 Class
     * @return 分页结果，rows 中为实体对象
     */
    <T> Page getPage(Page page, Class<T> clazz);

    /**
     * 设置排序字段（替换已有排序）
     * <p>
     * 注意：如果之前已设置过排序，此方法会替换原有排序。
     * 如果需要追加排序，请使用 addOrderBy() 方法。
     * </p>
     *
     * @param orderBy 排序表达式，如 "create_time DESC" 或 "name ASC, age DESC"
     * @return 当前 QueryBean 实例，支持链式调用
     */
    QueryBean setTheOrderBy(String orderBy);

    /**
     * 追加排序字段
     * <p>
     * 在已有排序的基础上追加排序字段。
     * </p>
     *
     * @param orderBy 排序表达式，如 "create_time DESC"
     * @return 当前 QueryBean 实例，支持链式调用
     */
    QueryBean addOrderBy(String orderBy);

    /**
     * 获取条件构建器
     * <p>
     * 返回的条件构建器支持链式调用，用于添加查询条件。
     * </p>
     *
     * @return QueryBeanCompare 实例
     */
    QueryBeanCompare getCompare();

    /**
     * 查询匹配的记录数量
     * <p>
     * 执行 COUNT 查询，返回匹配当前条件的记录数量。
     * </p>
     *
     * @return 匹配的记录数量
     */
    long getCount();

    /**
     * 获取当前查询构建器对应的 SqlBean
     * <p>
     * 返回组装好的 SqlBean 对象，包含 SQL 语句和参数。
     * </p>
     *
     * @return SqlBean 实例
     */
    SqlBean getSqlBean();
}