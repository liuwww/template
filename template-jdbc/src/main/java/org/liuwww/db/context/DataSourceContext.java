package org.liuwww.db.context;

import javax.sql.DataSource;

import org.liuwww.db.sql.DbType;

public interface DataSourceContext
{
    public void init();

    public TableMetaData getTableMetaData(String table);

    public DataSource getDataSource();

    public DbType getDbType();
}
