package io.github.liuwww.db.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.liuwww.db.dao.IQueryDao;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.SqlBean;
import io.github.liuwww.db.sql.util.PaginationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.liuwww.common.execption.DbException;
import io.github.liuwww.common.util.DbNameConverter;

/**
 * 基于Spring的QueryDao实现
 * <p>
 * 统一通过 SqlBean 中携带的 {@link DatabaseExecutor} 执行 SQL。
 * 无论 executor 是 {@link io.github.liuwww.db.executor.SpringDatabaseExecutor} 还是
 * {@link io.github.liuwww.db.executor.impl.JdbcDatabaseExecutor}，都委托给 executor 执行，
 * 避免冗余的 instanceof 判断（SpringDatabaseExecutor 内部已封装 JdbcTemplate 调用）。
 * </p>
 *
 * @author liuwww
 * @date 2026-03-01
 */
public class SpringQueryDao implements IQueryDao {

    private static Logger logger = LoggerFactory.getLogger(SpringQueryDao.class);

    @Override
    public Map<String, Object> getMap(SqlBean sqlBean) {
        DatabaseExecutor executor = requireExecutor(sqlBean);
        Map<String, Object> map = executor.queryForMap(sqlBean.getSql(), sqlBean.getParams());
        return map == null ? null : convertField(map);
    }

    @Override
    public List<Map<String, Object>> getList(SqlBean sqlBean) {
        DatabaseExecutor executor = requireExecutor(sqlBean);
        List<Map<String, Object>> list = executor.queryForList(sqlBean.getSql(), sqlBean.getParams());
        return list == null ? null : convertField(list);
    }

    @Override
    public <T> T getBean(SqlBean sqlBean, Class<T> clazz) {
        DatabaseExecutor executor = requireExecutor(sqlBean);
        return executor.queryForObject(sqlBean.getSql(), sqlBean.getParams(), clazz);
    }

    @Override
    public <T> List<T> getBeanList(SqlBean sqlBean, Class<T> clazz) {
        DatabaseExecutor executor = requireExecutor(sqlBean);
        return executor.query(sqlBean.getSql(), sqlBean.getParams(), clazz);
    }

    @Override
    public <T> Page getPage(SqlBean bean, Page pageInfo, Class<T> clazz) throws DbException {
        try {
            String sql = bean.getSql();
            DbType dbType = bean.getDbType();
            pageInfo.calculateStartAndEndRow();
            String querySql = PaginationFactory.limit(sql, dbType, pageInfo.getStartRow() - 1, pageInfo.getPageSize());

            DatabaseExecutor executor = requireExecutor(bean);
            List<T> list = executor.query(querySql, bean.getParams(), clazz);
            if (logger.isDebugEnabled()) {
                logger.debug("query sql:{},params:{}", querySql, bean.getParams());
            }
            pageInfo.setRows(list);

            String totalSql = PaginationFactory.count(sql, dbType);
            Number count = executor.queryForObject(totalSql, bean.getParams(), Number.class);
            if (logger.isDebugEnabled()) {
                logger.debug("totalSql sql:{},params:{}", totalSql, bean.getParams());
            }
            pageInfo.setTotal(count.longValue());
        } catch (Exception e) {
            throw new DbException("查询异常，sql:" + bean.getSql() + ",params:" + Arrays.asList(bean.getParams()), e);
        }
        return pageInfo;
    }

    @Override
    public Page getPage(SqlBean bean, Page pageInfo) throws DbException {
        try {
            String sql = bean.getSql();
            DbType dbType = bean.getDbType();
            pageInfo.calculateStartAndEndRow();
            String querySql = PaginationFactory.limit(sql, dbType, pageInfo.getStartRow() - 1, pageInfo.getPageSize());
            if (logger.isDebugEnabled()) {
                logger.debug("query sql:{},params:{}", querySql, bean.getParams());
            }

            DatabaseExecutor executor = requireExecutor(bean);
            List<Map<String, Object>> list = executor.queryForList(querySql, bean.getParams());
            list = convertField(list);
            pageInfo.setRows(list);

            Number count = executor.queryForObject(PaginationFactory.count(sql, dbType), bean.getParams(),
                    Number.class);
            pageInfo.setTotal(count.longValue());
            return pageInfo;
        } catch (Exception e) {
            throw new DbException("查询异常，sql:" + bean.getSql() + ",params:" + Arrays.asList(bean.getParams()), e);
        }
    }

    @Override
    public long getCount(SqlBean bean) throws DbException {
        String sql = null;
        try {
            DbType dbType = bean.getDbType();
            String originalSql = bean.getSql();
            if (originalSql.toUpperCase(java.util.Locale.ROOT).contains("COUNT(*)")) {
                sql = originalSql;
            } else {
                sql = PaginationFactory.count(originalSql, dbType);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("query sql:{},params:{}", sql, bean.getParams());
            }
            DatabaseExecutor executor = requireExecutor(bean);
            Number count = executor.queryForObject(sql, bean.getParams(), Number.class);
            return count.longValue();
        } catch (Exception e) {
            throw new DbException("查询异常，sql:" + sql + ",params:" + Arrays.asList(bean.getParams()), e);
        }
    }

    private DatabaseExecutor requireExecutor(SqlBean bean) throws DbException {
        DatabaseExecutor executor = bean.getDatabaseExecutor();
        if (executor == null) {
            throw new DbException("SqlBean 未设置 DatabaseExecutor");
        }
        return executor;
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
}
