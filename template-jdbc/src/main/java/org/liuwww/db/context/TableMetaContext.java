package org.liuwww.db.context;

import javax.sql.DataSource;

import org.liuwww.db.sql.DbType;

public interface TableMetaContext
{
    public void init();

    /**
     * @desc:依次检查DataSource，返回第一个获取到的表元数据
     * @Date:2019年1月22日下午4:50:00
     * @author liuwww
     * @param table
     * @return
     */
    public TableMetaData getTableMetaData(String table);

    /**
     * @desc:获取dataSource下表的元数据
     * @Date:2019年1月22日下午4:49:00
     * @author liuwww
     * @param table
     * @param ds
     * @return
     */
    public TableMetaData getTableMetaData(String table, DataSource ds);

    /**
     * @desc:获取知道beanName的DataSource下表的元数据
     * @Date:2019年1月22日下午4:48:04
     * @author liuwww
     * @param table
     * @param dsBeanName
     * @return
     */
    public TableMetaData getTableMetaData(String table, String dsBeanName);

    /**
     * @desc:根据beanName获取DataSource
     * @Date:2019年1月22日下午4:46:13
     * @author liuwww
     * @param dataSourceBeanName
     * @return
     */
    public DataSource getDataSource(String dataSourceBeanName);

    /**
     * @desc:根据beanName获取数据源的数据库类型
     * @Date:2019年1月22日下午4:46:55
     * @author liuwww
     * @param dataSourceBeanName
     * @return
     */
    public DbType getDbType(String dataSourceBeanName);

    /**
     * @desc:获取dataSource的数据库类型
     * @Date:2019年1月22日下午4:47:34
     * @author liuwww
     * @param dataSource
     * @return
     */
    public DbType getDbType(DataSource dataSource);
}
