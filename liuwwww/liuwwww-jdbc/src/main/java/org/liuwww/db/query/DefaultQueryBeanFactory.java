package org.liuwww.db.query;

import org.liuwww.db.dao.IQueryDao;
import org.liuwww.db.sql.SqlBean;
import org.liuwww.db.sql.SqlBeanUtil;
import org.liuwww.db.sql.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class DefaultQueryBeanFactory implements QueryBeanFactory
{

    @Autowired
    protected IQueryDao queryDao;

    @Override
    public <T> QueryBean createQueryBean(Entity<T> entity)
    {
        return createQueryBean(entity, null);
    }

    @Override
    public QueryBean createQueryBean(String fileName, String tag)
    {
        return createQueryBean(fileName, tag, (JdbcTemplate) null);
    }

    @Override
    public QueryBean createQueryBean(String fileName, String tag, Object... params)
    {
        return createQueryBean(fileName, tag, null, params);
    }

    @Override
    public QueryBean createQueryBean(String tableName)
    {
        return createQueryBean(tableName, (JdbcTemplate) null);
    }

    @Override
    public QueryBean createQueryBean(SqlBean sqlBean)
    {
        return new DefaultQueryBean(getQueryDao(), sqlBean);
    }

    protected IQueryDao getQueryDao()
    {
        return queryDao;
    }

    @Override
    public <T> QueryBean createQueryBean(Entity<T> entity, JdbcTemplate jdbcTemplate)
    {
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, jdbcTemplate);
        DefaultQueryBean queryBean = new DefaultQueryBean(getQueryDao(), bean);
        queryBean.setTableQuery(true);
        queryBean.getSqlBean().setJdbcTemplate(jdbcTemplate);
        return queryBean;
    }

    @Override
    public QueryBean createQueryBean(String file, String tag, JdbcTemplate jdbcTemplate)
    {
        QueryBean queryBean = createQueryBean(file, tag);
        queryBean.getSqlBean().setJdbcTemplate(jdbcTemplate);
        return queryBean;
    }

    @Override
    public QueryBean createQueryBean(String file, String tag, JdbcTemplate jdbcTemplate, Object... params)
    {
        QueryBean queryBean = createQueryBean(file, tag, params);
        queryBean.getSqlBean().setJdbcTemplate(jdbcTemplate);
        return queryBean;
    }

    @Override
    public QueryBean createQueryBean(String tableName, JdbcTemplate jdbcTemplate)
    {
        Table table = new Table(tableName, jdbcTemplate);
        SqlBean bean = SqlBeanUtil.getSqlBean(table);
        bean.setTables(new String[]
        { tableName });
        DefaultQueryBean queryBean = new DefaultQueryBean(getQueryDao(), bean);
        queryBean.setTableQuery(true);
        return queryBean;
    }

    @Override
    public QueryBean createQueryBean(Table table)
    {
        SqlBean bean = SqlBeanUtil.getSqlBean(table);
        DefaultQueryBean queryBean = new DefaultQueryBean(getQueryDao(), bean);
        queryBean.setTableQuery(true);
        return queryBean;
    }
}
