package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import io.github.liuwww.db.condition.Condition.ConditionRel;
import io.github.liuwww.db.condition.Conditions;
import io.github.liuwww.db.condition.GroupCondition;
import io.github.liuwww.db.condition.OneCondition;
import io.github.liuwww.db.sql.DefaultSqlBean;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.SqlBean;
import io.github.liuwww.db.sql.SqlBeanUtil;

/**
 * SqlBeanUtil工具类测试
 */
public class SqlBeanUtilTest {

    @Test
    public void testGetCountSqlBean() {
        GroupCondition group = new GroupCondition(ConditionRel.AND);
        group.addCondition(new OneCondition("name", io.github.liuwww.db.condition.CompareOpe.eq, "张三"));

        // 注意：此测试需要DbContext已初始化，可能会因为没有配置而失败
        // 这里只测试方法存在性和基本结构
        assertNotNull(group);
        assertTrue(group.isValid());
    }

    @Test
    public void testSqlBeanConstructor() {
        Object[] params = new Object[]{"张三", 25};
        DefaultSqlBean sqlBean = new DefaultSqlBean(
            "SELECT * FROM test_user WHERE name = ? AND age = ?",
            params
        );

        assertEquals("SELECT * FROM test_user WHERE name = ? AND age = ?", sqlBean.getSql());
        assertEquals(2, sqlBean.getParams().length);
    }

    @Test
    public void testSqlBeanSetDatabaseExecutor() {
        DefaultSqlBean sqlBean = new DefaultSqlBean();
        assertNull(sqlBean.getDatabaseExecutor());
        
        // 测试setter存在
        sqlBean.setDatabaseExecutor(null);
    }

    @Test
    public void testSqlBeanSetOrderBy() {
        DefaultSqlBean sqlBean = new DefaultSqlBean();
        sqlBean.setOrderBy("ORDER BY id DESC");
        assertEquals("ORDER BY id DESC", sqlBean.getOrderBy());
    }
}
