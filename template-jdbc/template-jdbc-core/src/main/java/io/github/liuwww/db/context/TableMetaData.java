package io.github.liuwww.db.context;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import io.github.liuwww.common.util.CaseInsensitiveMap;
import io.github.liuwww.db.sql.Column;
import io.github.liuwww.db.sql.DbType;

/**
 * 表元数据
 * <p>
 * 记录数据源相关的表信息，包括表名、表字段、主键等信息。
 * 用于 SQL 构建、字段映射、结果映射等操作。
 * </p>
 * 
 * @author lww
 */
public class TableMetaData {

    /** 数据源 */
    private DataSource dataSource;

    /** 表名 */
    private String tableName;

    /** 列列表 */
    private List<Column> columnList;

    /**
     * 主键列数组
     * <p>
     * 单主键时数组长度为1，联合主键时数组长度大于1。
     * </p>
     */
    private Column[] idColumns;

    /** 主键名称数组 */
    private String[] idNames;

    /** Java 字段名到 Column 的映射 */
    private Map<String, Column> nameMap;

    /** 数据库列名到 Column 的映射（大小写不敏感） */
    private Map<String, Column> columnMap;

    /** 数据库类型 */
    private DbType dbType;

    /** 表类型：0-表(TABLE)，1-视图(VIEW) */
    private byte tableType;

    /** 表类型常量 - 表 */
    public static final byte TABLE_TYPE_TABLE = 0;

    /** 表类型常量 - 视图 */
    public static final byte TABLE_TYPE_VIEW = 1;

    /** 表注释 */
    private String comment;

    /** 主键列名（数据库列名，支持逗号分隔的联合主键） */
    private String idColumnName;

    /**
     * 获取表类型
     *
     * @return 表类型（如 TABLE、VIEW）
     */
    public String getTableType() {
        return tableType == TABLE_TYPE_VIEW ? "VIEW" : "TABLE";
    }

    /**
     * 设置表类型
     *
     * @param tableType 表类型字符串（TABLE 或 VIEW）
     */
    public void setTableType(String tableType) {
        this.tableType = "VIEW".equalsIgnoreCase(tableType) ? TABLE_TYPE_VIEW : TABLE_TYPE_TABLE;
    }

    /**
     * 设置表类型
     *
     * @param tableType 表类型字节值
     */
    public void setTableType(byte tableType) {
        this.tableType = tableType;
    }

    /**
     * 获取数据库类型
     *
     * @return 数据库类型
     */
    public DbType getDbType() {
        return dbType;
    }

    /**
     * 设置数据库类型
     *
     * @param dbType 数据库类型
     */
    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    /**
     * 清空所有缓存数据
     * <p>
     * 清空列列表和字段映射，用于重新加载表结构。
     * </p>
     */
    public void clear() {
        if (columnList != null)
            columnList.clear();
        if (nameMap != null)
            nameMap.clear();
        if (columnMap != null)
            columnMap.clear();
    }

    /**
     * 根据字段名或列名获取列信息
     * <p>
     * 优先按 Java 字段名查找，如果找不到则按数据库列名查找。
     * </p>
     *
     * @param field Java 字段名或数据库列名
     * @return 列信息，如果找不到返回 null
     */
    public Column getColumn(String field) {
        Column column = nameMap.get(field);
        if (column == null) {
            column = columnMap.get(field);
        }
        return column;
    }

    /**
     * 根据数据库列名获取列信息
     *
     * @param columnName 数据库列名
     * @return 列信息，如果找不到返回 null
     */
    public Column getColumnByColumn(String columnName) {
        return columnMap.get(columnName);
    }

    /**
     * 根据 Java 字段名获取列信息
     *
     * @param field Java 字段名
     * @return 列信息，如果找不到返回 null
     */
    public Column getColumnByField(String field) {
        return nameMap.get(field);
    }

    /**
     * 创建空的 TableMetaData 实例
     * <p>
     * 初始化列列表和字段映射。
     * </p>
     */
    @SuppressWarnings("unchecked")
    public TableMetaData() {
        columnList = new ArrayList<Column>();
        nameMap = new HashMap<String, Column>();
        columnMap = new CaseInsensitiveMap();
    }

    /**
     * 从 ResultSet 中添加列信息
     * <p>
     * 从 JDBC DatabaseMetaData.getColumns() 返回的 ResultSet 中读取列信息并添加到缓存中。
     * </p>
     *
     * @param crs 包含列信息的 ResultSet
     * @param key 主键列名，如果当前列是主键则设置为主键列
     * @throws SQLException 当读取 ResultSet 失败时抛出
     */
    void addColumn(ResultSet crs, String key) throws SQLException {
        Column column = new Column();
        String columnName = crs.getString("COLUMN_NAME");
        column.setColumnName(columnName);
        column.setColumnSize(crs.getInt("COLUMN_SIZE"));
        column.setDataType(crs.getInt("DATA_TYPE"));
        column.setDataTypeName(crs.getString("TYPE_NAME"));
        column.setNullable(crs.getInt("NULLABLE") == 1);
        column.setRemarks(crs.getString("REMARKS"));
        nameMap.put(column.getColumnName(), column);
        columnMap.put(column.getColumnName(), column);
        this.columnList.add(column);

        // 处理单主键
        if (key != null && columnName.equals(key)) {
            this.idColumns = new Column[] { column };
            this.idNames = new String[] { columnName };
        }

        // 处理联合主键
        if (idNames != null) {
            for (int i = 0; i < idNames.length; i++) {
                if (idNames[i].trim().equalsIgnoreCase(columnName)) {
                    if (idColumns == null) {
                        idColumns = new Column[idNames.length];
                    }
                    idColumns[i] = column;
                    break;
                }
            }
        }
    }

    /**
     * 获取数据库 Catalog
     * <p>
     * 根据不同数据库类型返回对应的 catalog/schema 名称。
     * </p>
     *
     * @param userName 数据库用户名
     * @param dbName   数据库名称（如 mysql、oracle、postgresql）
     * @return catalog/schema 名称
     */
    public static String getDbCatalog(String userName, String dbName) {
        String catalog;
        if (dbName == null) {
            return userName != null ? userName : "public";
        }
        dbName = dbName.toLowerCase();
        if (userName != null) {
            if (dbName.equals("oracle")) {
                catalog = userName.toUpperCase(java.util.Locale.ROOT);
            } else if (dbName.equals("postgresql")) {
                catalog = "public";
            } else if (dbName.equals("mysql")) {
                catalog = null;
            } else if (dbName.equals("mssqlserver")) {
                catalog = null;
            } else if (dbName.equals("db2")) {
                catalog = userName.toUpperCase(java.util.Locale.ROOT);
            } else {
                catalog = userName;
            }
        } else {
            catalog = "public";
        }
        return catalog;
    }

    /**
     * 获取单主键列
     * <p>
     * 如果是联合主键，返回第一个主键列。
     * </p>
     *
     * @return 主键列信息，如果没有主键返回 null
     */
    public Column getIdColumn() {
        return idColumns != null && idColumns.length > 0 ? idColumns[0] : null;
    }

    /**
     * 设置单主键列
     *
     * @param idColumn 主键列信息
     */
    public void setIdColumn(Column idColumn) {
        this.idColumns = new Column[] { idColumn };
        this.idNames = new String[] { idColumn != null ? idColumn.getColumnName() : null };
    }

    /**
     * 设置主键列数组（支持单主键与联合主键）。
     * <p>
     * 同时根据列名填充 {@code idNames}；单主键时同步设置 {@code idColumnName}。
     * </p>
     *
     * @param idColumns 主键列数组，可为 null 表示无主键
     */
    public void setIdColumns(Column[] idColumns) {
        this.idColumns = idColumns;
        if (idColumns != null && idColumns.length > 0) {
            this.idNames = new String[idColumns.length];
            for (int i = 0; i < idColumns.length; i++) {
                this.idNames[i] = idColumns[i] != null ? idColumns[i].getColumnName() : null;
            }
            if (idColumns.length == 1 && idColumns[0] != null) {
                this.idColumnName = idColumns[0].getColumnName();
            }
        }
    }

    /**
     * 获取数据源
     *
     * @return 数据源
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 设置数据源
     *
     * @param dataSource 数据源
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取表名
     *
     * @return 表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置表名
     *
     * @param tableName 表名
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 获取列列表
     *
     * @return 列列表
     */
    public List<Column> getColumnList() {
        return columnList;
    }

    /**
     * 设置列列表
     *
     * @param columnList 列列表
     */
    protected void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    /**
     * 设置表注释
     *
     * @param comment 表注释
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 获取表注释
     *
     * @return 表注释
     */
    public String getComment() {
        return comment;
    }

    /**
     * 获取主键列名（数据库列名）
     * <p>
     * 如果是联合主键，返回第一个主键列名。
     * </p>
     *
     * @return 主键列名
     */
    public String getIdColumnName() {
        if (idColumnName != null) {
            return idColumnName;
        }
        if (idNames != null && idNames.length > 0) {
            return idNames[0];
        }
        return null;
    }

    /**
     * 设置主键列名
     * <p>
     * 支持逗号分隔的联合主键，如 "id1,id2"。
     * </p>
     *
     * @param idColumnName 主键列名
     */
    public void setIdColumnName(String idColumnName) {
        String[] ids = idColumnName.split(",");
        if (ids.length > 1) {
            this.idColumnName = null;
            this.idColumns = new Column[ids.length];
            this.idNames = ids;
        } else {
            this.idColumnName = idColumnName;
            this.idNames = new String[] { idColumnName };
        }
    }

    /**
     * 添加 Java 字段名到 Column 的映射
     *
     * @param name   Java 字段名
     * @param column 列信息
     */
    public void putNameMap(String name, Column column) {
        nameMap.put(name, column);
    }

    /**
     * 添加数据库列名到 Column 的映射
     *
     * @param name   数据库列名
     * @param column 列信息
     */
    public void putColumnMap(String name, Column column) {
        columnMap.put(name, column);
    }

    /**
     * 获取联合主键名称数组
     *
     * @return 主键名称数组
     */
    public String[] getIdNames() {
        return idNames;
    }

    /**
     * 判断是否是联合主键
     *
     * @return 如果是联合主键返回 true，否则返回 false
     */
    public boolean isUnionKey() {
        return idNames != null && idNames.length > 1;
    }

    /**
     * 获取联合主键列数组
     *
     * @return 主键列数组
     */
    public Column[] getIdColumns() {
        return idColumns;
    }

    @Override
    public String toString() {
        return "TableMetaData{tableName='" + tableName + "', dbType=" + dbType + ", columnCount=" + columnList.size()
                + ", isUnionKey=" + isUnionKey() + "}";
    }
}