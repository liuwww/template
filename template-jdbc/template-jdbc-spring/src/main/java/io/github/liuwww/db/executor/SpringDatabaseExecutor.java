package io.github.liuwww.db.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;

/**
 * 基于Spring JdbcTemplate的数据库执行器实现
 * 
 * @author liuwww
 * @date 2026-03-01
 */
public class SpringDatabaseExecutor implements DatabaseExecutor {

    private JdbcTemplate jdbcTemplate;

    public SpringDatabaseExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int update(String sql, Object[] params) {
        if (params != null && params.length > 0) {
            return jdbcTemplate.update(sql, params);
        } else {
            return jdbcTemplate.update(sql);
        }
    }

    @Override
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public <T> List<T> query(String sql, Object[] params, Class<T> clazz) {
        if (params != null && params.length > 0) {
            return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(clazz));
        } else {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(clazz));
        }
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, Object[] params) {
        if (params != null && params.length > 0) {
            return jdbcTemplate.queryForList(sql, params);
        } else {
            return jdbcTemplate.queryForList(sql);
        }
    }

    @Override
    public <T> T queryForObject(String sql, Object[] params, Class<T> clazz) {
        try {
            // 基本类型和 String、Number 等使用单列映射
            if (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || clazz == String.class
                    || java.util.Date.class.isAssignableFrom(clazz)) {
                if (params != null && params.length > 0) {
                    return jdbcTemplate.queryForObject(sql, params, clazz);
                } else {
                    return jdbcTemplate.queryForObject(sql, clazz);
                }
            }
            // 复杂对象使用 BeanPropertyRowMapper 进行属性映射
            if (params != null && params.length > 0) {
                return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<T>(clazz), params);
            } else {
                return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<T>(clazz));
            }
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object[] params) {
        try {
            if (params != null && params.length > 0) {
                return jdbcTemplate.queryForMap(sql, params);
            } else {
                return jdbcTemplate.queryForMap(sql);
            }
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public long queryCount(String sql, Object[] params) {
        Long result = queryForObject(sql, params, Long.class);
        return result != null ? result : 0L;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
    
    /**
     * 执行插入操作并返回自动生成的主键
     * 
     * @param sql SQL语句
     * @param params 参数
     * @return 主键值
     */
    @Override
    public String insertWithAutoGenKey(String sql, Object[] params) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, params[i]);
                    }
                }
                return ps;
            }
        }, keyHolder);
        
        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().toString();
        }
        return null;
    }
    
    /**
     * 批量执行插入操作并返回自动生成的主键
     * 
     * @param sql SQL语句
     * @param batchArgs 批量参数
     * @return 主键数组
     */
    @Override
    public String[] batchInsertWithAutoGenKey(final String sql, final List<Object[]> batchArgs) {
        return jdbcTemplate.execute(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                for (Object[] params : batchArgs) {
                    for (int i = 0; i < params.length; i++) {
                        StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, params[i]);
                    }
                    ps.addBatch();
                }
                return ps;
            }
        }, new PreparedStatementCallback<String[]>() {
            @Override
            public String[] doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.executeBatch();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys != null) {
                    try {
                        List<String> keyList = new ArrayList<>();
                        while (keys.next()) {
                            keyList.add(keys.getString(1));
                        }
                        return keyList.toArray(new String[0]);
                    } finally {
                        JdbcUtils.closeResultSet(keys);
                    }
                }
                return null;
            }
        });
    }
}