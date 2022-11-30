package org.liuwww.db.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.liuwww.db.dao.IQueryDao;
import org.liuwww.db.page.Page;
import org.liuwww.db.sql.SqlBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.PagerUtils;
import org.liuwww.common.execption.DbException;
import org.liuwww.common.util.DbNameConverter;

public class QueryDao implements IQueryDao
{

    private static Logger logger = LoggerFactory.getLogger(QueryDao.class);

    @Override
    public Map<String, Object> getMap(SqlBean sqlBean)
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("query sql:{},params:{}", sqlBean.getSql(), sqlBean.getParams());
            }
            Map<String, Object> map = sqlBean.getJdbcTemplate().queryForMap(sqlBean.getSql(), sqlBean.getParams());
            return convertField(map);
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
        catch (DataAccessException e)
        {
            e.printStackTrace();
            if (logger.isErrorEnabled())
            {
                logger.error("查询异常，sql:{},params:{}", sqlBean.getSql(), Arrays.asList(sqlBean.getParams()));
            }
            throw new DbException("00001", e);
        }
    }

    @Override
    public List<Map<String, Object>> getList(SqlBean sqlBean)
    {
        try

        {
            if (logger.isDebugEnabled())
            {
                logger.debug("query sql:{},params:{}", sqlBean.getSql(), sqlBean.getParams());
            }
            List<Map<String, Object>> list = sqlBean.getJdbcTemplate().queryForList(sqlBean.getSql(),
                    sqlBean.getParams());
            return convertField(list);
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
        catch (DataAccessException e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("查询异常，sql:{},params:{}", sqlBean.getSql(), Arrays.asList(sqlBean.getParams()));
            }
            throw new DbException("查询异常，sql:" + sqlBean.getSql() + ",params:" + Arrays.asList(sqlBean.getParams()), e);
        }
    }

    @Override
    public <T> T getBean(SqlBean sqlBean, Class<T> clazz)
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("query sql:{},parmas:{}", sqlBean.getSql(), sqlBean.getParams());
            }
            return sqlBean.getJdbcTemplate().queryForObject(sqlBean.getSql(), sqlBean.getParams(),
                    new BeanPropertyRowMapper<T>(clazz));
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
        catch (DataAccessException e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("查询异常，sql:{},params:{}", sqlBean.getSql(), Arrays.asList(sqlBean.getParams()));
            }
            throw new DbException("查询异常，sql:" + sqlBean.getSql() + ",params:" + Arrays.asList(sqlBean.getParams()), e);
        }
    }

    @Override
    public <T> List<T> getBeanList(SqlBean sqlBean, Class<T> clazz)
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("query sql:{},params:{}", sqlBean.getSql(), sqlBean.getParams());
            }
            return sqlBean.getJdbcTemplate().query(sqlBean.getSql(), sqlBean.getParams(),
                    new BeanPropertyRowMapper<T>(clazz));
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
        catch (DataAccessException e)
        {
            e.printStackTrace();
            if (logger.isErrorEnabled())
            {
                logger.error("查询异常，sql:{},params:{}", sqlBean.getSql(), Arrays.asList(sqlBean.getParams()));
            }
            throw new DbException("查询异常，sql:" + sqlBean.getSql() + ",params:" + Arrays.asList(sqlBean.getParams()), e);
        }
    }

    @Override
    public <T> Page getPage(SqlBean bean, Page pageInfo, Class<T> clazz) throws DbException
    {
        try
        {
            String sql = bean.getSql();
            String dbTypeName = bean.getDbType().toString();
            DbType dbType = DbType.of(dbTypeName);
            pageInfo.calculateStartAndEndRow();
            String querySql = PagerUtils.limit(sql, dbType, pageInfo.getStartRow() - 1, pageInfo.getPageSize());

            List<T> list = bean.getJdbcTemplate().query(querySql, bean.getParams(),
                    new BeanPropertyRowMapper<T>(clazz));
            if (logger.isDebugEnabled())
            {
                logger.debug("query sql:{},params:{}", querySql, bean.getParams());
            }
            pageInfo.setRows(list);
            String totalSql = PagerUtils.count(sql, dbType);
            Number count = bean.getJdbcTemplate().queryForObject(totalSql, bean.getParams(), Number.class);
            if (logger.isDebugEnabled())
            {
                logger.debug("totalSql sql:{},params:{}", totalSql, bean.getParams());
            }
            pageInfo.setTotal(count.longValue());
        }
        catch (DataAccessException e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("查询异常，sql:{},params:{}", bean.getSql(), Arrays.asList(bean.getParams()));
            }
            throw new DbException("查询异常，sql:" + bean.getSql() + ",params:" + Arrays.asList(bean.getParams()), e);
        }
        return pageInfo;
    }

    @Override
    public Page getPage(SqlBean bean, Page pageInfo) throws DbException
    {
        try
        {
            String sql = bean.getSql();
            String dbTypeName = bean.getDbType().toString();
            DbType dbType = DbType.of(dbTypeName);
            pageInfo.calculateStartAndEndRow();
            String querySql = PagerUtils.limit(sql, dbType, pageInfo.getStartRow() - 1, pageInfo.getPageSize());
            if (logger.isDebugEnabled())
            {
                logger.debug("query sql:{},params:{}", querySql, bean.getParams());
            }
            List<Map<String, Object>> list = bean.getJdbcTemplate().queryForList(querySql, bean.getParams());
            list = convertField(list);
            pageInfo.setRows(list);
            Number count = bean.getJdbcTemplate().queryForObject(PagerUtils.count(sql, dbType), bean.getParams(),
                    Number.class);
            pageInfo.setTotal(count.intValue());
            return pageInfo;
        }
        catch (DataAccessException e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("查询异常，sql:{},params:{}", bean.getSql(), Arrays.asList(bean.getParams()));
            }
            throw new DbException("查询异常，sql:" + bean.getSql() + ",params:" + Arrays.asList(bean.getParams()), e);
        }
    }

    private List<Map<String, Object>> convertField(List<Map<String, Object>> list)
    {
        List<Map<String, Object>> rlist = new ArrayList<Map<String, Object>>(list.size());
        for (Map<String, Object> map : list)
        {
            rlist.add(convertField(map));
        }
        return rlist;
    }

    private Map<String, Object> convertField(Map<String, Object> map)
    {

        Map<String, Object> rMap = new HashMap<String, Object>(map.size());
        for (String key : map.keySet())
        {
            String _key = DbNameConverter.convert(key);
            Object val = map.get(key);
            rMap.put(_key, val);
        }
        return rMap;
    }

    @Override
    public long getCount(SqlBean bean) throws DbException
    {
        String sql = null;
        try
        {
            String dbTypeName = bean.getDbType().toString();
            DbType dbType = DbType.of(dbTypeName);
            sql = PagerUtils.count(bean.getSql(), dbType);
            if (logger.isDebugEnabled())
            {
                logger.debug("query sql:{},params:{}", sql, bean.getParams());
            }
            Number count = bean.getJdbcTemplate().queryForObject(sql, bean.getParams(), Number.class);
            return count.longValue();
        }
        catch (DataAccessException e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("查询异常，sql:{},params:{}", sql, Arrays.asList(bean.getParams()));
            }
            throw new DbException("查询异常，sql:" + sql + ",params:" + Arrays.asList(bean.getParams()), e);
        }
    }

}
