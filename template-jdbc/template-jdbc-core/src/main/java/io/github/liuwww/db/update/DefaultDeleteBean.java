package io.github.liuwww.db.update;

import java.util.HashMap;
import java.util.Map;

import io.github.liuwww.db.service.IDataTemplate;

/**
 * 默认流式删除构建器实现
 * <p>
 * 实现了 {@link DeleteBean} 接口，通过链式调用构建删除操作。
 * 内部使用 paramMap 存储 WHERE 条件。
 * </p>
 * 
 * @author liuwww
 * @since 2023-03-28
 */
public class DefaultDeleteBean implements DeleteBean {

    /** WHERE 条件字段和值映射 */
    private Map<String, Object> paramMap;

    /** 数据操作模板 */
    private IDataTemplate dataTemplate;

    /** 表名 */
    private String tableName;

    /**
     * 创建删除构建器实例
     *
     * @param tableName    表名
     * @param dataTemplate 数据操作模板
     */
    public DefaultDeleteBean(String tableName, IDataTemplate dataTemplate) {
        super();
        this.dataTemplate = dataTemplate;
        this.tableName = tableName;
        this.paramMap = new HashMap<>();
    }

    @Override
    public int delete() {
        return dataTemplate.deleteRows(tableName, paramMap);
    }

    @Override
    public DeleteBean addCondition(String name, Object val) {
        paramMap.put(name, val);
        return this;
    }
}
