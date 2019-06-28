package org.liuwww.db.sql;

import java.sql.Types;

import org.liuwww.common.util.DbNameConverter;

public class Column
{
    private String name;

    private String columnName;

    private String tableName;

    private String DataTypeName;

    private int dataType;

    private String isNullAble;

    private int nullable;

    private String remarks;

    private int columnSize;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public String getTableName()
    {
        return tableName;
    }

    public String getIsNullAble()
    {
        return isNullAble;
    }

    public int getNullable()
    {
        return nullable;
    }

    public String getRemarks()
    {
        return remarks;
    }

    public int getColumnSize()
    {
        return columnSize;
    }

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
        this.name = DbNameConverter.convert(columnName);
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public void setIsNullAble(String isNullAble)
    {
        this.isNullAble = isNullAble;
    }

    public void setNullable(int nullable)
    {
        this.nullable = nullable;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    public void setColumnSize(int columnSize)
    {
        this.columnSize = columnSize;
    }

    public String getDataTypeName()
    {
        return DataTypeName;
    }

    public void setDataTypeName(String dataTypeName)
    {
        DataTypeName = dataTypeName;
    }

    public int getDataType()
    {
        return dataType;
    }

    public void setDataType(int dataType)
    {
        this.dataType = dataType;
    }

    public boolean isNumberColumn()
    {
        if (this.dataType == Types.BIGINT || this.dataType == Types.DECIMAL || this.dataType == Types.DOUBLE
                || this.dataType == Types.FLOAT || this.dataType == Types.INTEGER || this.dataType == Types.NUMERIC)
        {
            return true;
        }
        return false;
    }

}
