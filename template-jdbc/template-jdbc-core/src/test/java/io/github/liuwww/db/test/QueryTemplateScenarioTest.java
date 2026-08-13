package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
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
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.executor.impl.JdbcDatabaseExecutor;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.query.QueryBean;
import io.github.liuwww.db.service.IDataTemplate;
import io.github.liuwww.db.service.IQueryTemplate;

/**
 * QueryTemplate 使用场景测试
 * <p>
 * 以 {@link IQueryTemplate} 为入口，覆盖主键查询、等值查询、分页查询、
 * 计数查询及 QueryBean 流式条件查询（eq/like/gt/in/between 等）。
 * </p>
 */
public class QueryTemplateScenarioTest {

    private DataSource dataSource;
    private DatabaseExecutor executor;
    private CoreDbContext dbContext;
    private IDataTemplate dataTemplate;
    private IQueryTemplate queryTemplate;

    @Before
    public void setUp() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test_qt_scenario;DB_CLOSE_DELAY=-1;MODE=MySQL");
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

        prepareTestData();
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
                            "status INT DEFAULT 1, " +
                            "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")");
        }
    }

    private void prepareTestData() {
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", "u" + i);
            user.put("name", "用户" + i);
            user.put("age", 20 + (i % 10));
            user.put("email", "user" + i + "@example.com");
            user.put("status", i <= 10 ? 1 : 0);
            dataTemplate.insert("test_user", user);
        }
    }

    // ==================== 主键查询 ====================

    @Test
    public void testGetMapByPrimaryKey() {
        Map<String, Object> result = queryTemplate.getMap("test_user", "u1");
        assertNotNull("应能查询到记录", result);
        assertEquals("u1", result.get("ID"));
        assertEquals("用户1", result.get("NAME"));
    }

    @Test
    public void testGetMapByPrimaryKeyNotFound() {
        Map<String, Object> result = queryTemplate.getMap("test_user", "nonexistent");
        assertNull("不存在的记录应返回null", result);
    }

    @Test
    public void testGetBeanByPrimaryKey() {
        TestUserEntity user = queryTemplate.getBean("test_user", "u2", TestUserEntity.class);
        assertNotNull("应能查询到实体", user);
        assertEquals("u2", user.getId());
        assertEquals("用户2", user.getName());
    }

    @Test
    public void testGetBeanByPrimaryKeyNotFound() {
        TestUserEntity user = queryTemplate.getBean("test_user", "nonexistent", TestUserEntity.class);
        assertNull("不存在的记录应返回null", user);
    }

    // ==================== Map 条件查询 ====================

    @Test
    public void testGetMapListByCondition() {
        Map<String, Object> condition = new HashMap<>();
        condition.put("status", 1);

        List<Map<String, Object>> results = queryTemplate.getMapList("test_user", condition);
        assertEquals("status=1应有10条", 10, results.size());
    }

    @Test
    public void testGetMapListWithEmptyCondition() {
        List<Map<String, Object>> results = queryTemplate.getMapList("test_user", new HashMap<>());
        assertEquals("无条件应返回全部20条", 20, results.size());
    }

    @Test
    public void testGetCountByCondition() {
        Map<String, Object> condition = new HashMap<>();
        condition.put("status", 0);
        long count = queryTemplate.getCount("test_user", condition);
        assertEquals("status=0应有10条", 10L, count);
    }

    @Test
    public void testGetCountAll() {
        long count = queryTemplate.getCount("test_user", new HashMap<>());
        assertEquals(20L, count);
    }

    // ==================== 实体等值查询 ====================

    @Test
    public void testGetBeanListByEntity() {
        TestUserEntity condition = new TestUserEntity();
        condition.setStatus(1);

        List<TestUserEntity> results = queryTemplate.getBeanList(condition);
        assertNotNull(results);
        assertTrue("status=1应至少有10条", results.size() >= 10);
    }

    @Test
    public void testGetBeanByEntity() {
        TestUserEntity condition = new TestUserEntity();
        condition.setId("u5");

        TestUserEntity result = queryTemplate.getBean(condition);
        assertNotNull("应能查到id=u5的实体", result);
        assertEquals("u5", result.getId());
    }

    @Test
    public void testGetCountByEntity() {
        TestUserEntity condition = new TestUserEntity();
        condition.setStatus(0);

        long count = queryTemplate.getCount(condition);
        assertTrue("status=0应至少有10条", count >= 10);
    }

    // ==================== 分页查询 ====================

    @Test
    public void testGetPageByTableName() {
        Page page = new Page(1, 5);

        Page result = queryTemplate.getPage("test_user", page, new HashMap<>());
        assertNotNull(result);
        assertEquals("总数应为20", 20L, result.getTotal());
        assertEquals("每页5条", 5, result.getRows().size());
    }

    @Test
    public void testGetPageSecondPage() {
        Page page = new Page(2, 5);

        Page result = queryTemplate.getPage("test_user", page, new HashMap<>());
        assertNotNull(result);
        assertEquals(20L, result.getTotal());
        assertEquals(5, result.getRows().size());
    }

    @Test
    public void testGetPageByEntity() {
        TestUserEntity entity = new TestUserEntity();
        entity.setStatus(1);
        Page page = new Page(1, 3);

        Page result = queryTemplate.getPage(entity, page);
        assertNotNull(result);
        assertTrue("总数应 >= 10", result.getTotal() >= 10);
        assertEquals(3, result.getRows().size());
    }

    // ==================== QueryBean 流式查询 ====================

    @Test
    public void testQueryBeanEqCondition() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().eq("status", 1).getQueryBean();

        List<Map<String, Object>> results = queryBean.getMapList();
        assertEquals("status=1应有10条", 10, results.size());
    }

    @Test
    public void testQueryBeanLikeCondition() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().like("name", "用户1%").getQueryBean();

        List<Map<String, Object>> results = queryBean.getMapList();
        assertTrue("name LIKE '用户1%' 应包含用户1和用户10-19", results.size() >= 2);
    }

    @Test
    public void testQueryBeanGtCondition() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().gt("age", 25).getQueryBean();

        List<Map<String, Object>> results = queryBean.getMapList();
        for (Map<String, Object> row : results) {
            Integer age = (Integer) row.get("AGE");
            assertTrue("所有结果age应大于25", age > 25);
        }
    }

    @Test
    public void testQueryBeanMultipleConditions() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare()
                .eq("status", 1)
                .ge("age", 22)
                .getQueryBean();

        List<Map<String, Object>> results = queryBean.getMapList();
        for (Map<String, Object> row : results) {
            assertEquals(1, row.get("STATUS"));
            Integer age = (Integer) row.get("AGE");
            assertTrue("age应>=22", age >= 22);
        }
    }

    @Test
    public void testQueryBeanInCondition() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().in("id", Arrays.asList("u1", "u2", "u3")).getQueryBean();

        List<Map<String, Object>> results = queryBean.getMapList();
        assertEquals("IN查询应返回3条", 3, results.size());
    }

    @Test
    public void testQueryBeanRangeCondition() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().ge("age", 22).le("age", 25).getQueryBean();

        List<Map<String, Object>> results = queryBean.getMapList();
        for (Map<String, Object> row : results) {
            Integer age = (Integer) row.get("AGE");
            assertTrue("age应在22-25之间", age >= 22 && age <= 25);
        }
    }

    @Test
    public void testQueryBeanGetCount() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().eq("status", 1).getQueryBean();

        long count = queryBean.getCount();
        assertEquals("status=1应10条", 10L, count);
    }

    @Test
    public void testQueryBeanGetBeanListWithClass() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().eq("status", 0).getQueryBean();

        List<TestUserEntity> results = queryBean.getBeanList(TestUserEntity.class);
        assertNotNull(results);
        assertTrue("status=0应至少10条", results.size() >= 10);
        for (TestUserEntity u : results) {
            assertEquals(Integer.valueOf(0), u.getStatus());
        }
    }

    @Test
    public void testQueryBeanGetMap() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().eq("id", "u1").getQueryBean();

        Map<String, Object> result = queryBean.getMap();
        assertNotNull(result);
        assertEquals("u1", result.get("ID"));
    }

    @Test
    public void testQueryBeanAddOrderBy() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().gt("age", 20).getQueryBean();
        queryBean.addOrderBy("age DESC");

        List<Map<String, Object>> results = queryBean.getMapList();
        assertTrue("应有多条结果", results.size() > 1);

        Integer prevAge = null;
        for (Map<String, Object> row : results) {
            Integer age = (Integer) row.get("AGE");
            if (prevAge != null) {
                assertTrue("应按age降序排列", age <= prevAge);
            }
            prevAge = age;
        }
    }

    @Test
    public void testQueryBeanGetMapListWithMaxNum() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");

        List<Map<String, Object>> results = queryBean.getMapList(5);
        assertEquals("maxNum=5应返回5条", 5, results.size());
    }

    @Test
    public void testQueryBeanGetPage() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().gt("age", 20).getQueryBean();

        Page page = new Page(1, 5);
        Page result = queryBean.getPage(page);
        assertNotNull(result);
        assertEquals(5, result.getRows().size());
        assertTrue("总数应 > 5", result.getTotal() > 5);
    }

    // ==================== 综合查询工作流 ====================

    @Test
    public void testFullQueryWorkflow() {
        // 1. 主键查询
        Map<String, Object> user = queryTemplate.getMap("test_user", "u1");
        assertNotNull(user);

        // 2. 条件查询列表
        Map<String, Object> condition = new HashMap<>();
        condition.put("status", 1);
        List<Map<String, Object>> activeUsers = queryTemplate.getMapList("test_user", condition);
        assertEquals(10, activeUsers.size());

        // 3. 计数
        long count = queryTemplate.getCount("test_user", condition);
        assertEquals(10L, count);

        // 4. 分页
        Page page = queryTemplate.getPage("test_user", new Page(1, 5), condition);
        assertEquals(5, page.getRows().size());

        // 5. QueryBean复杂查询
        QueryBean qb = queryTemplate.createQueryBean("test_user");
        qb.getCompare().eq("status", 1).gt("age", 22).getQueryBean();
        List<Map<String, Object>> filtered = qb.getMapList();
        for (Map<String, Object> r : filtered) {
            assertEquals(1, r.get("STATUS"));
            assertTrue((Integer) r.get("AGE") > 22);
        }
    }
}
