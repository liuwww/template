package org.liuwww.db.context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.liuwww.db.sql.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.liuwww.common.execption.SysException;
import org.liuwww.common.util.StringUtil;

public class DefaultDataSourceContext implements DataSourceContext
{
    private static Logger logger = LoggerFactory.getLogger(DefaultDataSourceContext.class);

    private Map<String, TableMetaData> tableMap;

    private List<TableMetaData> metaList;

    private Set<String> includeTableSet;

    private Set<String> excludeTableSet;

    private DataSource dataSource;

    private DbType dbType;

    public DefaultDataSourceContext(DataSource dataSource, String includeTables)
    {
        this(dataSource, includeTables, null);
    }

    @SuppressWarnings("unchecked")
    public DefaultDataSourceContext(DataSource dataSource, String includeTables, String excludeTables)
    {
        super();
        this.dataSource = dataSource;
        if (StringUtil.isNotBlank(includeTables))
        {
            includeTableSet = new HashSet<String>(Arrays.asList(includeTables.toLowerCase().split(",")));
        }
        if (StringUtil.isNotBlank(excludeTables))
        {
            excludeTableSet = new HashSet<String>(Arrays.asList(excludeTables.toLowerCase().split(",")));
        }
        metaList = new ArrayList<TableMetaData>();
        tableMap = new CaseInsensitiveMap();
    }

    public boolean include(String tableName)
    {
        if (includeTableSet != null)
        {
            if (includeTableSet.contains(tableName.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean exclude(String tableName)
    {
        if (excludeTableSet != null)
        {
            if (excludeTableSet.contains(tableName.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init()
    {
        Connection conn = null;
        boolean isdebug = logger.isDebugEnabled();
        try
        {
            conn = dataSource.getConnection();
            DatabaseMetaData md = conn.getMetaData();
            String catalog = conn.getCatalog();
            String dbName = md.getDatabaseProductName();
            String schema = getDbCatalog(md.getUserName(), dbName);
            ResultSet rs = md.getTables(conn.getCatalog(), schema, null, new String[]
            { "TABLE", "VIEW" });
            while (rs.next())
            {
                String tableName = rs.getString("TABLE_NAME");
                if ((includeTableSet != null && !include(tableName)) || (excludeTableSet != null && exclude(tableName)))
                {
                    continue;
                }
                if (isdebug)
                {
                    logger.debug("开始加载表[{}]元数据……", tableName);
                }
                TableMetaData tmd = new TableMetaData();
                tmd.setDbType(DbType.valueOf(dbName.toUpperCase()));
                tmd.setDataSource(dataSource);
                String tableType = rs.getString("TABLE_TYPE");
                tmd.setTableType(tableType);
                ResultSet krs = md.getPrimaryKeys(catalog, schema, tableName);
                String key = null;
                while (krs.next())
                {
                    key = krs.getString("COLUMN_NAME");
                    break;
                }
                tmd.setTableName(tableName);
                ResultSet crs = md.getColumns(null, null, tableName, null);
                while (crs.next())
                {
                    tmd.addColumn(crs, key);
                }
                crs.close();
                metaList.add(tmd);
                tableMap.put(tableName, tmd);
                if (isdebug)
                {
                    logger.debug("表[{}]元数据加载完成", tableName);
                }
            }
            rs.close();

        }
        catch (SQLException e)
        {
            throw new SysException(e);
        }
        finally
        {
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
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

    @Override
    public TableMetaData getTableMetaData(String table)
    {
        return this.tableMap.get(table);
    }

    @Override
    public DataSource getDataSource()
    {
        return this.dataSource;
    }

    @Override
    public DbType getDbType()
    {
        return this.dbType;
    }

}
