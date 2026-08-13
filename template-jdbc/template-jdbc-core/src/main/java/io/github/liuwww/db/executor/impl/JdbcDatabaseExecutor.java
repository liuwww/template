package io.github.liuwww.db.executor.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import io.github.liuwww.common.execption.DbException;
import io.github.liuwww.db.executor.DatabaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于标准JDBC的数据库执行器实现
 * 
 * @author liuwww
 * @date 2026-03-01
 */
public class JdbcDatabaseExecutor implements DatabaseExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JdbcDatabaseExecutor.class);

    private Connection connection;

    private DataSource dataSource;

    public JdbcDatabaseExecutor(Connection connection) {
        this.connection = connection;
    }

    public JdbcDatabaseExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取数据库连接。
     * <p>
     * 若构造时注入了 Connection（如事务上下文），直接返回该连接，调用方不应关闭；
     * 否则从 DataSource 获取一个新连接，调用方使用完毕后必须关闭。
     * </p>
     *
     * @return 数据库连接
     * @throws DbException 获取连接失败时抛出
     */
    private Connection getConnection() {
        if (connection != null) {
            return connection;
        }
        if (dataSource != null) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw new DbException("获取数据库连接失败", e);
            }
        }
        throw new DbException("未配置 Connection 或 DataSource，无法获取数据库连接");
    }

    /**
     * 判断给定连接是否由本执行器管理生命周期（即从 DataSource 获取的连接）。
     * 注入的 Connection 由外部管理，不应由本执行器关闭。
     */
    private boolean isManagedConnection(Connection conn) {
        return conn != connection;
    }

    @Override
    public int update(String sql, Object[] params) {
        Connection conn = getConnection();
        boolean closeConn = isManagedConnection(conn);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("执行更新操作失败", e);
        } finally {
            if (closeConn) {
                closeQuietly(conn);
            }
        }
    }

    @Override
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        Connection conn = getConnection();
        boolean closeConn = isManagedConnection(conn);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] params : batchArgs) {
                setParameters(pstmt, params);
                pstmt.addBatch();
            }
            return pstmt.executeBatch();
        } catch (SQLException e) {
            throw new DbException("执行批量更新操作失败", e);
        } finally {
            if (closeConn) {
                closeQuietly(conn);
            }
        }
    }

    @Override
    public <T> List<T> query(String sql, Object[] params, Class<T> clazz) {
        Connection conn = getConnection();
        boolean closeConn = isManagedConnection(conn);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    T obj = createObjectFromResultSet(rs, clazz);
                    results.add(obj);
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DbException("执行查询操作失败", e);
        } finally {
            if (closeConn) {
                closeQuietly(conn);
            }
        }
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, Object[] params) {
        Connection conn = getConnection();
        boolean closeConn = isManagedConnection(conn);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Map<String, Object>> results = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        String columnName = rs.getMetaData().getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DbException("执行查询操作失败", e);
        } finally {
            if (closeConn) {
                closeQuietly(conn);
            }
        }
    }

    @Override
    public <T> T queryForObject(String sql, Object[] params, Class<T> clazz) {
        Connection conn = getConnection();
        boolean closeConn = isManagedConnection(conn);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createObjectFromResultSet(rs, clazz);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("执行查询操作失败", e);
        } finally {
            if (closeConn) {
                closeQuietly(conn);
            }
        }
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object[] params) {
        Connection conn = getConnection();
        boolean closeConn = isManagedConnection(conn);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        String columnName = rs.getMetaData().getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    return row;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("执行查询操作失败", e);
        } finally {
            if (closeConn) {
                closeQuietly(conn);
            }
        }
    }

    @Override
    public long queryCount(String sql, Object[] params) {
        Connection conn = getConnection();
        boolean closeConn = isManagedConnection(conn);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DbException("执行查询计数操作失败", e);
        } finally {
            if (closeConn) {
                closeQuietly(conn);
            }
        }
    }

    /**
     * 安静地关闭连接，忽略关闭过程中的 SQLException。
     *
     * @param conn 待关闭的连接，可为 null
     */
    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // 关闭连接时的异常忽略，避免掩盖业务异常
            }
        }
    }

    @Override
    public String insertWithAutoGenKey(String sql, Object[] params) {
        Connection conn = getConnection();
        boolean closeConn = isManagedConnection(conn);
        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            setParameters(pstmt, params);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("执行插入并获取自增主键失败", e);
        } finally {
            if (closeConn) {
                closeQuietly(conn);
            }
        }
    }

    @Override
    public String[] batchInsertWithAutoGenKey(String sql, List<Object[]> batchArgs) {
        Connection conn = getConnection();
        boolean closeConn = isManagedConnection(conn);
        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            for (Object[] params : batchArgs) {
                setParameters(pstmt, params);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            List<String> keys = new ArrayList<>();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                while (rs.next()) {
                    keys.add(rs.getString(1));
                }
            }
            // 校验返回的主键数量与批次大小是否一致
            if (keys.size() != batchArgs.size()) {
                throw new DbException("批量插入自增主键数量不匹配: 期望 " + batchArgs.size() + " 个，实际获取 " + keys.size() + " 个");
            }
            return keys.toArray(new String[0]);
        } catch (SQLException e) {
            throw new DbException("执行批量插入并获取自增主键失败", e);
        } finally {
            if (closeConn) {
                closeQuietly(conn);
            }
        }
    }

    private void setParameters(PreparedStatement pstmt, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }

    private <T> T createObjectFromResultSet(ResultSet rs, Class<T> clazz) throws SQLException {
        // 处理基本类型包装类
        if (clazz == Long.class || clazz == long.class) {
            return clazz.cast(rs.getLong(1));
        } else if (clazz == Integer.class || clazz == int.class) {
            return clazz.cast(rs.getInt(1));
        } else if (clazz == String.class) {
            return clazz.cast(rs.getString(1));
        } else if (clazz == Double.class || clazz == double.class) {
            return clazz.cast(rs.getDouble(1));
        } else if (clazz == Float.class || clazz == float.class) {
            return clazz.cast(rs.getFloat(1));
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return clazz.cast(rs.getBoolean(1));
        } else if (clazz == java.util.Date.class) {
            return clazz.cast(rs.getTimestamp(1));
        } else if (Number.class.isAssignableFrom(clazz)) {
            // 处理Number的子类（包括Number抽象类本身）
            return clazz.cast(rs.getLong(1));
        } else {
            // 对于复杂对象，使用反射将 ResultSet 列映射到 bean 字段
            try {
                T obj = clazz.getDeclaredConstructor().newInstance();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnLabel = metaData.getColumnLabel(i);
                    if (columnLabel == null || columnLabel.isEmpty()) {
                        columnLabel = metaData.getColumnName(i);
                    }
                    String fieldName = convertColumnToField(columnLabel);
                    Object value = rs.getObject(i);
                    setFieldValue(obj, fieldName, value);
                }
                return obj;
            } catch (InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException
                    | NoSuchMethodException e) {
                throw new RuntimeException("创建对象实例失败", e);
            }
        }
    }

    /**
     * 将数据库列名转换为 Java 字段名（下划线转驼峰）
     * 例如: CREATE_TIME -> createTime, ID -> id
     */
    private String convertColumnToField(String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            return columnName;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < columnName.length(); i++) {
            char c = columnName.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    /**
     * 通过反射设置对象字段值，支持类型转换
     */
    private void setFieldValue(Object obj, String fieldName, Object value) {
        if (value == null) {
            return;
        }
        Class<?> clazz = obj.getClass();
        try {
            java.lang.reflect.Field field = findField(clazz, fieldName);
            if (field == null) {
                return;
            }
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            Object convertedValue = convertValue(value, fieldType);
            if (convertedValue != null) {
                field.set(obj, convertedValue);
            }
        } catch (Exception e) {
            // 记录警告日志，不影响其他字段的设置
            if (logger.isWarnEnabled()) {
                logger.warn("设置字段 {} 的值失败: {}", fieldName, e.getMessage());
            }
        }
    }

    /**
     * 在类及其父类中查找字段
     */
    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        Class<?> searchType = clazz;
        while (searchType != null && searchType != Object.class) {
            try {
                java.lang.reflect.Field field = searchType.getDeclaredField(fieldName);
                return field;
            } catch (NoSuchFieldException e) {
                searchType = searchType.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 将值转换为目标类型
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        // 数值类型转换
        if (value instanceof Number) {
            Number num = (Number) value;
            if (targetType == Integer.class || targetType == int.class) {
                return num.intValue();
            } else if (targetType == Long.class || targetType == long.class) {
                return num.longValue();
            } else if (targetType == Double.class || targetType == double.class) {
                return num.doubleValue();
            } else if (targetType == Float.class || targetType == float.class) {
                return num.floatValue();
            } else if (targetType == Short.class || targetType == short.class) {
                return num.shortValue();
            } else if (targetType == Byte.class || targetType == byte.class) {
                return num.byteValue();
            }
        }
        // 字符串转换
        if (targetType == String.class) {
            return value.toString();
        }
        // java.util.Date 转换
        if (targetType == java.util.Date.class) {
            if (value instanceof java.sql.Timestamp) {
                return new java.util.Date(((java.sql.Timestamp) value).getTime());
            } else if (value instanceof java.sql.Date) {
                return new java.util.Date(((java.sql.Date) value).getTime());
            } else if (value instanceof java.sql.Time) {
                return new java.util.Date(((java.sql.Time) value).getTime());
            }
        }
        return value;
    }
}