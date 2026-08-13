package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import io.github.liuwww.db.condition.CompareOpe;
import io.github.liuwww.db.condition.Condition;
import io.github.liuwww.db.condition.Condition.ConditionRel;
import io.github.liuwww.db.condition.Conditions;
import io.github.liuwww.db.condition.GroupCondition;
import io.github.liuwww.db.condition.OneCondition;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.query.DefaultQueryBean;
import io.github.liuwww.db.query.QueryBean;
import io.github.liuwww.db.query.QueryBeanFactory;
import io.github.liuwww.db.query.conditon.QueryBeanCompare;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.SqlBean;

/**
 * QueryBean查询构建器测试
 */
public class QueryBeanTest {

    @Test
    public void testDefaultQueryBeanConstructor() {
        // 测试构造器
        Object[] params = new Object[]{"张三", 25};
        DefaultQueryBean queryBean = new DefaultQueryBean(
            "SELECT * FROM test_user WHERE name = ? AND age = ?",
            params
        );

        assertNotNull(queryBean);
        assertNotNull(queryBean.getCompare());
    }

    @Test
    public void testQueryBeanGetCompare() {
        DefaultQueryBean queryBean = new DefaultQueryBean("SELECT * FROM test_user", new Object[]{});
        QueryBeanCompare compare = queryBean.getCompare();

        assertNotNull(compare);
        // 测试链式调用
        compare.eq("name", "张三").gt("age", 18);
    }

    @Test
    public void testQueryBeanSetOrderBy() {
        DefaultQueryBean queryBean = new DefaultQueryBean("SELECT * FROM test_user", new Object[]{});

        // 测试setTheOrderBy
        queryBean.setTheOrderBy("name DESC");

        // 测试addOrderBy
        queryBean.addOrderBy("age ASC");
    }

    @Test
    public void testQueryBeanGetSqlBean() {
        Object[] params = new Object[]{"1"};
        DefaultQueryBean queryBean = new DefaultQueryBean(
            "SELECT * FROM test_user WHERE id = ?",
            params
        );

        SqlBean sqlBean = queryBean.getSqlBean();

        assertNotNull(sqlBean);
        assertNotNull(sqlBean.getSql());
        assertTrue(sqlBean.getSql().contains("test_user"));
    }

    @Test
    public void testQueryBeanConditions() {
        DefaultQueryBean queryBean = new DefaultQueryBean("SELECT * FROM test_user", new Object[]{});
        QueryBeanCompare compare = queryBean.getCompare();

        // 添加多个条件
        compare.eq("name", "张三");
        compare.gt("age", 18);
        compare.lt("age", 60);

        // 验证条件已添加
        assertNotNull(compare);
    }

    @Test
    public void testQueryBeanWithGroupCondition() {
        DefaultQueryBean queryBean = new DefaultQueryBean("SELECT * FROM test_user", new Object[]{});
        QueryBeanCompare compare = queryBean.getCompare();

        // 创建分组条件
        GroupCondition group = new GroupCondition(ConditionRel.OR);
        group.addCondition(new OneCondition("name", CompareOpe.eq, "张三"));
        group.addCondition(new OneCondition("name", CompareOpe.eq, "李四"));

        // 添加分组条件
        compare.addCondition(group);
    }
}
