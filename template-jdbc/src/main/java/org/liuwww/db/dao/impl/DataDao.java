package org.liuwww.db.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.liuwww.common.execption.DbException;
import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.dao.IDataDao;
import org.liuwww.db.sql.SqlBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;

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
    public String insert4AutoInc(final SqlBean bean) throws DbException
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
			Map<String, Object> keys = keyHolder.getKeys();
			if (keys.size() == 0) {
				return keyHolder.getKey().toString();
			} else {
				TableMetaData metaData = bean.getTableMetaData();
				return keys.get(metaData.getIdColumn().getColumnName()).toString();
			}
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
    public String[] insert4AutoInc(final String sql, final List<Object[]> batchArgs, JdbcTemplate jdbcTemplate)
            throws DbException
	{
		return insert4AutoInc(sql, batchArgs, jdbcTemplate, null);
	}

	@Override
	public String[] insert4AutoInc(final String sql, final List<Object[]> batchArgs, JdbcTemplate jdbcTemplate,
			final TableMetaData tmd) throws DbException
    {
        try
        {
            return jdbcTemplate.execute(new PreparedStatementCreator()
            {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException
                {
                    PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    for (Object[] params : batchArgs)
                    {
                        for (int i = 1, len = params.length; i <= len; i++)
                        {
                            StatementCreatorUtils.setParameterValue(ps, i, SqlTypeValue.TYPE_UNKNOWN, params[i - 1]);
                        }
                        ps.addBatch();
                    }
                    return ps;
                }
            }, new PreparedStatementCallback<String[]>()
            {

                @Override
                public String[] doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException
                {
                    ps.executeBatch();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys != null)
                    {
                        try
                        {
                            RowMapperResultSetExtractor<Map<String, Object>> rse = new RowMapperResultSetExtractor<Map<String, Object>>(
                                    new ColumnMapRowMapper(), 0);
                            List<Map<String, Object>> list = rse.extractData(keys);
                            String[] keyArr = new String[list.size()];
                            for (int i = 0; i < keyArr.length; i++)
                            {
                                Map<String, Object> km = list.get(i);
								if (km.size() == 1)
                                {
									Iterator<Object> keyIter = km.values().iterator();
									if (keyIter.hasNext()) {
										keyArr[i] = ((Number) keyIter.next()).toString();
									}
                                }
								else {
									if (tmd == null) {
										throw new DbException("GeneratedKeys 返回多个值，无法确定id值");
									}
									else {
										keyArr[i] = km.get(tmd.getIdColumn().getColumnName()).toString();
									}
								}
                            }
                            return keyArr;
                        }
                        finally
                        {
                            JdbcUtils.closeResultSet(keys);
                        }
                    }
                    return null;

                }

            });
        }
        catch (Exception e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("执行sql异常,sql:{},params:{},异常：{}", sql, batchArgs, e);
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
