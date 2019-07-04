package org.liuwww.db.query;

import org.liuwww.common.entity.TableEntity;
import org.liuwww.db.context.DbContext;
import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.dao.IQueryDao;
import org.liuwww.db.sql.SqlBean;
import org.liuwww.db.sql.SqlBeanUtil;
import org.liuwww.db.sql.Table;
import org.springframework.jdbc.core.JdbcTemplate;

public class DefaultQueryBeanFactory implements QueryBeanFactory
{

    protected IQueryDao queryDao;

    @Override
    public <T> QueryBean createQueryBean(TableEntity<T> entity)
    {
        return createQueryBean(entity, null);
    }

    @Override
    public QueryBean createQueryBean(String fileName, String tag)
    {
        return createQueryBean(fileName, tag, (JdbcTemplate) null, (Object[]) null);
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
        DefaultQueryBean queryBean = new DefaultQueryBean(getQueryDao(), sqlBean);
        setJdbcTemplate(queryBean, null);
        return queryBean;
    }

    protected IQueryDao getQueryDao()
    {
        return queryDao;
    }

    @Override
    public <T> QueryBean createQueryBean(TableEntity<T> entity, JdbcTemplate jdbcTemplate)
    {
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, jdbcTemplate, null);
        DefaultQueryBean queryBean = new DefaultQueryBean(getQueryDao(), bean);
        queryBean.setTableQuery(true);
        setJdbcTemplate(queryBean, jdbcTemplate);
        return queryBean;
    }

    protected void setJdbcTemplate(QueryBean queryBean, JdbcTemplate jdbcTemplate)
    {
        if (jdbcTemplate != null)
        {
            queryBean.getSqlBean().setJdbcTemplate(jdbcTemplate);
        }
        else
        {
            SqlBean sqlBean = queryBean.getSqlBean();
            if (sqlBean.getJdbcTemplate() == null)
            {
                TableMetaData tmd = sqlBean.getTableMetaData();
                if (tmd != null)
                {
                    sqlBean.setJdbcTemplate(DbContext.getJdbcTemplateForTable(tmd.getTableName()));
                }
                else
                {
                    sqlBean.setJdbcTemplate(SqlBeanUtil.getDefaultJdbcTemplate());
                }
            }
        }

        if (queryBean.getSqlBean().getDbType() == null)
        {
            queryBean.getSqlBean()
                    .setDbType(DbContext.getDbTypeForJdbcTemlate(queryBean.getSqlBean().getJdbcTemplate()));
        }

    }

    @Override
    public QueryBean createQueryBean(String file, String tag, JdbcTemplate jdbcTemplate)
    {
        return createQueryBean(file, tag, jdbcTemplate, (Object[]) null);
    }

    @Override
    public QueryBean createQueryBean(String file, String tag, JdbcTemplate jdbcTemplate, Object... params)
    {

        SqlBean bean = SqlBeanUtil.getOriginalSqlBean(file, tag);
        bean.setParams(params);
        QueryBean queryBean = new DefaultQueryBean(getQueryDao(), bean);
        setJdbcTemplate(queryBean, jdbcTemplate);
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
