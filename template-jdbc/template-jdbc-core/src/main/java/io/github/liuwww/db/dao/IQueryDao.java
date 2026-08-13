package io.github.liuwww.db.dao;

import java.util.List;
import java.util.Map;

import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.sql.SqlBean;
import io.github.liuwww.common.execption.DbException;

/**
 * 查询 DAO 接口
 * <p>
 * 提供底层的数据库查询操作，直接执行 SQL 语句并返回结果。
 * 与 IQueryTemplate 不同，此接口不包含业务逻辑，仅负责 SQL 执行和结果映射。
 * </p>
 * 
 * @author liuwww
 * @since 2017-05-11
 */
public interface IQueryDao {

    /**
     * 执行查询，返回 Map 列表
     * <p>
     * 每行数据为一个 Map，键为列名，值为列值。
     * </p>
     *
     * @param sqlBean 包含 SQL 语句和参数的 SqlBean 对象
     * @return Map 列表，每行为一个 Map
     * @throws DbException 当 SQL 执行失败时抛出
     */
    List<Map<String, Object>> getList(SqlBean sqlBean) throws DbException;

    /**
     * 执行查询，返回单个实体对象
     * <p>
     * 将查询结果的第一行映射为指定类型的实体对象。
     * </p>
     *
     * @param <T>     实体类型
     * @param sqlBean 包含 SQL 语句和参数的 SqlBean 对象
     * @param clazz   实体对象的 Class
     * @return 匹配的实体对象，如果没有结果则返回 null
     * @throws DbException 当 SQL 执行失败或结果映射失败时抛出
     */
    <T> T getBean(SqlBean sqlBean, Class<T> clazz) throws DbException;

    /**
     * 执行查询，返回实体对象列表
     * <p>
     * 将查询结果的每一行映射为指定类型的实体对象。
     * </p>
     *
     * @param <T>     实体类型
     * @param sqlBean 包含 SQL 语句和参数的 SqlBean 对象
     * @param clazz   实体对象的 Class
     * @return 实体对象列表
     * @throws DbException 当 SQL 执行失败或结果映射失败时抛出
     */
    <T> List<T> getBeanList(SqlBean sqlBean, Class<T> clazz) throws DbException;

    /**
     * 执行查询，返回单个 Map 对象
     * <p>
     * 将查询结果的第一行作为 Map 返回。
     * </p>
     *
     * @param sqlBean 包含 SQL 语句和参数的 SqlBean 对象
     * @return Map 对象，如果没有结果则返回 null
     * @throws DbException 当 SQL 执行失败时抛出
     */
    Map<String, Object> getMap(SqlBean sqlBean) throws DbException;

    /**
     * 执行分页查询，返回实体类型的分页结果
     * <p>
     * 自动处理分页逻辑，包括 COUNT 查询和数据查询。
     * </p>
     *
     * @param <T>       实体类型
     * @param bean      包含 SQL 语句和参数的 SqlBean 对象
     * @param pageInfo  分页信息，包含页码、每页大小等
     * @param clazz     实体对象的 Class
     * @return 分页结果，rows 中为实体对象
     * @throws DbException 当 SQL 执行失败或分页处理失败时抛出
     */
    <T> Page getPage(SqlBean bean, Page pageInfo, Class<T> clazz) throws DbException;

    /**
     * 执行分页查询，返回 Map 类型的分页结果
     * <p>
     * 自动处理分页逻辑，包括 COUNT 查询和数据查询。
     * </p>
     *
     * @param bean     包含 SQL 语句和参数的 SqlBean 对象
     * @param pageInfo 分页信息，包含页码、每页大小等
     * @return 分页结果，rows 中为 Map 对象
     * @throws DbException 当 SQL 执行失败或分页处理失败时抛出
     */
    Page getPage(SqlBean bean, Page pageInfo) throws DbException;

    /**
     * 执行 COUNT 查询
     * <p>
     * 根据 SqlBean 中的 SQL 语句自动包装为 COUNT 查询。
     * </p>
     *
     * @param bean 包含 SQL 语句和参数的 SqlBean 对象
     * @return 匹配的记录数量
     * @throws DbException 当 SQL 执行失败时抛出
     */
    long getCount(SqlBean bean) throws DbException;
}