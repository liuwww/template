package org.liuwww.db.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.liuwww.db.context.TableMetaData;
import org.springframework.jdbc.core.JdbcTemplate;

public class DefaultSqlBean implements SqlBean
{
    protected StringBuilder sql;

    protected List<Object> params;

    protected StringBuilder orderBy;

    protected DbType dbType;

    protected String[] tables;

    protected JdbcTemplate jdbcTemplate;

    protected TableMetaData tmd;

    protected String groupBy;

    protected DefaultSqlBean()
    {

    }

    public DefaultSqlBean(CharSequence sql, String orderBy, JdbcTemplate jdbcTemplate)
    {
        setSql(sql);
        setOrderBy(orderBy);
        this.jdbcTemplate = jdbcTemplate;
    }

    private void setSql(CharSequence sql)
    {
        if (sql instanceof StringBuilder)
        {
            this.sql = (StringBuilder) sql;
        }
        else if (sql != null)
        {
            this.sql = new StringBuilder(sql);
        }
        else
        {
            sql = null;
        }

    }

    public DefaultSqlBean(CharSequence sql, Object[] params)
    {
        setSql(sql);
        setParams(params);
    }

    public DefaultSqlBean(CharSequence sql, Object[] params, String orderBy, JdbcTemplate jdbcTemplate)
    {
        setSql(sql);
        setParams(params);
        this.orderBy = new StringBuilder(orderBy);
        this.jdbcTemplate = jdbcTemplate;
    }

    public DefaultSqlBean(CharSequence sql, Object[] params, String orderBy, DbType dbType, JdbcTemplate jdbcTemplate)
    {
        setSql(sql);
        setParams(params);
        setOrderBy(orderBy);
        this.dbType = dbType;
        this.jdbcTemplate = jdbcTemplate;
    }

    public DefaultSqlBean(CharSequence sql, Object[] params, JdbcTemplate jdbcTemplate)
    {
        setSql(sql);
        setParams(params);
        this.jdbcTemplate = jdbcTemplate;
    }

    public DefaultSqlBean(CharSequence sql, List<Object> params, StringBuilder orderBy, DbType dbType, String[] tables,
            JdbcTemplate jdbcTemplate)
    {
        if (sql != null)
        {
            setSql(sql);
        }
        this.params = params;
        this.orderBy = orderBy;
        this.dbType = dbType;
        this.tables = tables;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getSql()
    {
        return sql.toString();
    }

    @Override
    public Object[] getParams()
    {
        if (this.params == null)
        {
            return null;
        }
        return params.toArray();
    }

    @Override
    public void setParams(Object[] params)
    {
        if (params != null)
        {
            if (this.params == null)
            {
                this.params = new ArrayList<Object>(params.length);
            }
            else
            {
                this.params.clear();
            }
            for (Object o : params)
            {
                this.params.add(o);
            }
        }
        else
        {
            if (this.params != null)
            {
                this.params.clear();
            }
        }
    }

    public void addParams(Collection<Object> collection)
    {
        if (this.params == null)
        {
            this.params = new ArrayList<Object>(collection.size());
        }
        this.params.addAll(collection);
    }

    @Override
    public String getOrderBy()
    {
        if (orderBy == null)
        {
            return null;
        }
        return orderBy.toString();
    }

    public void setOrderBy(StringBuilder orderBy)
    {
        this.orderBy = orderBy;
    }

    @Override
    public DbType getDbType()
    {
        return dbType;
    }

    @Override
    public void setDbType(DbType dbType)
    {
        this.dbType = dbType;
    }

    @Override
    public String[] getTables()
    {
        return tables;
    }

    @Override
    public void setTables(String[] tables)
    {
        this.tables = tables;
    }

    @Override
    public JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;
    }

    @Override
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void setSql(String sql)
    {
        if (sql == null)
        {
            sql = "";
        }
        if (this.sql == null)
        {
            this.sql = new StringBuilder(sql);
        }
        else
        {
            this.sql.delete(0, this.sql.length());
            this.sql.append(sql);
        }
    }

    @Override
    public void setOrderBy(String orderBy)
    {
        if (orderBy == null)
        {
            orderBy = "";
        }
        if (this.orderBy == null)
        {
            this.orderBy = new StringBuilder(orderBy);
        }
        else
        {
            this.orderBy.delete(0, this.orderBy.length());
            this.orderBy.append(orderBy);
        }
    }

    public StringBuilder getOriginalSql()
    {
        return this.sql;
    }

    public StringBuilder getOriginalOrderby()
    {
        return this.orderBy;
    }

    public List<Object> getParamsList()
    {
        return this.params;
    }

    @Override
    public TableMetaData getTableMetaData()
    {
        return this.tmd;
    }

    @Override
    public void setTableMetaData(TableMetaData tmd)
    {
        this.tmd = tmd;
    }

    @Override
    public String getGroupBy()
    {
        return this.groupBy;
    }

    @Override
    public void setGroupBy(String groupBy)
    {
        this.groupBy = groupBy;
    }

}
