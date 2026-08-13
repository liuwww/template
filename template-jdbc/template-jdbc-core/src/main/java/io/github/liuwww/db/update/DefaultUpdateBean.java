package io.github.liuwww.db.update;

import java.util.HashMap;
import java.util.Map;

import io.github.liuwww.db.service.IDataTemplate;

/**
 * 默认流式更新构建器实现
 * <p>
 * 实现了 {@link UpdateBean} 接口，通过链式调用构建更新操作。
 * 内部使用 valMap 存储要更新的字段，paramMap 存储 WHERE 条件。
 * </p>
 * 
 * @author liuwww
 * @since 2023-03-28
 */
public class DefaultUpdateBean implements UpdateBean {

    /** 要更新的字段和值映射 */
    private Map<String, Object> valMap;

    /** WHERE 条件字段和值映射 */
    private Map<String, Object> paramMap;

    /** 数据操作模板 */
    private IDataTemplate dataTemplate;

    /** 表名 */
    private String tableName;

    /**
     * 创建更新构建器实例
     *
     * @param tableName    表名
     * @param dataTemplate 数据操作模板
     */
    public DefaultUpdateBean(String tableName, IDataTemplate dataTemplate) {
        super();
        this.tableName = tableName;
        this.dataTemplate = dataTemplate;
        this.valMap = new HashMap<>();
        this.paramMap = new HashMap<>();
    }

    @Override
    public int update() {
        return dataTemplate.updateRows(tableName, valMap, paramMap);
    }

    @Override
    public UpdateBean addCondition(String name, Object val) {
        paramMap.put(name, val);
        return this;
    }

    @Override
    public UpdateBean addValue(String field, Object val) {
        valMap.put(field, val);
        return this;
    }
}
