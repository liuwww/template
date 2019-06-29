package org.liuwww.db.sql;

import java.util.Map;

public class Row
{
    private String idValue;

    private String idName;

    private String tableName;

    private boolean isEffective = true;

    private Map<String, Object> rowValueMap;

    private DbType dbType;

    // 系统默认处理的字段
    private Map<String, Object> additionalMap;

    public DbType getDbType()
    {
        return dbType;
    }

    public void setDbType(DbType dbType)
    {
        this.dbType = dbType;
    }

    public boolean getIsEffective()
    {
        return isEffective;
    }

    public void setIsEffective(boolean isEffective)
    {
        this.isEffective = isEffective;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getIdValue()
    {
        return idValue;
    }

    public void setIdValue(String idValue)
    {
        this.idValue = idValue;
    }

    public String getIdName()
    {
        return idName;
    }

    public void setIdName(String idName)
    {
        this.idName = idName;
    }

    public Map<String, Object> getRowValueMap()
    {
        return rowValueMap;
    }

    protected void setRowValueMap(Map<String, Object> rowValueMap)
    {
        this.rowValueMap = rowValueMap;
    }

    public Map<String, Object> getAdditionalMap()
    {
        return additionalMap;
    }

    public void setAdditionalMap(Map<String, Object> additionalMap)
    {
        this.additionalMap = additionalMap;
    }

}
