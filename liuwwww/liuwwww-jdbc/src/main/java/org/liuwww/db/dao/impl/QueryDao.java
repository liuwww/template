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
import com.alibaba.druid.sql.PagerUtils;
import org.liuwww.common.execption.SysException;
import org.liuwww.common.util.DbNameConverter;

public class QueryDao implements IQueryDao
{

    private static Logger logger = LoggerFactory.getLogger(QueryDao.class);

    @Override
    public Map<String, Object> getMap(SqlBean sqlBean)
    {
        try
        {
            logger.debug("query sql:{},params:{}", sqlBean.getSql(), sqlBean.getParams());
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
            logger.error("查询异常，sql:{},params:{}", sqlBean.getSql(), Arrays.asList(sqlBean.getParams()));
            throw new SysException("00001", e);
        }
    }

    @Override
    public List<Map<String, Object>> getList(SqlBean sqlBean)
    {
        try

        {
            logger.debug("query sql:{},params:{}", sqlBean.getSql(), sqlBean.getParams());
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
            e.printStackTrace();
            logger.error("查询异常，sql:{},params:{}", sqlBean.getSql(), Arrays.asList(sqlBean.getParams()));
            throw new SysException("00001", e);
        }
    }

    @Override
    public <T> T getBean(SqlBean sqlBean, Class<T> clazz)
    {
        try
        {
            logger.debug("query sql:{},parmas:{}", sqlBean.getSql(), sqlBean.getParams());
            return sqlBean.getJdbcTemplate().queryForObject(sqlBean.getSql(), sqlBean.getParams(),
                    new BeanPropertyRowMapper<T>(clazz));
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
        catch (DataAccessException e)
        {
            e.printStackTrace();
            logger.error("查询异常，sql:{},params:{}", sqlBean.getSql(), Arrays.asList(sqlBean.getParams()));
            throw new SysException("00001", e);
        }
    }

    @Override
    public <T> List<T> getBeanList(SqlBean sqlBean, Class<T> clazz)
    {
        try
        {
            logger.debug("query sql:{},params:{}", sqlBean.getSql(), sqlBean.getParams());
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
            logger.error("查询异常，sql:{},params:{}", sqlBean.getSql(), Arrays.asList(sqlBean.getParams()));
            throw new SysException("00001", e);
        }
    }

    @Override
    public <T> Page getPage(SqlBean bean, Page pageInfo, Class<T> clazz) throws SysException
    {
        try
        {
            String sql = bean.getSql();
            String dbType = bean.getDbType().toString();
            String querySql = PagerUtils.limit(sql, dbType, pageInfo.getStartRow(), pageInfo.getPageSize());
            logger.debug("query sql:{},params:{}", querySql, bean.getParams());
            List<T> list = bean.getJdbcTemplate().query(querySql, bean.getParams(),
                    new BeanPropertyRowMapper<T>(clazz));
            pageInfo.setRows(list);
            Number count = bean.getJdbcTemplate().queryForObject(PagerUtils.count(sql, dbType), bean.getParams(),
                    Number.class);
            pageInfo.setTotal(count.intValue());
        }
        catch (DataAccessException e)
        {
            e.printStackTrace();
            logger.error("查询异常，sql:{},params:{}", bean.getSql(), Arrays.asList(bean.getParams()));
            throw new SysException("00001", e);
        }
        return pageInfo;
    }

    @Override
    public Page getPage(SqlBean bean, Page pageInfo) throws SysException
    {
        try
        {
            String sql = bean.getSql();
            String dbType = bean.getDbType().toString();
            String querySql = PagerUtils.limit(sql, dbType, pageInfo.getStartRow(), pageInfo.getPageSize());
            logger.debug("query sql:{},params:{}", querySql, bean.getParams());
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
            e.printStackTrace();
            logger.error("查询异常，sql:{},params:{}", bean.getSql(), Arrays.asList(bean.getParams()));
            throw new SysException("00001", e);
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
    public int getCount(SqlBean bean) throws SysException
    {
        String sql = null;
        try
        {
            sql = PagerUtils.count(bean.getSql(), bean.getDbType().toString());
            logger.debug("query sql:{},params:{}", sql, bean.getParams());
            Number count = bean.getJdbcTemplate().queryForObject(sql, bean.getParams(), Number.class);
            return count.intValue();
        }
        catch (DataAccessException e)
        {
            e.printStackTrace();
            logger.error("查询异常，sql:{},params:{}", sql, Arrays.asList(bean.getParams()));
            throw new SysException("00001", e);
        }
    }

}
