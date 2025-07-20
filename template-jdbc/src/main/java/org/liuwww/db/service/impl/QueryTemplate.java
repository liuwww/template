package org.liuwww.db.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.liuwww.db.context.DbContext;
import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.dao.IQueryDao;
import org.liuwww.db.dao.impl.QueryDao;
import org.liuwww.db.page.Page;
import org.liuwww.db.query.DefaultQueryBeanFactory;
import org.liuwww.db.query.QueryBeanFactory;
import org.liuwww.db.service.IQueryTemplate;
import org.liuwww.db.sql.SqlBean;
import org.liuwww.db.sql.SqlBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.liuwww.common.entity.Order;
import org.liuwww.common.entity.TableEntity;
import org.liuwww.common.execption.DbException;

public class QueryTemplate extends DefaultQueryBeanFactory implements IQueryTemplate, QueryBeanFactory {

    public QueryTemplate() {
        this.queryDao = new QueryDao();
    }

    @Autowired
    public QueryTemplate(IQueryDao queryDao) {
        this.queryDao = queryDao;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getBeanList(TableEntity<T> entity) {
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null, null);
        checkSqlBeanJdbcTemplate(entity.tableName(), null, bean);
        return (List<T>) queryDao.getBeanList(bean, entity.getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(TableEntity<T> entity) {
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null, null);
        checkSqlBeanJdbcTemplate(entity.tableName(), null, bean);
        return (T) queryDao.getBean(bean, entity.getClass());
    }

    @Override
    public <T> Page getPage(TableEntity<T> entity, Page page) {
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null, new Order(page.getSortName(), page.getSortOrder()));
        checkSqlBeanJdbcTemplate(entity.tableName(), null, bean);
        return queryDao.getPage(bean, page, entity.getClass());
    }

    @Override
    public <T> long getCount(TableEntity<T> entity) {
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null, null);
        checkSqlBeanJdbcTemplate(entity.tableName(), null, bean);
        return queryDao.getCount(bean);
    }

    @Override
    public Page getPage(String tableName, Map<String, Object> paramMap) {
        Page pageInfo = new Page();
        Object pageNum = paramMap.get("pageNum");
        if (pageNum != null) {
            int _page = Integer.parseInt(pageNum.toString());
            pageInfo.setPageNum(_page);
        }
        Object pageSize = paramMap.get("pageSize");
        if (pageSize != null) {
            int _pageSize = Integer.parseInt(pageSize.toString());
            pageInfo.setPages(_pageSize);
        }
        return getPage(tableName, pageInfo, paramMap);
    }

    @Override
    public Page getPage(String tableName, Page page, Map<String, Object> paramMap) {
        SqlBean bean = SqlBeanUtil.getSqlBean(tableName, paramMap, new Order(page.getSortName(), page.getSortOrder()));
        checkSqlBeanJdbcTemplate(tableName, null, bean);
        return queryDao.getPage(bean, page);
    }

    @Override
    public List<Map<String, Object>> getMapList(String tableName, Map<String, Object> paramMap) {
        SqlBean bean = SqlBeanUtil.getSqlBean(tableName, paramMap, null);
        checkSqlBeanJdbcTemplate(tableName, null, bean);
        return queryDao.getList(bean);
    }

    @Override
    protected IQueryDao getQueryDao() {
        return this.queryDao;
    }

    @Override
    public Map<String, Object> getMap(String tableName, Serializable id) {
        TableMetaData tmd = getTableMetaData(tableName);
        String idName = tmd.getIdColumn().getColumnName();
        return createQueryBean(tableName).getCompare().eq(idName, id).getQueryBean().getMap();
    }

    @Override
    public <T> T getBean(String tableName, Serializable id, Class<T> clazz) {
        TableMetaData tmd = getTableMetaData(tableName);
        String idName = tmd.getIdColumn().getColumnName();
        return createQueryBean(tableName).getCompare().eq(idName, id).getQueryBean().getBean(clazz);
    }

    private TableMetaData getTableMetaData(String tableName) {
        TableMetaData tmd = DbContext.getTableMetaContext().getTableMetaData(tableName);
        if (tmd == null) {
            throw new DbException("表(视图)【" + tableName + "】不存在或没加载");
        }
        return tmd;
    }

    protected void checkSqlBeanJdbcTemplate(String tableName, JdbcTemplate jdbcTemplate, SqlBean bean) {
        if (jdbcTemplate != null) {
            bean.setJdbcTemplate(jdbcTemplate);
        }
        if (bean.getJdbcTemplate() == null) {
            bean.setJdbcTemplate(DbContext.getJdbcTemplateForTable(tableName));
        }
    }

}
