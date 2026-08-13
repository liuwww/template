package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import org.junit.Test;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.DefaultSqlBean;

public class SqlBeanTest {

    @Test
    public void testSqlBeanSettersAndGetters() {
        DefaultSqlBean bean = new DefaultSqlBean();
        bean.setSql("SELECT * FROM user WHERE id = ?");
        bean.setParams(new Object[]{"123"});
        bean.setDbType(DbType.MYSQL);
        bean.setOrderBy("create_time DESC");
        bean.setGroupBy("status");

        assertEquals("SELECT * FROM user WHERE id = ?", bean.getSql());
        assertArrayEquals(new Object[]{"123"}, bean.getParams());
        assertEquals(DbType.MYSQL, bean.getDbType());
        assertEquals("create_time DESC", bean.getOrderBy());
        assertEquals("status", bean.getGroupBy());
    }

    @Test
    public void testSqlBeanTables() {
        DefaultSqlBean bean = new DefaultSqlBean();
        bean.setTables(new String[]{"user", "order"});

        assertArrayEquals(new String[]{"user", "order"}, bean.getTables());
    }

    @Test
    public void testSqlBeanConstructor() {
        DefaultSqlBean bean = new DefaultSqlBean();
        bean.setSql("SELECT * FROM user");
        bean.setParams(new Object[]{});
        bean.setOrderBy("ORDER BY id");
        bean.setDbType(DbType.MYSQL);

        assertEquals("SELECT * FROM user", bean.getSql());
        assertEquals(DbType.MYSQL, bean.getDbType());
        assertEquals("ORDER BY id", bean.getOrderBy());
    }

    @Test
    public void testSqlBeanDatabaseExecutor() {
        DefaultSqlBean bean = new DefaultSqlBean();
        assertNull(bean.getDatabaseExecutor());
    }
}
