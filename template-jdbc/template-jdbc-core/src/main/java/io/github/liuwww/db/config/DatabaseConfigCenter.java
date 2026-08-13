package io.github.liuwww.db.config;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.executor.DatabaseExecutor;

/**
 * 数据库配置中心接口
 * 用于管理数据库表结构、SQL组装等功能
 * 
 * @author liuwww
 * @date 2026-03-01
 */
public interface DatabaseConfigCenter {

    /**
     * 初始化配置中心
     */
    void init();

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
     * @param tableName 表名
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
     * 刷新表结构
     */
    void refresh();

    /**
     * 设置DatabaseExecutor
     * 
     * @param databaseExecutor 数据库执行器
     */
    void setDatabaseExecutor(DatabaseExecutor databaseExecutor);

    /**
     * 获取DatabaseExecutor
     * 
     * @return 数据库执行器
     */
    DatabaseExecutor getDatabaseExecutor();
}