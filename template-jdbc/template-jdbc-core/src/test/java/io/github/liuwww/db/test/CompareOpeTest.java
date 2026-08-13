package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import org.junit.Test;
import io.github.liuwww.db.condition.CompareOpe;

public class CompareOpeTest {

    @Test
    public void testCompareOpeValues() {
        assertEquals(11, CompareOpe.values().length);
    }

    @Test
    public void testLt() {
        assertEquals("<", CompareOpe.lt.getVal());
        assertFalse(CompareOpe.lt.defaultEffective());
    }

    @Test
    public void testGt() {
        assertEquals(">", CompareOpe.gt.getVal());
        assertFalse(CompareOpe.gt.defaultEffective());
    }

    @Test
    public void testNe() {
        assertEquals("!=", CompareOpe.ne.getVal());
        assertFalse(CompareOpe.ne.defaultEffective());
    }

    @Test
    public void testEq() {
        assertEquals("=", CompareOpe.eq.getVal());
        assertFalse(CompareOpe.eq.defaultEffective());
    }

    @Test
    public void testLe() {
        assertEquals("<=", CompareOpe.le.getVal());
        assertFalse(CompareOpe.le.defaultEffective());
    }

    @Test
    public void testGe() {
        assertEquals(">=", CompareOpe.ge.getVal());
        assertFalse(CompareOpe.ge.defaultEffective());
    }

    @Test
    public void testLike() {
        assertEquals("like", CompareOpe.like.getVal());
        assertFalse(CompareOpe.like.defaultEffective());
    }

    @Test
    public void testNotNull() {
        assertEquals("not null", CompareOpe.notNull.getVal());
        assertTrue(CompareOpe.notNull.defaultEffective());
    }

    @Test
    public void testIsNull() {
        assertEquals("is null", CompareOpe.isNull.getVal());
        assertTrue(CompareOpe.isNull.defaultEffective());
    }

    @Test
    public void testEmptyStr() {
        assertEquals("=''", CompareOpe.emptyStr.getVal());
        assertTrue(CompareOpe.emptyStr.defaultEffective());
    }

    @Test
    public void testIn() {
        assertEquals("in", CompareOpe.in.getVal());
        assertFalse(CompareOpe.in.defaultEffective());
    }

    @Test
    public void testValueOf() {
        assertEquals(CompareOpe.eq, CompareOpe.valueOf("eq"));
        assertEquals(CompareOpe.lt, CompareOpe.valueOf("lt"));
        assertEquals(CompareOpe.gt, CompareOpe.valueOf("gt"));
        assertEquals(CompareOpe.like, CompareOpe.valueOf("like"));
        assertEquals(CompareOpe.in, CompareOpe.valueOf("in"));
    }
}
