package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import io.github.liuwww.db.condition.CompareOpe;
import io.github.liuwww.db.condition.Condition.ConditionRel;
import io.github.liuwww.db.condition.Conditions;
import io.github.liuwww.db.context.TableMetaData;

public class ConditionsTest {

    @Test
    public void testConditionsEq() {
        Conditions conditions = new Conditions();
        conditions.eq("name", "张三");
        conditions.eq("age", 25);

        assertEquals(2, conditions.size());
    }

    @Test
    public void testConditionsGt() {
        Conditions conditions = new Conditions();
        conditions.gt("age", 18);

        assertEquals(1, conditions.size());
    }

    @Test
    public void testConditionsLike() {
        Conditions conditions = new Conditions();
        conditions.like("name", "%张%");

        assertEquals(1, conditions.size());
    }

    @Test
    public void testConditionsLikeAuto() {
        Conditions conditions = new Conditions();
        conditions.likeAuto("name", "张");

        assertEquals(1, conditions.size());
    }

    @Test
    public void testConditionsIn() {
        Conditions conditions = new Conditions();
        conditions.in("id", Arrays.asList(1, 2, 3));

        assertEquals(1, conditions.size());
    }

    @Test
    public void testConditionsNotNull() {
        Conditions conditions = new Conditions();
        conditions.notNull("email");

        assertEquals(1, conditions.size());
    }

    @Test
    public void testConditionsIsNull() {
        Conditions conditions = new Conditions();
        conditions.isNull("deleted_at");

        assertEquals(1, conditions.size());
    }

    @Test
    public void testConditionsAddParamMap() {
        Conditions conditions = new Conditions();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", "张三");
        paramMap.put("age", 25);
        conditions.addParamMap(paramMap);

        assertEquals(2, conditions.size());
    }

    @Test
    public void testGetParamList() {
        Conditions conditions = new Conditions();
        conditions.eq("name", "张三");
        conditions.gt("age", 18);

        assertEquals(2, conditions.getParamList().size());
        assertTrue(conditions.getParamList().contains("张三"));
        assertTrue(conditions.getParamList().contains(18));
    }

    @Test
    public void testChainCalling() {
        Conditions conditions = new Conditions();
        conditions.eq("name", "张三")
                  .gt("age", 18)
                  .like("email", "%@example.com");

        assertEquals(3, conditions.size());
    }
}
