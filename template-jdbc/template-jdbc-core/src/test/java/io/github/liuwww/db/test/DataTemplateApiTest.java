package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.github.liuwww.db.context.CoreDbContext;
import io.github.liuwww.db.context.DbContext;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.executor.impl.JdbcDatabaseExecutor;
import io.github.liuwww.db.service.IDataTemplate;
import io.github.liuwww.db.service.IQueryTemplate;

public class DataTemplateApiTest {

    private DataSource dataSource;
    private DatabaseExecutor executor;
    private CoreDbContext dbContext;
    private IDataTemplate dataTemplate;
    private IQueryTemplate queryTemplate;

    @Before
    public void setUp() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test_api;DB_CLOSE_DELAY=-1;MODE=MySQL");
        ds.setUser("sa");
        ds.setPassword("");
        this.dataSource = ds;
        this.executor = new JdbcDatabaseExecutor(dataSource);

        createTestTables();

        this.dbContext = new CoreDbContext();
        this.dbContext.addDataSource(dataSource);
        this.dbContext.setDatabaseExecutor(executor);
        this.dbContext.init();

        DbContext.setCurrentContext(this.dbContext);

        this.dataTemplate = dbContext.getDataTemplate();
        this.queryTemplate = dbContext.getQueryTemplate();
    }

    @After
    public void tearDown() throws Exception {
        DbContext.setCurrentContext(null);
    }

    private void createTestTables() throws Exception {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS test_user");
            stmt.execute(
                    "CREATE TABLE test_user (" +
                            "id VARCHAR(32) PRIMARY KEY, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "age INT, " +
                            "email VARCHAR(200), " +
                            "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")");
        }
    }

    @Test
    public void testTableMetaDataLoaded() {
        TableMetaData tmd = dbContext.getConfigCenter().getTableMetaData("test_user");
        assertNotNull("TableMetaData should be loaded", tmd);
        assertEquals("test_user", tmd.getTableName());
    }

    @Test
    public void testDataTemplateInsert() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", "1");
        user.put("name", "张三");
        user.put("age", 25);
        user.put("email", "zhangsan@example.com");

        dataTemplate.insert("test_user", user);

        assertNotNull("ID should be set", user.get("id"));
    }

    @Test
    public void testQueryTemplateGetMap() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", "2");
        user.put("name", "李四");
        user.put("age", 30);
        user.put("email", "lisi@example.com");

        dataTemplate.insert("test_user", user);

        Map<String, Object> result = queryTemplate.getMap("test_user", "2");
        assertNotNull("User should be found", result);
        assertEquals("李四", result.get("NAME"));
        assertEquals(30, result.get("AGE"));
    }

    @Test
    public void testDataTemplateUpdate() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", "3");
        user.put("name", "原始名称");
        user.put("age", 20);
        dataTemplate.insert("test_user", user);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", "3");
        updateData.put("name", "更新名称");
        updateData.put("age", 25);
        int rows = dataTemplate.update("test_user", updateData);

        assertEquals("Should update 1 row", 1, rows);

        Map<String, Object> result = queryTemplate.getMap("test_user", "3");
        assertEquals("更新名称", result.get("NAME"));
        assertEquals(25, result.get("AGE"));
    }

    @Test
    public void testDataTemplateDelete() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", "4");
        user.put("name", "待删除");
        user.put("age", 35);
        dataTemplate.insert("test_user", user);

        int rows = dataTemplate.delete("test_user", "4");
        assertEquals("Should delete 1 row", 1, rows);

        Map<String, Object> result = queryTemplate.getMap("test_user", "4");
        assertNull("User should be deleted", result);
    }

    @Test
    public void testDataTemplateUpdateRows() {
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "5a");
        user1.put("name", "update_test");
        user1.put("age", 20);
        dataTemplate.insert("test_user", user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "5b");
        user2.put("name", "update_test");
        user2.put("age", 20);
        dataTemplate.insert("test_user", user2);

        Map<String, Object> valMap = new HashMap<>();
        valMap.put("age", 99);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", "update_test");

        int rows = dataTemplate.updateRows("test_user", valMap, paramMap);
        assertEquals("Should update 2 rows", 2, rows);
    }

    @Test
    public void testDataTemplateDeleteRows() {
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "6a");
        user1.put("name", "delete_test");
        user1.put("age", 25);
        dataTemplate.insert("test_user", user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "6b");
        user2.put("name", "delete_test");
        user2.put("age", 30);
        dataTemplate.insert("test_user", user2);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", "delete_test");

        int rows = dataTemplate.deleteRows("test_user", paramMap);
        assertEquals("Should delete 2 rows", 2, rows);
    }

    @Test
    public void testQueryTemplateGetBeanList() {
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", "list" + i);
            user.put("name", "用户" + i);
            user.put("age", 20 + i);
            dataTemplate.insert("test_user", user);
        }

        Map<String, Object> condition = new HashMap<>();
        condition.put("age >", 23);

        List<Map<String, Object>> results = queryTemplate.getMapList("test_user", condition);
        assertTrue("Should find at least 2 users", results.size() >= 2);
    }

    @Test
    public void testQueryTemplateGetCount() {
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", "count" + i);
            user.put("name", "计数用户" + i);
            user.put("age", 20 + i);
            dataTemplate.insert("test_user", user);
        }

        Map<String, Object> condition = new HashMap<>();
        long count = queryTemplate.getCount("test_user", condition);
        assertTrue("Count should be at least 3", count >= 3);
    }

    @Test
    public void testFullCrudWorkflow() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "完整流程");
        user.put("age", 40);
        user.put("email", "workflow@example.com");

        dataTemplate.insert("test_user", user);
        String userId = (String) user.get("id");
        assertNotNull("ID should be generated", userId);

        Map<String, Object> result = queryTemplate.getMap("test_user", userId);
        assertEquals("完整流程", result.get("NAME"));

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", userId);
        updateData.put("age", 41);
        dataTemplate.update("test_user", updateData);

        result = queryTemplate.getMap("test_user", userId);
        assertEquals(41, result.get("AGE"));

        dataTemplate.delete("test_user", userId);
        result = queryTemplate.getMap("test_user", userId);
        assertNull("User should be deleted", result);
    }

    @Test
    public void testUpdateRowsWithExactMatch() {
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "exact1");
        user1.put("name", "精确匹配A");
        user1.put("age", 25);
        dataTemplate.insert("test_user", user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "exact2");
        user2.put("name", "精确匹配A");
        user2.put("age", 30);
        dataTemplate.insert("test_user", user2);

        Map<String, Object> valMap = new HashMap<>();
        valMap.put("age", 99);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", "精确匹配A");

        int rows = dataTemplate.updateRows("test_user", valMap, paramMap);
        assertEquals("Should update 2 rows", 2, rows);
    }

    @Test
    public void testDeleteRowsWithExactMatch() {
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "delete_exact1");
        user1.put("name", "精确删除");
        user1.put("age", 25);
        dataTemplate.insert("test_user", user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "delete_exact2");
        user2.put("name", "精确删除");
        user2.put("age", 30);
        dataTemplate.insert("test_user", user2);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", "精确删除");

        int rows = dataTemplate.deleteRows("test_user", paramMap);
        assertEquals("Should delete 2 rows", 2, rows);
    }
}
