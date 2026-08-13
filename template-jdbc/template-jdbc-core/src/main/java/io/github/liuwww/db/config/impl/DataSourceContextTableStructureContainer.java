package io.github.liuwww.db.config.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import io.github.liuwww.db.config.TableStructureContainer;
import io.github.liuwww.db.context.TableMetaData;

/**
 * 基于 ConcurrentHashMap 的表结构容器实现。
 * <p>
 * 表名以小写形式存储，支持大小写不敏感的表名查找。
 * </p>
 *
 * @author lww
 */
public class DataSourceContextTableStructureContainer implements TableStructureContainer {

    private final Map<String, TableMetaData> tableMetaDataMap;

    public DataSourceContextTableStructureContainer() {
        this.tableMetaDataMap = new ConcurrentHashMap<>();
    }

    @Override
    public TableMetaData getTableMetaData(String tableName) {
        if (tableName == null) {
            return null;
        }
        return tableMetaDataMap.get(tableName.toLowerCase());
    }

    @Override
    public TableMetaData getTableMetaData(String tableName, DataSource dataSource) {
        TableMetaData metaData = getTableMetaData(tableName);
        if (metaData == null) {
            return null;
        }
        // dataSource 为 null 时不按数据源过滤，返回任意匹配的表
        if (dataSource == null || metaData.getDataSource() == dataSource) {
            return metaData;
        }
        return null;
    }

    @Override
    public List<String> getTableNames() {
        return new ArrayList<>(tableMetaDataMap.keySet());
    }

    @Override
    public Map<String, TableMetaData> getAllTableMetaData() {
        return new HashMap<>(tableMetaDataMap);
    }

    @Override
    public void addTableMetaData(String tableName, TableMetaData tableMetaData) {
        if (tableName != null && tableMetaData != null) {
            tableMetaDataMap.put(tableName.toLowerCase(), tableMetaData);
        }
    }

    @Override
    public void removeTableMetaData(String tableName) {
        if (tableName != null) {
            tableMetaDataMap.remove(tableName.toLowerCase());
        }
    }

    @Override
    public void clear() {
        tableMetaDataMap.clear();
    }
}
