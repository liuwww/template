package org.liuwww.db.dao;

import java.util.List;

import org.liuwww.db.sql.SqlBean;
import org.springframework.jdbc.core.JdbcTemplate;

import org.liuwww.common.execption.DbException;

public interface IDataDao
{

    public int executeUpdate(SqlBean bean) throws DbException;

    public int executeUpdate(String sql, Object[] params, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc:返回主键
     * @Date 2017年9月1日下午3:08:05
     * @author liuwww
     * @param bean
     * @return
     * @throws DbException
     */
    public String insert4AutoInc(SqlBean bean) throws DbException;

    public int[] executeBatchUpdate(String sql, List<Object[]> batchArgs, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @desc:返回主键数组
     * @Date:2019年8月8日下午8:47:09
     * @author liuwww
     * @param sql
     * @param batchArgs
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public String[] insert4AutoInc(final String sql, final List<Object[]> batchArgs, JdbcTemplate jdbcTemplate)
            throws DbException;

}
