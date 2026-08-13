package io.github.liuwww.common.Idgen;

import java.util.Map;

import io.github.liuwww.db.context.TableMetaData;

public interface IdGenerator {
    /**
     * 生成下一个ID
     * 
     * @param tableMetaData 表元数据
     * @return 生成的ID，返回null时,为无效值,表示使用数据库自动生成策略
     */
    String nextKey(TableMetaData tableMetaData);

    /**
     * 获取下一个ID
     * 
     * @param tableMetaData 表元数据
     * @return 生成的ID，返回-1时，为无效值，表示使用数据库自动生成策略
     */
    int nextIntKey(TableMetaData tableMetaData);

    /**
     * 获取下一个ID
     * 
     * @param tableMetaData 表元数据
     * @return 生成的ID，返回-1L时，为无效值，表示使用数据库自动生成策略
     */
    long nextLongKey(TableMetaData tableMetaData);

    /**
     * 判断主键是否由数据库自动生成
     * 
     * @param tableMetaData 表元数据
     * @return true表示由数据库自动生成，false表示需要应用层生成
     */
    boolean isCreatedByDatabase(TableMetaData tableMetaData);

    /**
     * 获取ID生成器类型标识
     * 
     * @return ID生成器类型名称
     */
    default String getType() {
        return this.getClass().getSimpleName();
    }

    /**
     * 初始化方法
     * 
     * @param properties 配置属性
     */
    default void initialize(Map<String, Object> properties) {
        // 默认空实现
    }
}