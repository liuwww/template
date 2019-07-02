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
    public String insertByMysql(SqlBean bean) throws DbException;

    public int[] executeBatchUpdate(String sql, List<Object[]> batchArgs, JdbcTemplate jdbcTemplate)
            throws DbException;

}
