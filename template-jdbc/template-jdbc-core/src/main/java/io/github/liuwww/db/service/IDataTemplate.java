package io.github.liuwww.db.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.github.liuwww.db.sql.Row;
import io.github.liuwww.db.transaction.TransactionCallback;
import io.github.liuwww.db.update.DeleteBean;
import io.github.liuwww.db.update.UpdateBean;
import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.common.execption.DbException;

/**
 * 数据操作模板接口
 * <p>
 * 提供统一的数据库增删改操作，支持 Map 和实体对象两种方式。
 * 所有操作都会自动生成主键（除非已在参数中指定）。
 * </p>
 * 
 * @author liuwww
 * @since 2017-05-11
 */
public interface IDataTemplate {

    /**
     * 插入单条数据
     * <p>
     * 如果参数中已设置了 Id，则以设置的为准；
     * 否则由配置的 ID 生成器自动生成。
     * </p>
     *
     * @param tableName 表名
     * @param fieldVals 字段名与值的映射，键为实体字段名或数据库列名
     * @return 插入后的 Row 对象，包含生成的主键值
     * @throws DbException 当表不存在或插入失败时抛出
     */
    Row insert(String tableName, Map<String, Object> fieldVals) throws DbException;

    /**
     * 批量插入多条数据
     * <p>
     * 如果参数中已设置了 Id，则以设置的为准；
     * 否则由配置的 ID 生成器自动生成。
     * </p>
     *
     * @param tableName     表名
     * @param fieldValList  字段值映射列表，每个 Map 代表一条记录
     * @return 插入后的 Row 对象列表
     * @throws DbException 当表不存在或插入失败时抛出
     */
    List<Row> insert(String tableName, List<Map<String, Object>> fieldValList) throws DbException;

    /**
     * 插入单个实体对象
     * <p>
     * 如果实体中已设置了 Id，则以设置的为准；
     * 否则由配置的 ID 生成器自动生成。
     * </p>
     *
     * @param <T>    实体类型
     * @param entity 实体对象，需实现 {@link TableEntity} 接口
     * @return 插入后的实体对象，包含生成的主键值
     * @throws DbException 当表不存在或插入失败时抛出
     */
    <T> T insert(TableEntity<T> entity) throws DbException;

    /**
     * 批量插入多个实体对象
     * <p>
     * 如果实体中已设置了 Id，则以设置的为准；
     * 否则由配置的 ID 生成器自动生成。
     * </p>
     *
     * @param <T>        实体类型
     * @param entityList 实体对象列表
     * @return 插入后的实体对象列表
     * @throws DbException 当表不存在或插入失败时抛出
     */
    <T> List<T> insert(List<? extends TableEntity<T>> entityList) throws DbException;

    /**
     * 根据主键更新单条数据
     * <p>
     * 根据传入的 fieldVals 中的主键值定位记录，并更新其他字段。
     * </p>
     *
     * @param tableName 表名
     * @param fieldVals 字段名与值的映射，必须包含主键字段
     * @return 受影响的行数
     * @throws DbException 当表不存在或更新失败时抛出
     */
    int update(String tableName, Map<String, Object> fieldVals) throws DbException;

    /**
     * 根据主键批量更新多条数据
     * <p>
     * 对每个 Map 执行单条更新操作。
     * </p>
     *
     * @param tableName      表名
     * @param fieldMapList   字段值映射列表，每个 Map 必须包含主键字段
     * @return 受影响的总行数
     * @throws DbException 当表不存在或更新失败时抛出
     */
    int update(String tableName, List<Map<String, Object>> fieldMapList) throws DbException;

    /**
     * 更新单个实体对象
     * <p>
     * 根据实体的主键值定位记录，并更新其他字段。
     * </p>
     *
     * @param <T>    实体类型
     * @param entity 实体对象，需包含有效的主键值
     * @return 受影响的行数
     * @throws DbException 当表不存在或更新失败时抛出
     */
    <T> int update(TableEntity<T> entity) throws DbException;

    /**
     * 批量更新多个实体对象
     * <p>
     * 对每个实体执行单条更新操作。
     * </p>
     *
     * @param <T>        实体类型
     * @param entityList 实体对象列表
     * @return 受影响的总行数
     * @throws DbException 当表不存在或更新失败时抛出
     */
    <T> int update(List<? extends TableEntity<T>> entityList) throws DbException;

    /**
     * 根据条件批量更新多条数据
     * <p>
     * valMap 中的字段为要更新的字段和值，
     * paramMap 中的字段为 WHERE 条件，每个有效参数都将作为条件。
     * </p>
     *
     * @param tableName 表名
     * @param valMap    要修改的字段和值
     * @param paramMap  条件参数，每个有效参数都将作为 WHERE 条件
     * @return 受影响的行数
     * @throws DbException 当表不存在或更新失败时抛出
     */
    int updateRows(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap) throws DbException;

    /**
     * 根据主键删除单条数据
     *
     * @param tableName 表名
     * @param idMap     包含主键字段和值的映射
     * @return 受影响的行数
     * @throws DbException 当表不存在或删除失败时抛出
     */
    int delete(String tableName, Map<String, Object> idMap) throws DbException;

    /**
     * 根据主键批量删除多条数据
     *
     * @param tableName 表名
     * @param idMapList 包含主键字段和值的映射列表
     * @return 受影响的总行数
     */
    int delete(String tableName, List<Map<String, Object>> idMapList);

    /**
     * 根据条件删除数据
     * <p>
     * paramMap 中的每个有效参数都将作为 WHERE 条件。
     * </p>
     *
     * @param tableName 表名
     * @param paramMap  条件参数
     * @return 受影响的行数
     * @throws DbException 当表不存在或删除失败时抛出
     */
    int deleteRows(String tableName, Map<String, Object> paramMap) throws DbException;

    /**
     * 删除单个实体对象对应的记录
     * <p>
     * 根据实体的主键值定位并删除记录。
     * </p>
     *
     * @param <T>    实体类型
     * @param entity 实体对象
     * @return 受影响的行数
     * @throws DbException 当表不存在或删除失败时抛出
     */
    <T> int delete(TableEntity<T> entity) throws DbException;

    /**
     * 批量删除多个实体对象对应的记录
     *
     * @param <T>        实体类型
     * @param entityList 实体对象列表
     * @return 受影响的总行数
     * @throws DbException 当表不存在或删除失败时抛出
     */
    <T> int delete(List<? extends TableEntity<T>> entityList) throws DbException;

    /**
     * 根据单个主键值删除记录
     *
     * @param table 表名
     * @param id    主键值
     * @return 受影响的行数
     * @throws DbException 当表不存在或删除失败时抛出
     */
    int delete(String table, Serializable id) throws DbException;

    /**
     * 根据多个主键值批量删除记录
     *
     * @param table 表名
     * @param ids   主键值数组
     * @return 受影响的总行数
     * @throws DbException 当表不存在或删除失败时抛出
     */
    int delete(String table, Serializable[] ids) throws DbException;

    /**
     * 创建流式更新构建器
     * <p>
     * 返回的 UpdateBean 支持链式调用，用于构建复杂的更新操作。
     * </p>
     *
     * @param tableName 表名
     * @return UpdateBean 实例
     */
    UpdateBean createUpdateBean(String tableName);

    /**
     * 创建流式删除构建器
     * <p>
     * 返回的 DeleteBean 支持链式调用，用于构建复杂的删除操作。
     * </p>
     *
     * @param tableName 表名
     * @return DeleteBean 实例
     */
    DeleteBean createDeleteBean(String tableName);

    /**
     * 在事务中执行操作
     * <p>
     * 回调内通过 {@code IDataTemplate} 执行的所有操作将共享同一个事务连接，
     * 回调正常返回则提交，抛出异常则回滚。
     * </p>
     *
     * @param <T>      返回类型
     * @param callback 事务回调
     * @return 回调返回值
     * @throws DbException 当事务管理器未初始化或事务执行失败时抛出
     */
    <T> T executeInTransaction(TransactionCallback<T> callback);
}