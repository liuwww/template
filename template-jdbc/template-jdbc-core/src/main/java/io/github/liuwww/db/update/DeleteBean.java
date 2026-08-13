package io.github.liuwww.db.update;

/**
 * 流式删除构建器接口
 * <p>
 * 提供链式调用的方式构建删除操作，支持设置删除条件。
 * 适用于灵活的单条或多条数据删除场景。
 * </p>
 * 
 * @author liuwww
 * @since 2023-03-28
 */
public interface DeleteBean {

    /**
     * 执行删除操作
     *
     * @return 受影响的行数
     */
    int delete();

    /**
     * 添加删除的条件
     * <p>
     * 条件字段将作为 WHERE 子句的一部分。
     * </p>
     *
     * @param name 字段名
     * @param val  字段值
     * @return DeleteBean 实例自身，支持链式调用
     */
    DeleteBean addCondition(String name, Object val);
}
