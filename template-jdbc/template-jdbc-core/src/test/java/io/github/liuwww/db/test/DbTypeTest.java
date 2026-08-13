package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import org.junit.Test;
import io.github.liuwww.db.sql.DbType;

public class DbTypeTest {

    @Test
    public void testGetByNameMySQL() {
        assertEquals(DbType.MYSQL, DbType.getByName("mysql"));
        assertEquals(DbType.MYSQL, DbType.getByName("MYSQL"));
        assertEquals(DbType.MYSQL, DbType.getByName("Mysql"));
    }

    @Test
    public void testGetByNameOracle() {
        assertEquals(DbType.ORACLE, DbType.getByName("oracle"));
        assertEquals(DbType.ORACLE, DbType.getByName("ORACLE"));
    }

    @Test
    public void testGetByNameH2() {
        assertEquals(DbType.H2, DbType.getByName("h2"));
        assertEquals(DbType.H2, DbType.getByName("H2"));
    }

    @Test
    public void testGetByNamePostgreSQL() {
        assertEquals(DbType.PostgreSQL, DbType.getByName("postgresql"));
        assertEquals(DbType.PostgreSQL, DbType.getByName("PostgreSQL"));
    }

    @Test
    public void testGetByNameOther() {
        assertEquals(DbType.OTHER, DbType.getByName("unknown"));
        assertEquals(DbType.OTHER, DbType.getByName(""));
        assertEquals(DbType.OTHER, DbType.getByName(null));
    }

    @Test
    public void testToString() {
        assertEquals("mysql", DbType.MYSQL.toString());
        assertEquals("oracle", DbType.ORACLE.toString());
        assertEquals("h2", DbType.H2.toString());
        assertEquals("postgresql", DbType.PostgreSQL.toString());
    }
}
