package org.liuwww.db.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.liuwww.db.dao.IDataDao;
import org.liuwww.db.sql.SqlBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.liuwww.common.execption.DbException;

public class DataDao implements IDataDao
{

    private Logger logger = LoggerFactory.getLogger(DataDao.class);

    @Override
    public int executeUpdate(String sql, Object[] params, JdbcTemplate jdbcTemplate) throws DbException
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("data sql:{},params:{}", sql, params);
            }
            return jdbcTemplate.update(sql, params);
        }
        catch (DataAccessException e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("执行sql异常,sql:{},params:{},异常：{}", sql, Arrays.asList(params), e);
            }
            throw new DbException("sql执行错误", e);
        }
    }

    @Override
    public int executeUpdate(SqlBean bean) throws DbException
    {
        return executeUpdate(bean.getSql(), bean.getParams(), bean.getJdbcTemplate());
    }

    @Override
    public String insertByMysql(final SqlBean bean) throws DbException
    {
        try
        {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            bean.getJdbcTemplate().update(new PreparedStatementCreator()
            {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException
                {
                    PreparedStatement ps = con.prepareStatement(bean.getSql(), Statement.RETURN_GENERATED_KEYS);
                    Object[] params = bean.getParams();
                    for (int i = 1, len = params.length; i <= len; i++)
                    {
                        StatementCreatorUtils.setParameterValue(ps, i, SqlTypeValue.TYPE_UNKNOWN, params[i - 1]);
                    }
                    return ps;
                }
            }, keyHolder);
            return keyHolder.getKey().toString();
        }
        catch (Exception e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("执行sql异常,sql:{},params:{},异常：{}", bean.getSql(), Arrays.asList(bean.getParams()), e);
            }
            throw new DbException("sql执行错误", e);
        }

    }

    @Override
    public int[] executeBatchUpdate(String sql, List<Object[]> batchArgs, JdbcTemplate jdbcTemplate) throws DbException
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("data sql:{},params:{}", sql, batchArgs);
            }
            return jdbcTemplate.batchUpdate(sql, batchArgs);
        }
        catch (DataAccessException e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("执行sql异常,sql:{},params:{},异常：{}", sql, batchArgs, e);
            }
            throw new DbException("sql执行错误", e);
        }
    }

}
