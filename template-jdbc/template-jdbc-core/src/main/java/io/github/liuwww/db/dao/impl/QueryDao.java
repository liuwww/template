package io.github.liuwww.db.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.liuwww.db.dao.IQueryDao;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.SqlBean;
import io.github.liuwww.db.sql.util.PaginationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.liuwww.common.execption.DbException;
import io.github.liuwww.common.util.DbNameConverter;

public class QueryDao implements IQueryDao {

    private static final Logger logger = LoggerFactory.getLogger(QueryDao.class);

    @Override
    public Map<String, Object> getMap(SqlBean sqlBean) {
        return sqlBean.getDatabaseExecutor().queryForMap(sqlBean.getSql(), sqlBean.getParams());
    }

    @Override
    public List<Map<String, Object>> getList(SqlBean sqlBean) {
        return sqlBean.getDatabaseExecutor().queryForList(sqlBean.getSql(), sqlBean.getParams());
    }

    @Override
    public <T> T getBean(SqlBean sqlBean, Class<T> clazz) {
        return sqlBean.getDatabaseExecutor().queryForObject(sqlBean.getSql(), sqlBean.getParams(), clazz);
    }

    @Override
    public <T> List<T> getBeanList(SqlBean sqlBean, Class<T> clazz) {
        return sqlBean.getDatabaseExecutor().query(sqlBean.getSql(), sqlBean.getParams(), clazz);
    }

    @Override
    public <T> Page getPage(SqlBean bean, Page pageInfo, Class<T> clazz) throws DbException {
        try {
            String sql = bean.getSql();
            String dbTypeName = bean.getDbType().toString();
            DbType dbType = DbType.getByName(dbTypeName);
            pageInfo.calculateStartAndEndRow();
            String querySql = PaginationFactory.limit(sql, dbType, pageInfo.getStartRow() - 1, pageInfo.getPageSize());

            List<T> list = bean.getDatabaseExecutor().query(querySql, bean.getParams(), clazz);
            if (logger.isDebugEnabled()) {
                logger.debug("query sql:{},params:{}", querySql, bean.getParams());
            }
            pageInfo.setRows(list);
            String totalSql = PaginationFactory.count(sql, dbType);
            Number count = bean.getDatabaseExecutor().queryForObject(totalSql, bean.getParams(), Number.class);
            if (logger.isDebugEnabled()) {
                logger.debug("totalSql sql:{},params:{}", totalSql, bean.getParams());
            }
            pageInfo.setTotal(count.longValue());
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("查询异常，sql:{},params:{}", bean.getSql(), java.util.Arrays.asList(bean.getParams()));
            }
            throw new DbException("查询异常，sql:" + bean.getSql() + ",params:" + java.util.Arrays.asList(bean.getParams()),
                    e);
        }
        return pageInfo;
    }

    @Override
    public Page getPage(SqlBean bean, Page pageInfo) throws DbException {
        try {
            String sql = bean.getSql();
            String dbTypeName = bean.getDbType().toString();
            DbType dbType = DbType.getByName(dbTypeName);
            pageInfo.calculateStartAndEndRow();
            String querySql = PaginationFactory.limit(sql, dbType, pageInfo.getStartRow() - 1, pageInfo.getPageSize());
            if (logger.isDebugEnabled()) {
                logger.debug("query sql:{},params:{}", querySql, bean.getParams());
            }
            List<Map<String, Object>> list = bean.getDatabaseExecutor().queryForList(querySql, bean.getParams());
            list = convertField(list);
            pageInfo.setRows(list);
            Number count = bean.getDatabaseExecutor().queryForObject(PaginationFactory.count(sql, dbType),
                    bean.getParams(),
                    Number.class);
            pageInfo.setTotal(count.longValue());
            return pageInfo;
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("查询异常，sql:{},params:{}", bean.getSql(), java.util.Arrays.asList(bean.getParams()));
            }
            throw new DbException("查询异常，sql:" + bean.getSql() + ",params:" + java.util.Arrays.asList(bean.getParams()),
                    e);
        }
    }

    private List<Map<String, Object>> convertField(List<Map<String, Object>> list) {
        List<Map<String, Object>> rlist = new ArrayList<Map<String, Object>>(list.size());
        for (Map<String, Object> map : list) {
            rlist.add(convertField(map));
        }
        return rlist;
    }

    private Map<String, Object> convertField(Map<String, Object> map) {

        Map<String, Object> rMap = new HashMap<String, Object>(map.size());
        for (String key : map.keySet()) {
            String _key = DbNameConverter.convert(key);
            Object val = map.get(key);
            rMap.put(_key, val);
        }
        return rMap;
    }

    @Override
    public long getCount(SqlBean bean) throws DbException {
        String sql = null;
        try {
            String dbTypeName = bean.getDbType().toString();
            DbType dbType = DbType.getByName(dbTypeName);
            String originalSql = bean.getSql();
            if (originalSql.toUpperCase(java.util.Locale.ROOT).contains("COUNT(*)")) {
                sql = originalSql;
            } else {
                sql = PaginationFactory.count(originalSql, dbType);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("query sql:{},params:{}", sql, bean.getParams());
            }
            Number count = bean.getDatabaseExecutor().queryForObject(sql, bean.getParams(), Number.class);
            return count.longValue();
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("查询异常，sql:{},params:{}", sql, java.util.Arrays.asList(bean.getParams()));
            }
            throw new DbException("查询异常，sql:" + sql + ",params:" + java.util.Arrays.asList(bean.getParams()), e);
        }
    }

    // 为Spring集成提供setter方法
    public void setQueryExecutor(io.github.liuwww.db.executor.DatabaseExecutor queryExecutor) {
        // 这里可以提供额外的初始化逻辑
    }
}
