package io.github.liuwww.db.sql.util;

import static org.junit.Assert.*;

import org.junit.Test;
import io.github.liuwww.db.sql.DbType;

public class PaginationFactoryTest {

    @Test
    public void testMySqlPagination() {
        String sql = "SELECT * FROM users WHERE status = 1";
        String paginated = PaginationFactory.limit(sql, DbType.MYSQL, 10, 20);

        assertTrue(paginated.contains("LIMIT"));
        assertTrue(paginated.contains("10, 20") || paginated.contains("10,20"));
    }

    @Test
    public void testMySqlCount() {
        String sql = "SELECT * FROM users WHERE status = 1";
        String countSql = PaginationFactory.count(sql, DbType.MYSQL);

        assertTrue(countSql.contains("SELECT COUNT(*)"));
        assertTrue(countSql.contains(sql) || countSql.contains("users"));
    }

    @Test
    public void testOraclePagination() {
        String sql = "SELECT * FROM users";
        String paginated = PaginationFactory.limit(sql, DbType.ORACLE, 10, 20);

        assertTrue(paginated.contains("ROWNUM"));
        assertTrue(paginated.contains("ROW_ID"));
    }

    @Test
    public void testOracleCount() {
        String sql = "SELECT * FROM users";
        String countSql = PaginationFactory.count(sql, DbType.ORACLE);

        assertTrue(countSql.contains("SELECT COUNT(*)"));
    }

    @Test
    public void testH2Pagination() {
        String sql = "SELECT * FROM users";
        String paginated = PaginationFactory.limit(sql, DbType.H2, 0, 10);

        assertTrue(paginated.contains("LIMIT"));
    }

    @Test
    public void testPostgreSqlPagination() {
        String sql = "SELECT * FROM users";
        String paginated = PaginationFactory.limit(sql, DbType.PostgreSQL, 10, 20);

        assertTrue(paginated.contains("LIMIT 20 OFFSET 10") || paginated.contains("LIMIT 20"));
    }

    @Test
    public void testDefaultPagination() {
        String sql = "SELECT * FROM users";
        String paginated = PaginationFactory.limit(sql, DbType.OTHER, 0, 10);

        assertTrue(paginated.contains("LIMIT"));
    }

    @Test
    public void testMySqlOffsetZero() {
        String sql = "SELECT * FROM users";
        String paginated = PaginationFactory.limit(sql, DbType.MYSQL, 0, 10);

        assertTrue(paginated.contains("LIMIT 10") || paginated.contains("LIMIT 0, 10"));
    }
}
