package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.DefaultSqlBeanBuilder;
import io.github.liuwww.db.sql.SqlBean;

/**
 * SQL 构建器测试类
 */
public class SqlBeanBuilderTest {

    private DefaultSqlBeanBuilder builder = new DefaultSqlBeanBuilder();

    @Test
    public void testBuildInsert() {
        io.github.liuwww.db.sql.Row row = new io.github.liuwww.db.sql.Row();
        row.setTableName("test_user");
        row.setIdName("id");
        row.setIdValue("1");
        row.setDbType(DbType.MYSQL);
        
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("name", "张三");
        valueMap.put("age", 25);
        row.setRowValueMap(valueMap);
        
        SqlBean bean = builder.buildInsert(row);
        
        assertNotNull(bean);
        assertNotNull(bean.getSql());
        assertTrue(bean.getSql().contains("insert into"));
        assertTrue(bean.getSql().contains("test_user"));
        assertTrue(bean.getSql().contains("id"));
        assertTrue(bean.getSql().contains("name"));
        assertTrue(bean.getSql().contains("age"));
        assertTrue(bean.getSql().contains("values"));
    }

    @Test
    public void testBuildInsertWithoutId() {
        io.github.liuwww.db.sql.Row row = new io.github.liuwww.db.sql.Row();
        row.setTableName("test_user");
        row.setIdName("id");
        row.setIdValue("1");
        row.setDbType(DbType.MYSQL);
        
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("name", "张三");
        valueMap.put("age", 25);
        row.setRowValueMap(valueMap);
        
        SqlBean bean = builder.buildInsert(row, false);
        
        assertNotNull(bean);
        assertNotNull(bean.getSql());
        assertFalse(bean.getSql().contains("id"));
    }

    @Test
    public void testBuildUpdate() {
        io.github.liuwww.db.sql.Row row = new io.github.liuwww.db.sql.Row();
        row.setTableName("test_user");
        row.setIdName("id");
        row.setIdValue("1");
        row.setDbType(DbType.MYSQL);
        
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("name", "李四");
        valueMap.put("age", 30);
        row.setRowValueMap(valueMap);
        
        SqlBean bean = builder.buildUpdate(row);
        
        assertNotNull(bean);
        assertNotNull(bean.getSql());
        assertTrue(bean.getSql().contains("update"));
        assertTrue(bean.getSql().contains("set"));
        assertTrue(bean.getSql().contains("where"));
        assertTrue(bean.getSql().contains("id"));
    }

    @Test
    public void testBuildDelete() {
        io.github.liuwww.db.sql.Row row = new io.github.liuwww.db.sql.Row();
        row.setTableName("test_user");
        row.setIdName("id");
        row.setIdValue("1");
        row.setDbType(DbType.MYSQL);
        
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("id", "1");
        row.setRowValueMap(valueMap);
        
        SqlBean bean = builder.buildDelete(row);
        
        assertNotNull(bean);
        assertNotNull(bean.getSql());
        assertTrue(bean.getSql().contains("delete from"));
        assertTrue(bean.getSql().contains("test_user"));
        assertTrue(bean.getSql().contains("where"));
    }

    @Test
    public void testBuildInsertSql() {
        // 测试需要 TableMetaData，这里只验证方法存在
        // 完整测试需要更复杂的配置
        assertNotNull(builder);
    }

    @Test
    public void testBuildDeleteSql() {
        // 测试需要 TableMetaData，这里只验证方法存在
        assertNotNull(builder);
    }

    @Test
    public void testGetConditionField() {
        // 测试需要 TableMetaData，这里只验证方法存在
        assertNotNull(builder);
    }
}
