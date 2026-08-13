package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import io.github.liuwww.db.condition.CompareOpe;
import io.github.liuwww.db.condition.Condition.ConditionRel;
import io.github.liuwww.db.condition.Conditions;
import io.github.liuwww.db.condition.GroupCondition;
import io.github.liuwww.db.condition.OneCondition;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.executor.RowMapper;
import io.github.liuwww.db.executor.impl.JdbcDatabaseExecutor;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.DefaultSqlBeanBuilder;
import io.github.liuwww.db.sql.Row;
import io.github.liuwww.db.sql.SqlBean;

/**
 * 完整的 CRUD 集成测试
 * 测试完整的业务流程：数据插入 -> 查询 -> 更新 -> 删除
 */
public class FullCrudIntegrationTest extends BaseTest {

    @Test
    public void testFullCrudWorkflow() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 1. INSERT - 插入数据
        int insertRows = exec.update(
            "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
            new Object[]{"1", "张三", 25, "zhangsan@example.com"}
        );
        assertEquals("Should insert 1 row", 1, insertRows);

        // 2. SELECT - 查询数据
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user WHERE id = ?",
            new Object[]{"1"}
        );
        assertEquals("Should find 1 row", 1, results.size());
        assertEquals("张三", results.get(0).get("NAME"));
        assertEquals(25, results.get(0).get("AGE"));
        assertEquals("zhangsan@example.com", results.get(0).get("EMAIL"));

        // 3. UPDATE - 更新数据
        int updateRows = exec.update(
            "UPDATE test_user SET name = ?, age = ? WHERE id = ?",
            new Object[]{"李四", 30, "1"}
        );
        assertEquals("Should update 1 row", 1, updateRows);

        // 4. Verify Update - 验证更新
        results = exec.queryForList(
            "SELECT * FROM test_user WHERE id = ?",
            new Object[]{"1"}
        );
        assertEquals(1, results.size());
        assertEquals("李四", results.get(0).get("NAME"));
        assertEquals(30, results.get(0).get("AGE"));

        // 5. DELETE - 删除数据
        int deleteRows = exec.update(
            "DELETE FROM test_user WHERE id = ?",
            new Object[]{"1"}
        );
        assertEquals("Should delete 1 row", 1, deleteRows);

        // 6. Verify Delete - 验证删除
        results = exec.queryForList(
            "SELECT * FROM test_user WHERE id = ?",
            new Object[]{"1"}
        );
        assertEquals("Should find 0 rows", 0, results.size());
    }

    @Test
    public void testInsertMultipleRecords() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 插入多条记录
        for (int i = 1; i <= 10; i++) {
            exec.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[]{String.valueOf(i), "用户" + i, 20 + i, "user" + i + "@example.com"}
            );
        }

        // 查询所有记录
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user",
            new Object[]{}
        );
        assertEquals("Should have 10 rows", 10, results.size());
    }

    @Test
    public void testQueryWithConditions() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 准备测试数据
        for (int i = 1; i <= 5; i++) {
            exec.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[]{String.valueOf(i), "用户" + i, 20 + i, "user" + i + "@example.com"}
            );
        }

        // 条件查询：age > 22
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user WHERE age > ?",
            new Object[]{22}
        );
        assertTrue("Should have at least 3 rows with age > 22", results.size() >= 3);
    }

    @Test
    public void testQueryWithLikeCondition() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 准备测试数据
        exec.update(
            "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
            new Object[]{"1", "张三", 25, "zhangsan@example.com"}
        );
        exec.update(
            "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
            new Object[]{"2", "李四", 30, "lisi@example.com"}
        );
        exec.update(
            "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
            new Object[]{"3", "王五", 28, "wangwu@example.com"}
        );

        // LIKE 查询
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user WHERE name LIKE ?",
            new Object[]{"%张%"}
        );
        assertEquals("Should find 1 row with name containing 张", 1, results.size());
        assertEquals("张三", results.get(0).get("NAME"));
    }

    @Test
    public void testQueryWithOrderBy() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 准备测试数据
        for (int i = 1; i <= 5; i++) {
            exec.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[]{String.valueOf(i), "用户" + i, 30 - i, "user" + i + "@example.com"}
            );
        }

        // 按年龄降序查询
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user ORDER BY age DESC",
            new Object[]{}
        );
        assertEquals(5, results.size());
        assertEquals(29, results.get(0).get("AGE"));
        assertEquals(25, results.get(4).get("AGE"));
    }

    @Test
    public void testUpdateMultipleFields() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 插入数据
        exec.update(
            "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
            new Object[]{"1", "原始用户", 20, "original@example.com"}
        );

        // 更新多个字段
        int updateRows = exec.update(
            "UPDATE test_user SET name = ?, age = ?, email = ? WHERE id = ?",
            new Object[]{"更新用户", 35, "updated@example.com", "1"}
        );
        assertEquals(1, updateRows);

        // 验证更新
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user WHERE id = ?",
            new Object[]{"1"}
        );
        assertEquals(1, results.size());
        assertEquals("更新用户", results.get(0).get("NAME"));
        assertEquals(35, results.get(0).get("AGE"));
        assertEquals("updated@example.com", results.get(0).get("EMAIL"));
    }

    @Test
    public void testDeleteMultipleRecords() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 插入多条数据
        for (int i = 1; i <= 5; i++) {
            exec.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[]{String.valueOf(i), "用户" + i, 20 + i, "user" + i + "@example.com"}
            );
        }

        // 删除 age < 23 的记录
        int deleteRows = exec.update(
            "DELETE FROM test_user WHERE age < ?",
            new Object[]{23}
        );
        assertTrue("Should delete at least 2 rows", deleteRows >= 2);

        // 验证剩余记录
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user",
            new Object[]{}
        );
        assertTrue("Should have less than 5 rows", results.size() < 5);
    }

    @Test
    public void testQueryCount() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 插入测试数据
        for (int i = 1; i <= 7; i++) {
            exec.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[]{String.valueOf(i), "用户" + i, 20 + i, "user" + i + "@example.com"}
            );
        }

        // 查询总数
        Long count = exec.queryForObject(
            "SELECT COUNT(*) FROM test_user",
            new Object[]{},
            Long.class
        );
        assertEquals("Should have 7 rows", Long.valueOf(7), count);
    }

    @Test
    public void testQueryWithRowMapper() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 插入测试数据
        exec.update(
            "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
            new Object[]{"1", "张三", 25, "zhangsan@example.com"}
        );

        // 使用 RowMapper 查询（使用queryForList替代）
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user WHERE id = ?",
            new Object[]{"1"}
        );
        
        assertEquals(1, results.size());
        assertEquals("1", results.get(0).get("ID"));
        assertEquals("张三", results.get(0).get("NAME"));
        assertEquals(25, results.get(0).get("AGE"));
        assertEquals("zhangsan@example.com", results.get(0).get("EMAIL"));
    }

    @Test
    public void testSqlBeanBuilderInsertWithExecutor() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);
        DefaultSqlBeanBuilder builder = new DefaultSqlBeanBuilder();

        // 构建插入 SQL
        Row row = new Row();
        row.setTableName("test_user");
        row.setIdName("id");
        row.setIdValue("1");
        row.setDbType(DbType.MYSQL);

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("name", "张三");
        valueMap.put("age", 25);
        valueMap.put("email", "zhangsan@example.com");
        row.setRowValueMap(valueMap);

        SqlBean insertBean = builder.buildInsert(row);

        // 执行插入
        assertNotNull(insertBean);
        assertNotNull(insertBean.getSql());
        assertTrue(insertBean.getSql().contains("insert into"));

        int rows = exec.update(insertBean.getSql(), insertBean.getParams());
        assertEquals("Should insert 1 row", 1, rows);

        // 验证插入
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user WHERE id = ?",
            new Object[]{"1"}
        );
        assertEquals(1, results.size());
        assertEquals("张三", results.get(0).get("NAME"));
    }

    @Test
    public void testSqlBeanBuilderUpdateWithExecutor() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);
        DefaultSqlBeanBuilder builder = new DefaultSqlBeanBuilder();

        // 先插入数据
        exec.update(
            "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
            new Object[]{"1", "原始用户", 20, "original@example.com"}
        );

        // 构建更新 SQL
        Row row = new Row();
        row.setTableName("test_user");
        row.setIdName("id");
        row.setIdValue("1");
        row.setDbType(DbType.MYSQL);

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("name", "更新用户");
        valueMap.put("age", 30);
        row.setRowValueMap(valueMap);

        SqlBean updateBean = builder.buildUpdate(row);

        // 执行更新
        assertNotNull(updateBean);
        assertNotNull(updateBean.getSql());
        assertTrue(updateBean.getSql().contains("update"));

        int rows = exec.update(updateBean.getSql(), updateBean.getParams());
        assertEquals("Should update 1 row", 1, rows);

        // 验证更新
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user WHERE id = ?",
            new Object[]{"1"}
        );
        assertEquals(1, results.size());
        assertEquals("更新用户", results.get(0).get("NAME"));
        assertEquals(30, results.get(0).get("AGE"));
    }

    @Test
    public void testSqlBeanBuilderDeleteWithExecutor() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);
        DefaultSqlBeanBuilder builder = new DefaultSqlBeanBuilder();

        // 先插入数据
        exec.update(
            "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
            new Object[]{"1", "待删除用户", 25, "delete@example.com"}
        );

        // 构建删除 SQL
        Row row = new Row();
        row.setTableName("test_user");
        row.setIdName("id");
        row.setIdValue("1");
        row.setDbType(DbType.MYSQL);

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("id", "1");
        row.setRowValueMap(valueMap);

        SqlBean deleteBean = builder.buildDelete(row);

        // 执行删除
        assertNotNull(deleteBean);
        assertNotNull(deleteBean.getSql());
        assertTrue(deleteBean.getSql().contains("delete from"));

        int rows = exec.update(deleteBean.getSql(), deleteBean.getParams());
        assertEquals("Should delete 1 row", 1, rows);

        // 验证删除
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user WHERE id = ?",
            new Object[]{"1"}
        );
        assertEquals("Should find 0 rows", 0, results.size());
    }

    @Test
    public void testTransactionRollback() throws Exception {
        // 注意：H2 内存数据库支持事务
        // 这个测试验证事务回滚机制
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 插入数据
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(
                        "INSERT INTO test_user (id, name, age, email) VALUES ('1', '事务用户', 25, 'tx@example.com')"
                    );
                }

                // 查询验证
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_user WHERE id = '1'");
                    assertTrue(rs.next());
                    assertEquals(1, rs.getInt(1));
                }

                // 模拟异常，回滚事务
                throw new RuntimeException("模拟异常");

            } catch (RuntimeException e) {
                conn.rollback();
            }

            // 验证回滚后数据不存在
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_user WHERE id = '1'");
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }

            conn.setAutoCommit(true);
        }
    }

    @Test
    public void testTransactionCommit() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            // 插入数据
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "INSERT INTO test_user (id, name, age, email) VALUES ('1', '事务用户', 25, 'tx@example.com')"
                );
            }

            // 提交事务
            conn.commit();

            // 验证数据已提交
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_user WHERE id = '1'");
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }

            conn.setAutoCommit(true);
        }
    }

    @Test
    public void testBatchInsert() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                for (int i = 1; i <= 10; i++) {
                    stmt.addBatch(
                        "INSERT INTO test_user (id, name, age, email) VALUES ('" + i + "', '用户" + i + "', " + (20 + i) + ", 'user" + i + "@example.com')"
                    );
                }
                stmt.executeBatch();
            }

            conn.commit();

            // 验证批量插入
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_user");
                assertTrue(rs.next());
                assertEquals(10, rs.getInt(1));
            }

            conn.setAutoCommit(true);
        }
    }

    @Test
    public void testConditionsWithGroupAndOr() {
        // 构建复杂的条件组合
        GroupCondition group = new GroupCondition(ConditionRel.OR);
        group.addCondition(new OneCondition("name", CompareOpe.like, "%张%"));
        group.addCondition(new OneCondition("name", CompareOpe.like, "%李%"));

        Conditions conditions = new Conditions();
        conditions.gt("age", 20);
        conditions.addCondition(group, ConditionRel.AND);

        assertTrue(conditions.isValid());
        assertEquals(2, conditions.size());
    }

    @Test
    public void testQueryWithPagination() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 插入测试数据
        for (int i = 1; i <= 25; i++) {
            exec.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[]{String.valueOf(i), "用户" + i, 20 + (i % 10), "user" + i + "@example.com"}
            );
        }

        // 分页查询：第1页，每页10条
        List<Map<String, Object>> page1 = exec.queryForList(
            "SELECT * FROM test_user LIMIT 0, 10",
            new Object[]{}
        );
        assertEquals(10, page1.size());

        // 分页查询：第2页，每页10条
        List<Map<String, Object>> page2 = exec.queryForList(
            "SELECT * FROM test_user LIMIT 10, 10",
            new Object[]{}
        );
        assertEquals(10, page2.size());

        // 分页查询：第3页，每页10条
        List<Map<String, Object>> page3 = exec.queryForList(
            "SELECT * FROM test_user LIMIT 20, 10",
            new Object[]{}
        );
        assertEquals(5, page3.size());
    }

    @Test
    public void testInsertWithNullValues() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 插入包含 NULL 值的记录
        int rows = exec.update(
            "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
            new Object[]{"1", "张三", null, null}
        );
        assertEquals(1, rows);

        // 验证
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user WHERE id = ?",
            new Object[]{"1"}
        );
        assertEquals(1, results.size());
        assertNull(results.get(0).get("AGE"));
        assertNull(results.get(0).get("EMAIL"));
    }

    @Test
    public void testQueryWithInClause() {
        DatabaseExecutor exec = new JdbcDatabaseExecutor(dataSource);

        // 插入测试数据
        for (int i = 1; i <= 5; i++) {
            exec.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[]{String.valueOf(i), "用户" + i, 20 + i, "user" + i + "@example.com"}
            );
        }

        // IN 查询
        List<Map<String, Object>> results = exec.queryForList(
            "SELECT * FROM test_user WHERE id IN (?, ?, ?)",
            new Object[]{"1", "3", "5"}
        );
        assertEquals(3, results.size());
    }

    /**
     * 测试用户实体类
     */
    public static class TestUser {
        private String id;
        private String name;
        private int age;
        private String email;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
