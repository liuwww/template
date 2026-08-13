package io.github.liuwww.db.update;

/**
 * 流式更新构建器接口
 * <p>
 * 提供链式调用的方式构建更新操作，支持设置更新字段和条件。
 * 适用于灵活的单条或多条数据更新场景。
 * </p>
 * 
 * @author liuwww
 * @since 2023-03-28
 */
public interface UpdateBean {

    /**
     * 执行更新操作
     *
     * @return 受影响的行数
     */
    int update();

    /**
     * 添加更新的条件
     * <p>
     * 条件字段将作为 WHERE 子句的一部分。
     * </p>
     *
     * @param name 字段名
     * @param val  字段值
     * @return UpdateBean 实例自身，支持链式调用
     */
    UpdateBean addCondition(String name, Object val);

    /**
     * 添加要更新的字段
     * <p>
     * 字段将作为 SET 子句的一部分。
     * </p>
     *
     * @param field 更新的字段名
     * @param val   字段的值
     * @return UpdateBean 实例自身，支持链式调用
     */
    UpdateBean addValue(String field, Object val);
}
