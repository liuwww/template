package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import io.github.liuwww.db.condition.CompareOpe;
import io.github.liuwww.db.condition.Condition.ConditionRel;
import io.github.liuwww.db.condition.Conditions;
import io.github.liuwww.db.condition.GroupCondition;
import io.github.liuwww.db.condition.OneCondition;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.sql.Column;
import io.github.liuwww.db.sql.DbType;

/**
 * Conditions 增强测试类
 * 测试条件集合类的各种功能，包括表元数据过滤
 */
public class ConditionsEnhancedTest {

    @Test
    public void testConditionsBasicUsage() {
        Conditions conditions = new Conditions();
        conditions.eq("name", "张三");
        conditions.gt("age", 18);

        assertEquals(2, conditions.size());
        assertTrue(conditions.isValid());
    }

    @Test
    public void testConditionsChainCalling() {
        Conditions conditions = new Conditions();
        Conditions result = conditions
                .eq("name", "张三")
                .gt("age", 18)
                .like("email", "%@example.com")
                .notNull("status");

        assertSame(conditions, result);
        assertEquals(4, conditions.size());
    }

    @Test
    public void testConditionsWithTableMetaData() {
        // 创建表元数据
        TableMetaData tmd = new TableMetaData();
        tmd.setTableName("test_user");
        tmd.setDbType(DbType.MYSQL);

        Column idColumn = new Column();
        idColumn.setColumnName("id");
        tmd.putColumnMap("id", idColumn);
        tmd.putNameMap("id", idColumn);

        Column nameColumn = new Column();
        nameColumn.setColumnName("name");
        tmd.putColumnMap("name", nameColumn);
        tmd.putNameMap("name", nameColumn);

        Column ageColumn = new Column();
        ageColumn.setColumnName("age");
        tmd.putColumnMap("age", ageColumn);
        tmd.putNameMap("age", ageColumn);

        // 创建带表元数据的条件
        Conditions conditions = new Conditions(tmd, true);
        conditions.eq("name", "张三");
        conditions.gt("age", 18);

        assertEquals(2, conditions.size());
    }

    @Test
    public void testConditionsWithInvalidFieldFiltered() {
        // 创建表元数据
        TableMetaData tmd = new TableMetaData();
        tmd.setTableName("test_user");
        tmd.setDbType(DbType.MYSQL);

        Column nameColumn = new Column();
        nameColumn.setColumnName("name");
        tmd.putColumnMap("name", nameColumn);
        tmd.putNameMap("name", nameColumn);

        // 创建带表元数据的条件
        Conditions conditions = new Conditions(tmd, true);
        conditions.eq("name", "张三");
        conditions.eq("invalid_field", "test");

        // invalid_field 不在表结构中，应该被过滤掉
        assertEquals(1, conditions.size());
    }

    @Test
    public void testConditionsWithoutTableMetaData() {
        // 不设置表元数据，所有字段都有效
        Conditions conditions = new Conditions();
        conditions.eq("name", "张三");
        conditions.eq("invalid_field", "test");

        assertEquals(2, conditions.size());
    }

    @Test
    public void testGetParamList() {
        Conditions conditions = new Conditions();
        conditions.eq("name", "张三");
        conditions.gt("age", 18);
        conditions.like("email", "%test%");

        List<Object> params = conditions.getParamList();
        assertEquals(3, params.size());
        assertTrue(params.contains("张三"));
        assertTrue(params.contains(18));
        assertTrue(params.contains("%test%"));
    }

    @Test
    public void testAddParamMap() {
        Conditions conditions = new Conditions();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", "张三");
        paramMap.put("age", 25);
        paramMap.put("status", 1);
        conditions.addParamMap(paramMap);

        assertEquals(3, conditions.size());
    }

    @Test
    public void testAddConditionWithGroup() {
        Conditions conditions = new Conditions();
        conditions.eq("status", 1);

        GroupCondition group = new GroupCondition(ConditionRel.OR);
        group.addCondition(new OneCondition("name", CompareOpe.like, "%张%"));
        group.addCondition(new OneCondition("name", CompareOpe.like, "%李%"));

        conditions.addCondition(group, ConditionRel.AND);

        assertEquals(2, conditions.size());
        assertTrue(conditions.isValid());
    }

    @Test
    public void testInConditionWithList() {
        Conditions conditions = new Conditions();
        conditions.in("id", Arrays.asList(1, 2, 3));

        assertEquals(1, conditions.size());
        assertTrue(conditions.isValid());
    }

    @Test
    public void testInConditionWithArray() {
        Conditions conditions = new Conditions();
        conditions.in("id", java.util.Arrays.asList(1, 2, 3));

        assertEquals(1, conditions.size());
        assertTrue(conditions.isValid());
    }

    @Test
    public void testLikeAuto() {
        Conditions conditions = new Conditions();
        conditions.likeAuto("name", "张");

        assertEquals(1, conditions.size());
        assertTrue(conditions.isValid());
    }

    @Test
    public void testLikeAutoWithNullValue() {
        Conditions conditions = new Conditions();
        conditions.likeAuto("name", null);

        assertEquals(0, conditions.size());
    }

    @Test
    public void testLikeAutoWithEmptyValue() {
        Conditions conditions = new Conditions();
        conditions.likeAuto("name", "");

        assertEquals(0, conditions.size());
    }

    @Test
    public void testLikeR() {
        Conditions conditions = new Conditions();
        conditions.likeR("name", "张");

        assertEquals(1, conditions.size());
    }

    @Test
    public void testLikeL() {
        Conditions conditions = new Conditions();
        conditions.likeL("name", "三");

        assertEquals(1, conditions.size());
    }

    @Test
    public void testEmptyStr() {
        Conditions conditions = new Conditions();
        conditions.emptyStr("name");

        assertEquals(1, conditions.size());
        assertTrue(conditions.isValid());
    }

    @Test
    public void testIterator() {
        Conditions conditions = new Conditions();
        conditions.eq("name", "张三");
        conditions.gt("age", 18);

        int count = 0;
        for (io.github.liuwww.db.condition.Condition c : conditions) {
            count++;
            assertNotNull(c);
        }
        assertEquals(2, count);
    }

    @Test
    public void testGetTableMetaData() {
        TableMetaData tmd = new TableMetaData();
        tmd.setTableName("test_user");

        Conditions conditions = new Conditions(tmd, false);
        assertSame(tmd, conditions.getTableMetaData());
    }

    @Test
    public void testSetTmd() {
        Conditions conditions = new Conditions();
        TableMetaData tmd = new TableMetaData();
        tmd.setTableName("test_user");

        conditions.setTmd(tmd);
        assertSame(tmd, conditions.getTableMetaData());
    }

    @Test
    public void testIsOneAndSetOne() {
        Conditions conditions = new Conditions();
        assertFalse(conditions.isOne());

        conditions.setOne(true);
        assertTrue(conditions.isOne());
    }

    @Test
    public void testMultipleConditionTypes() {
        Conditions conditions = new Conditions();
        conditions.eq("name", "张三")
                .ne("status", 0)
                .gt("age", 18)
                .ge("score", 60)
                .lt("age", 60)
                .le("score", 100)
                .like("email", "%@example.com")
                .notNull("phone")
                .isNull("deleted_at")
                .emptyStr("nickname");

        assertEquals(10, conditions.size());
    }

    @Test
    public void testConditionWithOrRelation() {
        Conditions conditions = new Conditions();
        conditions.eq("name", "张三", ConditionRel.OR);
        conditions.gt("age", 18, ConditionRel.OR);

        assertEquals(2, conditions.size());
    }

    @Test
    public void testNullAndEmptyValues() {
        Conditions conditions = new Conditions();

        // null 值的 eq 条件应该无效
        conditions.eq("name", null);
        assertEquals(0, conditions.size());

        // 空字符串的 eq 条件应该无效
        conditions.eq("name", "");
        assertEquals(0, conditions.size());
    }

    @Test
    public void testSqlFragmentGeneration() {
        Conditions conditions = new Conditions();
        conditions.eq("name", "张三");
        conditions.gt("age", 18);

        StringBuilder sql = new StringBuilder("SELECT * FROM test_user");
        boolean first = true;
        for (io.github.liuwww.db.condition.Condition c : conditions) {
            if (c.isValid()) {
                if (first) {
                    sql.append(" WHERE");
                    first = false;
                } else {
                    sql.append(" AND");
                }
                sql.append(" ").append(c.getSqlFragment(DbType.MYSQL));
            }
        }

        String sqlStr = sql.toString();
        assertTrue(sqlStr.contains("WHERE"));
        assertTrue(sqlStr.contains("name"));
        assertTrue(sqlStr.contains("age"));
    }
}
