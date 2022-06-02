package org.liuwww.db.context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
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
import org.liuwww.db.sql.Column;
import org.liuwww.db.sql.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.liuwww.common.execption.DbException;
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

    public String getDbCatalog(DatabaseMetaData md) throws SQLException
    {
        ResultSet rs = null;
        try
        {
            rs = md.getSchemas();
            while (rs.next())
            {
                for (int i = 0, len = rs.getMetaData().getColumnCount(); i < len; i++)
                {
                    if (rs.getBoolean(3))
                    {
                        return rs.getString(1);
                    }
                }
            }
            return null;
        }
        finally
        {
            rs.close();
        }
    }

    @Override
    public void init()
    {
        Connection conn = null;
        try
        {
            conn = dataSource.getConnection();
            DatabaseMetaData md = conn.getMetaData();
            String catalog = conn.getCatalog();
            String dbName = md.getDatabaseProductName();
            this.dbType = DbType.getByName(dbName.toUpperCase());
            String schema = getDbCatalog(md.getUserName(), dbName);
            if (DbType.MYSQL.equals(dbType))
            {
                new MysqlTableMataInit(conn, catalog).init();
            }
            else
            {
                new DefaultTableMataInit(md, schema, catalog).init();
            }
        }
        catch (Exception e)
        {
            throw new DbException(e);
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
            else if (dbName.equals("h2"))
            {
                catalog = "PUBLIC";
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

    class MysqlTableMetaInit
    {
        void init()
        {

        }
    }

    class MysqlTableMataInit
    {
        boolean isDebug = logger.isDebugEnabled();

        static final String tableQuerySql = "SELECT TABLE_NAME,TABLE_TYPE, TABLE_COMMENT  FROM INFORMATION_SCHEMA. TABLES WHERE TABLE_TYPE in('VIEW','BASE TABLE') and TABLE_SCHEMA = ?";

        static final String columnQuerySql = "SELECT c.TABLE_NAME,c.COLUMN_NAME,c.IS_NULLABLE,c.DATA_TYPE,c.CHARACTER_MAXIMUM_LENGTH,c.CHARACTER_OCTET_LENGTH,c.NUMERIC_PRECISION,c.NUMERIC_SCALE,c.DATETIME_PRECISION,c.COLUMN_TYPE,c.COLUMN_COMMENT FROM information_schema.`COLUMNS` c WHERE c.TABLE_SCHEMA = ?";

        static final String primaryQuerySql = "SELECT t.TABLE_NAME, GROUP_CONCAT(t.COLUMN_NAME) AS ID_COLUMN FROM information_schema.STATISTICS t WHERE t.INDEX_NAME = 'PRIMARY' AND t.TABLE_SCHEMA = ? GROUP BY t.TABLE_NAME";

        String catalog;

        Connection conn;

        public MysqlTableMataInit(Connection conn, String catalog)
        {
            super();
            this.catalog = catalog;
            this.conn = conn;
        }

        void init()
        {
            try
            {
                // 处理表
                PreparedStatement statement = conn.prepareStatement(tableQuerySql);
                statement.setString(1, catalog);
                ResultSet rs = statement.executeQuery();
                while (rs.next())
                {
                    String tableName = rs.getString("TABLE_NAME");
                    if ((includeTableSet != null && !include(tableName))
                            || (excludeTableSet != null && exclude(tableName)))
                    {
                        continue;
                    }
                    if (isDebug)
                    {
                        logger.debug("开始加载表[{}]元数据……", tableName);
                    }
                    TableMetaData tmd = new TableMetaData();
                    tmd.setDbType(dbType);
                    tmd.setDataSource(dataSource);
                    String tableType = rs.getString("TABLE_TYPE");
                    tmd.setTableType(tableType);
                    String comment = rs.getString("TABLE_COMMENT");
                    tmd.setcomment(comment);
                    tmd.setTableName(tableName);
                    metaList.add(tmd);
                    tableMap.put(tableName, tmd);
                    if (isDebug)
                    {
                        logger.debug("表[{}]元数据加载完成", tableName);
                    }
                }
                rs.close();
                statement.close();

                // 处理主键
                statement = conn.prepareStatement(primaryQuerySql);
                statement.setString(1, catalog);
                rs = statement.executeQuery();
                while (rs.next())
                {
                    String tableName = rs.getString("TABLE_NAME");
                    String idColumn = rs.getString("ID_COLUMN");
                    TableMetaData tmd = tableMap.get(tableName);
                    if (tmd != null)
                    {
                        tmd.setIdColumnName(idColumn);
                    }
                }
                rs.close();
                statement.close();

                // 处理字段

                statement = conn.prepareStatement(columnQuerySql);
                statement.setString(1, catalog);
                rs = statement.executeQuery();
                while (rs.next())
                {
                    String tableName = rs.getString("TABLE_NAME");
                    TableMetaData tmd = tableMap.get(tableName);
                    if (tmd != null)
                    {
                        Column column = new Column();
                        String columnName = rs.getString("COLUMN_NAME");
                        column.setColumnName(columnName);
                        // column.setColumnSize(rs.getInt("COLUMN_SIZE"));
                        String dataType = rs.getString("DATA_TYPE");
                        column.setDataType(MysqlDataTypeDefs.mysqlToJavaType(dataType));
                        column.setDataTypeName(dataType);
                        column.setIsNullAble(rs.getString("IS_NULLABLE"));
                        // column.setNullable(rs.getInt("NULLABLE"));
                        column.setRemarks(rs.getString("COLUMN_COMMENT"));
                        column.setTableName(tableName);
                        tmd.putColumnMap(column.getColumnName(), column);
                        tmd.putNameMap(column.getName(), column);
                        tmd.getColumnList().add(column);
                        if (columnName.equals(tmd.getIdColumnName()))
                        {
                            tmd.setIdColumn(column);
                        }
                        else if (tmd.isUnionKey())
                        {
                            String[] idNames = tmd.getIdNames();
                            for (int i = 0; i < idNames.length; i++)
                            {
                                if (StringUtil.equals(idNames[i], columnName))
                                {
                                    tmd.getIdColumns()[i] = column;
                                }
                            }
                        }
                    }
                }
                rs.close();
                statement.close();
            }
            catch (SQLException e)
            {
                throw new DbException(e);
            }
        }
    }

    class DefaultTableMataInit
    {
        boolean isDebug = logger.isDebugEnabled();

        DatabaseMetaData md;

        String schema;

        String catalog;

        public DefaultTableMataInit(DatabaseMetaData md, String schema, String catalog)
        {
            super();
            this.md = md;
            this.schema = schema;
            this.catalog = catalog;
        }

        void init()
        {
            try
            {
                ResultSet rs = md.getTables(catalog, schema, null, new String[]
                { "TABLE", "VIEW" });

                while (rs.next())
                {
                    String tableName = rs.getString("TABLE_NAME");
                    if ((includeTableSet != null && !include(tableName))
                            || (excludeTableSet != null && exclude(tableName)))
                    {
                        continue;
                    }
                    if (isDebug)
                    {
                        logger.debug("开始加载表[{}]元数据……", tableName);
                    }
                    TableMetaData tmd = new TableMetaData();
                    tmd.setDbType(dbType);
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
                    if (isDebug)
                    {
                        logger.debug("表[{}]元数据加载完成", tableName);
                    }
                }
                rs.close();
            }
            catch (SQLException e)
            {
                throw new DbException(e);
            }
        }
    }

}
