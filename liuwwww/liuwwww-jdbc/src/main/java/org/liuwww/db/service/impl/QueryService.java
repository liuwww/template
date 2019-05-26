package org.liuwww.db.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.liuwww.db.context.DbContext;
import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.dao.IQueryDao;
import org.liuwww.db.page.Page;
import org.liuwww.db.query.DefaultQueryBeanFactory;
import org.liuwww.db.query.Entity;
import org.liuwww.db.query.QueryBeanFactory;
import org.liuwww.db.service.IQueryService;
import org.liuwww.db.sql.SqlBean;
import org.liuwww.db.sql.SqlBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.liuwww.common.execption.BusinessExecption;

public class QueryService extends DefaultQueryBeanFactory implements IQueryService, QueryBeanFactory
{
    @Autowired
    private IQueryDao queryDao;

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getBeanList(Entity<T> entity)
    {
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null);
        checkSqlBeanJdbcTemplate(entity.tableName(), null, bean);
        return (List<T>) queryDao.getBeanList(bean, entity.getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(Entity<T> entity)
    {
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null);
        checkSqlBeanJdbcTemplate(entity.tableName(), null, bean);
        return (T) queryDao.getBean(bean, entity.getClass());
    }

    @Override
    public <T> Page getPage(Entity<T> entity, Page pagInfo)
    {
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null);
        checkSqlBeanJdbcTemplate(entity.tableName(), null, bean);
        return queryDao.getPage(bean, pagInfo, entity.getClass());
    }

    @Override
    public <T> int getCount(Entity<T> entity)
    {
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null);
        checkSqlBeanJdbcTemplate(entity.tableName(), null, bean);
        return queryDao.getCount(bean);
    }

    @Override
    public Page getPage(String tableName, Map<String, Object> paramMap)
    {
        Page pageInfo = new Page();
        Object pageNum = paramMap.get("pageNum");
        if (pageNum != null)
        {
            int _page = Integer.parseInt(pageNum.toString());
            pageInfo.setPageNum(_page);
        }
        Object pageSize = paramMap.get("pageSize");
        if (pageSize != null)
        {
            int _pageSize = Integer.parseInt(pageSize.toString());
            pageInfo.setPages(_pageSize);
        }
        return getPage(tableName, pageInfo, paramMap);
    }

    @Override
    public Page getPage(String tableName, Page pageInfo, Map<String, Object> paramMap)
    {
        SqlBean bean = SqlBeanUtil.getSqlBean(tableName, paramMap);
        checkSqlBeanJdbcTemplate(tableName, null, bean);
        return queryDao.getPage(bean, pageInfo);
    }

    @Override
    public List<Map<String, Object>> getList(String tableName, Map<String, Object> paramMap)
    {
        SqlBean bean = SqlBeanUtil.getSqlBean(tableName, paramMap);
        checkSqlBeanJdbcTemplate(tableName, null, bean);
        return queryDao.getList(bean);
    }

    @Override
    protected IQueryDao getQueryDao()
    {
        return this.queryDao;
    }

    @Override
    public Map<String, Object> getMap(String tableName, Serializable id)
    {
        TableMetaData tmd = getTableMetaData(tableName);
        String idName = tmd.getIdColumn().getColumnName();
        return createQueryBean(tableName).getCompare().eq(idName, id).getQueryBean().getMap();
    }

    @Override
    public <T> T getEntity(String tableName, Serializable id, Class<T> clazz)
    {
        TableMetaData tmd = getTableMetaData(tableName);
        String idName = tmd.getIdColumn().getColumnName();
        return createQueryBean(tableName).getCompare().eq(idName, id).getQueryBean().getBean(clazz);
    }

    private TableMetaData getTableMetaData(String tableName)
    {
        TableMetaData tmd = DbContext.getTableMetaContext().getTableMetaData(tableName);
        if (tmd == null)
        {
            throw new BusinessExecption("表(视图)【" + tableName + "】不存在或没加载");
        }
        return tmd;
    }

    protected void checkSqlBeanJdbcTemplate(String tableName, JdbcTemplate jdbcTemplate, SqlBean bean)
    {
        if (jdbcTemplate != null)
        {
            bean.setJdbcTemplate(jdbcTemplate);
        }
        if (bean.getJdbcTemplate() == null)
        {
            bean.setJdbcTemplate(DbContext.getJdbcTemplateForTable(tableName));
        }
    }

}
