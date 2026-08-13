package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import org.junit.Test;
import io.github.liuwww.db.condition.Condition.ConditionRel;
import io.github.liuwww.db.condition.CompareOpe;
import io.github.liuwww.db.condition.GroupCondition;
import io.github.liuwww.db.condition.OneCondition;
import io.github.liuwww.db.sql.DbType;

public class GroupConditionTest {

    @Test
    public void testGroupConditionWithAnd() {
        GroupCondition group = new GroupCondition(ConditionRel.AND);
        group.addCondition(new OneCondition("name", CompareOpe.eq, "张三"));
        group.addCondition(new OneCondition("age", CompareOpe.gt, 18));

        assertTrue(group.isValid());
        assertEquals(2, group.size());

        String sql = group.getSqlFragment(DbType.MYSQL);
        assertTrue(sql.contains("name"));
        assertTrue(sql.contains("age"));
    }

    @Test
    public void testGroupConditionWithOr() {
        GroupCondition group = new GroupCondition(ConditionRel.OR);
        group.addCondition(new OneCondition("name", CompareOpe.eq, "张三"));
        group.addCondition(new OneCondition("name", CompareOpe.eq, "李四"));

        assertTrue(group.isValid());

        String sql = group.getSqlFragment(DbType.MYSQL);
        assertTrue(sql.contains(" or "));
    }

    @Test
    public void testGroupConditionWithInvalidCondition() {
        GroupCondition group = new GroupCondition();
        group.addCondition(new OneCondition("name", CompareOpe.eq, null));
        group.addCondition(new OneCondition("age", CompareOpe.gt, 18));

        assertTrue(group.isValid());
    }

    @Test
    public void testGroupConditionAllInvalid() {
        GroupCondition group = new GroupCondition();
        group.addCondition(new OneCondition("name", CompareOpe.eq, null));
        group.addCondition(new OneCondition("age", CompareOpe.eq, ""));

        assertFalse(group.isValid());
    }

    @Test
    public void testDefaultGroupCondition() {
        GroupCondition group = new GroupCondition();
        assertEquals(ConditionRel.AND, group.getConditionRel());
    }
}
