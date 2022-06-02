package org.liuwww.db.context;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.liuwww.db.sql.Column;
import org.liuwww.db.sql.DbType;

/**
 * 记录数据源相关的表信息，包括表名，表字段等信息
 * @author lwww 2017年5月4日下午5:47:04
 */
public class TableMetaData
{
    private DataSource dataSource;

    private String tableName;

    private List<Column> columnList;

    private Column idColumn;

    /**
     * 处理联合主键的情况
     */
    private Column[] idColumns;

    private String[] idNames;

    private Map<String, Column> nameMap;

    private Map<String, Column> columnMap;

    private DbType dbType;

    private String tableType;

    private String comment;

    private String idColumnName;

    public String getTableType()
    {
        return tableType;
    }

    public void setTableType(String tableType)
    {
        this.tableType = tableType;
    }

    public DbType getDbType()
    {
        return dbType;
    }

    public void setDbType(DbType dbType)
    {
        this.dbType = dbType;
    }

    public void clear()
    {
        columnList.clear();
        nameMap.clear();
        columnMap.clear();
    }

    public Column getColumn(String field)
    {
        Column column = nameMap.get(field);
        if (column == null)
        {
            column = columnMap.get(field);
        }
        return column;
    }

    public Column getColumnByColumn(String columnName)
    {
        return columnMap.get(columnName);
    }

    public Column getColumnByField(String field)
    {
        return nameMap.get(field);
    }

    @SuppressWarnings("unchecked")
    public TableMetaData()
    {
        columnList = new ArrayList<Column>();
        nameMap = new HashMap<String, Column>();
        columnMap = new CaseInsensitiveMap();
    }

    void addColumn(ResultSet crs, String key) throws SQLException
    {
        Column column = new Column();
        String columnName = crs.getString("COLUMN_NAME");
        column.setColumnName(columnName);
        column.setColumnSize(crs.getInt("COLUMN_SIZE"));
        column.setDataType(crs.getInt("DATA_TYPE"));
        column.setDataTypeName(crs.getString("TYPE_NAME"));
        column.setIsNullAble(crs.getString("IS_NULLABLE"));
        column.setNullable(crs.getInt("NULLABLE"));
        column.setRemarks(crs.getString("REMARKS"));
        column.setTableName(crs.getString("TABLE_NAME"));
        nameMap.put(column.getName(), column);
        columnMap.put(column.getColumnName(), column);
        this.columnList.add(column);
        if (columnName.equals(key))
        {
            setIdColumn(column);
        }
    }

    public static String getDbCatalog(String userName, String dbName)
    {
        String catalog;
        dbName = dbName.toLowerCase();
        if (userName != null)
        {
            if (dbName.equals("oracle"))
            {
                catalog = userName.toUpperCase();
            }
            else if (dbName.equals("postgresql"))
            {
                catalog = "public";
            }
            else if (dbName.equals("mysql"))
            {
                catalog = null;
            }
            else if (dbName.equals("mssqlserver"))
            {
                catalog = null;
            }
            else if (dbName.equals("db2"))
            {
                catalog = userName.toUpperCase();
            }
            else
            {
                catalog = userName;
            }
        }
        else
        {
            catalog = "public";
        }
        return catalog;
    }

    public Column getIdColumn()
    {
        return idColumn;
    }

    protected void setIdColumn(Column idColumn)
    {
        this.idColumn = idColumn;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    protected void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public String getTableName()
    {
        return tableName;
    }

    protected void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public List<Column> getColumnList()
    {
        return columnList;
    }

    protected void setColumnList(List<Column> columnList)
    {
        this.columnList = columnList;
    }

    protected void setcomment(String comment)
    {
        this.comment = comment;
    }

    public String getComment()
    {
        return comment;
    }

    public String getIdColumnName()
    {
        return idColumnName;
    }

    protected void setIdColumnName(String idColumnName)
    {
        this.idColumnName = idColumnName;
        String[] ids = idColumnName.split(",");
        if (ids.length > 1)
        {
            // 联合主键处理
            idColumns = new Column[ids.length];
            idColumnName = null;
            idNames = ids;
        }
    }

    protected void putNameMap(String name, Column column)
    {
        nameMap.put(name, column);
    }

    protected void putColumnMap(String name, Column column)
    {
        columnMap.put(name, column);
    }

    public String[] getIdNames()
    {
        return idNames;
    }

    /**
     * 判断是否是联合主键
     */
    public boolean isUnionKey()
    {
        return idNames != null && idNames.length > 1;
    }

    public Column[] getIdColumns()
    {
        return idColumns;
    }

}
