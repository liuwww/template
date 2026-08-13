package io.github.liuwww.db.sql;

import java.util.Map;

/**
 * 数据库行数据
 * <p>
 * 表示数据库表中的一行数据，包含表名、主键值、字段值映射等信息。
 * 用于 SQL 构建和数据传递。
 * </p>
 * 
 * @author liuwww
 * @since 2017-05-11
 */
public class Row {

    /** 单主键值 */
    private String idValue;

    /** 单主键名 */
    private String idName;

    /** 联合主键值数组 */
    private String[] idValues;

    /** 联合主键名数组 */
    private String[] idNames;

    /** 表名 */
    private String tableName;

    /** 是否有效（用于标记是否实际包含数据） */
    private boolean isEffective = true;

    /** 字段值映射（键为字段名或列名，值为字段值） */
    private Map<String, Object> rowValueMap;

    /** 数据库类型 */
    private DbType dbType;

    /**
     * 系统默认处理的附加字段映射
     * <p>
     * 用于存储系统自动添加的字段（如创建时间、更新时间等）。
     * </p>
     */
    private Map<String, Object> additionalMap;

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
     * 获取是否有效
     *
     * @return 如果行数据有效返回 true，否则返回 false
     */
    public boolean getIsEffective() {
        return isEffective;
    }

    /**
     * 设置是否有效
     *
     * @param isEffective 是否有效
     */
    public void setIsEffective(boolean isEffective) {
        this.isEffective = isEffective;
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
     * 获取单主键值
     *
     * @return 主键值
     */
    public String getIdValue() {
        return idValue;
    }

    /**
     * 设置单主键值
     *
     * @param idValue 主键值
     */
    public void setIdValue(String idValue) {
        this.idValue = idValue;
    }

    /**
     * 获取单主键名
     *
     * @return 主键名
     */
    public String getIdName() {
        return idName;
    }

    /**
     * 设置单主键名
     *
     * @param idName 主键名
     */
    public void setIdName(String idName) {
        this.idName = idName;
    }

    /**
     * 获取字段值映射
     *
     * @return 字段值映射
     */
    public Map<String, Object> getRowValueMap() {
        return rowValueMap;
    }

    /**
     * 设置字段值映射
     *
     * @param rowValueMap 字段值映射
     */
    public void setRowValueMap(Map<String, Object> rowValueMap) {
        this.rowValueMap = rowValueMap;
    }

    /**
     * 获取附加字段映射
     *
     * @return 附加字段映射
     */
    public Map<String, Object> getAdditionalMap() {
        return additionalMap;
    }

    /**
     * 设置附加字段映射
     *
     * @param additionalMap 附加字段映射
     */
    public void setAdditionalMap(Map<String, Object> additionalMap) {
        this.additionalMap = additionalMap;
    }

    /**
     * 获取联合主键值数组
     *
     * @return 主键值数组
     */
    public String[] getIdValues() {
        return idValues;
    }

    /**
     * 设置联合主键值数组
     *
     * @param idValues 主键值数组
     */
    public void setIdValues(String[] idValues) {
        this.idValues = idValues;
    }

    /**
     * 获取联合主键名数组
     *
     * @return 主键名数组
     */
    public String[] getIdNames() {
        return idNames;
    }

    /**
     * 设置联合主键名数组
     *
     * @param idNames 主键名数组
     */
    public void setIdNames(String[] idNames) {
        this.idNames = idNames;
    }

    @Override
    public String toString() {
        return "Row{tableName='" + tableName + "', idName='" + idName + "', idValue='" + idValue
                + "', isEffective=" + isEffective + ", fieldCount=" + (rowValueMap != null ? rowValueMap.size() : 0) + "}";
    }
}