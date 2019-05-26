package org.liuwww.db.sql;

import org.liuwww.db.context.TableMetaData;
import org.springframework.jdbc.core.JdbcTemplate;

public interface SqlBean
{
    public JdbcTemplate getJdbcTemplate();

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate);

    public String getSql();

    public void setSql(String sql);

    public Object[] getParams();

    public void setParams(Object[] params);

    public String getOrderBy();

    public void setOrderBy(String orderBy);

    public DbType getDbType();

    public void setDbType(DbType dbType);

    public String[] getTables();

    public void setTables(String[] tables);

    public TableMetaData getTableMetaData();

    public void setTableMetaData(TableMetaData tmd);

}
