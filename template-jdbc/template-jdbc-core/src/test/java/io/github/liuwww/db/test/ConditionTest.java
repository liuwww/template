package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import io.github.liuwww.db.condition.CompareOpe;
import io.github.liuwww.db.condition.Condition;
import io.github.liuwww.db.condition.Condition.ConditionRel;
import io.github.liuwww.db.condition.OneCondition;
import io.github.liuwww.db.sql.DbType;

public class ConditionTest {

    @Test
    public void testOneConditionEqual() {
        OneCondition cond = new OneCondition("name", CompareOpe.eq, "张三");
        
        assertTrue(cond.isValid());
        assertEquals("name", cond.getField());
        assertEquals(CompareOpe.eq, cond.getOpe());
        assertEquals("张三", cond.getVal());
        assertEquals(ConditionRel.AND, cond.getConditionRel());
        
        String sql = cond.getSqlFragment(DbType.MYSQL);
        assertTrue(sql.contains("name"));
        assertTrue(sql.contains("="));
    }

    @Test
    public void testOneConditionGreaterThan() {
        OneCondition cond = new OneCondition("age", CompareOpe.gt, 18);
        
        assertTrue(cond.isValid());
        String sql = cond.getSqlFragment(DbType.MYSQL);
        assertTrue(sql.contains("age"));
        assertTrue(sql.contains(">"));
    }

    @Test
    public void testOneConditionLike() {
        OneCondition cond = new OneCondition("name", CompareOpe.like, "%张%");
        
        assertTrue(cond.isValid());
        String sql = cond.getSqlFragment(DbType.MYSQL);
        assertTrue(sql.contains("name"));
        assertTrue(sql.contains("like"));
    }

    @Test
    public void testOneConditionIn() {
        List<Object> values = new ArrayList<>();
        values.add(1);
        values.add(2);
        values.add(3);
        
        OneCondition cond = new OneCondition("id", CompareOpe.in, values);
        
        assertTrue(cond.isValid());
        String sql = cond.getSqlFragment(DbType.MYSQL);
        assertTrue(sql.contains("id"));
        assertTrue(sql.contains("in"));
    }

    @Test
    public void testOneConditionNotNull() {
        OneCondition cond = new OneCondition("email", CompareOpe.notNull, null);
        
        assertTrue(cond.isValid());
        String sql = cond.getSqlFragment(DbType.MYSQL);
        assertTrue(sql.contains("email"));
        assertTrue(sql.contains("is not null"));
    }

    @Test
    public void testOneConditionIsNull() {
        OneCondition cond = new OneCondition("email", CompareOpe.isNull, null);
        
        assertTrue(cond.isValid());
        String sql = cond.getSqlFragment(DbType.MYSQL);
        assertTrue(sql.contains("email"));
        assertTrue(sql.contains("is null"));
    }

    @Test
    public void testOneConditionWithOrRel() {
        OneCondition cond = new OneCondition("name", CompareOpe.eq, "张三", ConditionRel.OR);
        
        assertEquals(ConditionRel.OR, cond.getConditionRel());
    }

    @Test
    public void testInvalidConditionWithNullValue() {
        OneCondition cond = new OneCondition("name", CompareOpe.eq, null);
        
        assertFalse(cond.isValid());
    }

    @Test
    public void testInvalidConditionWithEmptyString() {
        OneCondition cond = new OneCondition("name", CompareOpe.eq, "");
        
        assertFalse(cond.isValid());
    }

    @Test
    public void testConditionParamToString() {
        Condition.Param param = new Condition.Param("test");
        assertEquals("[test]", param.toString());
    }
}
