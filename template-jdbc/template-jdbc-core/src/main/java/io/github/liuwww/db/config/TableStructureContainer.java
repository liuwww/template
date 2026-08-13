package io.github.liuwww.db.config;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import io.github.liuwww.db.context.TableMetaData;

/**
 * 表结构配置容器接口
 * 用于存储和管理表结构信息
 * 
 * @author liuwww
 */
public interface TableStructureContainer {

    /**
     * 获取表元数据
     * 
     * @param tableName 表名
     * @return 表元数据
     */
    TableMetaData getTableMetaData(String tableName);

    /**
     * 获取指定数据源的表元数据
     * 
     * @param tableName  表名
     * @param dataSource 数据源
     * @return 表元数据
     */
    TableMetaData getTableMetaData(String tableName, DataSource dataSource);

    /**
     * 获取所有表名列表
     * 
     * @return 表名列表
     */
    List<String> getTableNames();

    /**
     * 获取所有表元数据
     * 
     * @return 表元数据映射
     */
    Map<String, TableMetaData> getAllTableMetaData();

    /**
     * 添加表元数据
     * 
     * @param tableName     表名
     * @param tableMetaData 表元数据
     */
    void addTableMetaData(String tableName, TableMetaData tableMetaData);

    /**
     * 移除表元数据
     * 
     * @param tableName 表名
     */
    void removeTableMetaData(String tableName);

    /**
     * 清空所有表元数据
     */
    void clear();
}