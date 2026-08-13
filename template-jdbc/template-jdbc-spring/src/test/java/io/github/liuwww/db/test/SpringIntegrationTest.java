package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.github.liuwww.db.config.TemplateJdbcConfig;
import io.github.liuwww.db.context.DbContext;
import io.github.liuwww.db.executor.SpringDatabaseExecutor;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.query.QueryBean;
import io.github.liuwww.db.service.IDataTemplate;
import io.github.liuwww.db.service.IQueryTemplate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Spring 集成测试
 * <p>
 * 验证 Spring 环境下 {@link TemplateJdbcConfig} 装配的 Bean 能正常工作，
 * 覆盖 DataTemplate 增删改、QueryTemplate 查询/分页/QueryBean 等场景。
 * </p>
 *
 * @author liuwww
 */
public class SpringIntegrationTest {

    private AnnotationConfigApplicationContext context;
    private DataSource dataSource;
    private IDataTemplate dataTemplate;
    private IQueryTemplate queryTemplate;
    private SpringDatabaseExecutor databaseExecutor;

    @Before
    public void setUp() throws Exception {
        context = new AnnotationConfigApplicationContext(SpringTestConfig.class, TemplateJdbcConfig.class);
        dataSource = context.getBean(DataSource.class);
        dataTemplate = context.getBean(IDataTemplate.class);
        queryTemplate = context.getBean(IQueryTemplate.class);
        databaseExecutor = context.getBean(SpringDatabaseExecutor.class);

        createTestTables();

        // 表创建后刷新元数据，使 configCenter 能识别新表
        context.getBean(io.github.liuwww.db.config.DatabaseConfigCenter.class).refresh();

        prepareTestData();
    }

    @After
    public void tearDown() throws Exception {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS test_user");
        }
        DbContext.setCurrentContext(null);
        if (context != null) {
            context.close();
        }
    }

    private void createTestTables() throws Exception {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS test_user (" +
                    "id VARCHAR(32) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "age INT, " +
                    "email VARCHAR(200), " +
                    "status INT DEFAULT 1" +
                    ")");
        }
    }

    private void prepareTestData() throws Exception {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            for (int i = 1; i <= 20; i++) {
                String id = "spring-u" + String.format("%03d", i);
                String name = "spring用户" + i;
                int age = 20 + i;
                String email = "user" + i + "@spring.test";
                int status = i <= 10 ? 1 : 0;
                stmt.execute(String.format(
                        "INSERT INTO test_user (id, name, age, email, status) VALUES ('%s', '%s', %d, '%s', %d)",
                        id, name, age, email, status));
            }
        }
    }

    // ==================== Bean 装配验证 ====================

    @Test
    public void testBeanWiring() {
        assertNotNull("DataTemplate 应被注入", dataTemplate);
        assertNotNull("QueryTemplate 应被注入", queryTemplate);
        assertNotNull("SpringDatabaseExecutor 应被注入", databaseExecutor);
        assertNotNull("DbContext 上下文应已注册", DbContext.getCurrentContext());
    }

    // ==================== DataTemplate 测试 ====================

    @Test
    public void testInsertEntity() {
        SpringTestUserEntity user = new SpringTestUserEntity();
        user.setId("spring-insert-001");
        user.setName("插入测试");
        user.setAge(30);
        user.setEmail("insert@spring.test");
        user.setStatus(1);

        SpringTestUserEntity result = dataTemplate.insert(user);
        assertNotNull("插入应返回实体", result);

        Map<String, Object> condition = new HashMap<>();
        condition.put("id", "spring-insert-001");
        long count = queryTemplate.getCount("test_user", condition);
        assertEquals("应查询到 1 条记录", 1, count);
    }

    @Test
    public void testUpdateEntity() {
        SpringTestUserEntity user = new SpringTestUserEntity();
        user.setId("spring-u001");
        user.setName("已更新名称");
        user.setAge(99);

        int result = dataTemplate.update(user);
        assertEquals("更新应返回 1", 1, result);

        Map<String, Object> condition = new HashMap<>();
        condition.put("id", "spring-u001");
        List<Map<String, Object>> list = queryTemplate.getMapList("test_user", condition);
        assertEquals("应查询到 1 条", 1, list.size());
        assertEquals("名称应已更新", "已更新名称", list.get(0).get("name"));
    }

    @Test
    public void testDeleteEntity() {
        SpringTestUserEntity user = new SpringTestUserEntity();
        user.setId("spring-u002");

        int result = dataTemplate.delete(user);
        assertEquals("删除应返回 1", 1, result);

        Map<String, Object> condition = new HashMap<>();
        condition.put("id", "spring-u002");
        long count = queryTemplate.getCount("test_user", condition);
        assertEquals("删除后应查询到 0 条", 0, count);
    }

    @Test
    public void testBatchInsert() {
        java.util.List<SpringTestUserEntity> list = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SpringTestUserEntity user = new SpringTestUserEntity();
            user.setId("spring-batch-" + i);
            user.setName("批量用户" + i);
            user.setAge(25 + i);
            user.setEmail("batch" + i + "@spring.test");
            user.setStatus(1);
            list.add(user);
        }

        java.util.List<SpringTestUserEntity> inserted = dataTemplate.insert(list);
        assertEquals("批量插入应返回 5 条", 5, inserted.size());

        Map<String, Object> condition = new HashMap<>();
        condition.put("status", 1);
        long count = queryTemplate.getCount("test_user", condition);
        assertTrue("status=1 的记录数应 >= 5", count >= 5);
    }

    // ==================== QueryTemplate 测试 ====================

    @Test
    public void testGetBeanByPrimaryKey() {
        SpringTestUserEntity user = queryTemplate.getBean("test_user", "spring-u001",
                SpringTestUserEntity.class);
        assertNotNull("应查询到实体", user);
        assertEquals("ID 应匹配", "spring-u001", user.getId());
        assertEquals("名称应匹配", "spring用户1", user.getName());
    }

    @Test
    public void testGetCountByCondition() {
        Map<String, Object> condition = new HashMap<>();
        condition.put("status", 1);
        long count = queryTemplate.getCount("test_user", condition);
        assertEquals("status=1 应有 10 条", 10, count);
    }

    @Test
    public void testGetMapListWithCondition() {
        Map<String, Object> condition = new HashMap<>();
        condition.put("status", 0);
        List<Map<String, Object>> list = queryTemplate.getMapList("test_user", condition);
        assertEquals("status=0 应有 10 条", 10, list.size());
    }

    @Test
    public void testGetPage() {
        Page page = new Page(1, 5);
        Map<String, Object> condition = new HashMap<>();
        condition.put("status", 1);
        Page result = queryTemplate.getPage("test_user", page, condition);
        assertEquals("总记录数应为 10", 10L, result.getTotal());
        assertEquals("当前页记录数应为 5", 5, result.getRows().size());
    }

    // ==================== QueryBean 流式查询测试 ====================

    @Test
    public void testQueryBeanEqCondition() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().eq("status", 1).getQueryBean();

        List<Map<String, Object>> results = queryBean.getMapList();
        assertEquals("status=1 应有 10 条", 10, results.size());
    }

    @Test
    public void testQueryBeanGtCondition() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().gt("age", 30).getQueryBean();

        List<Map<String, Object>> results = queryBean.getMapList();
        assertTrue("age>30 应有多条记录", results.size() > 0);
    }

    @Test
    public void testQueryBeanLikeCondition() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().like("name", "spring用户1%").getQueryBean();

        List<Map<String, Object>> results = queryBean.getMapList();
        assertTrue("name LIKE 'spring用户1%' 应有多条", results.size() >= 2);
    }

    @Test
    public void testQueryBeanGetCount() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().eq("status", 0).getQueryBean();

        long count = queryBean.getCount();
        assertEquals("status=0 应有 10 条", 10, count);
    }

    @Test
    public void testQueryBeanGetPage() {
        QueryBean queryBean = queryTemplate.createQueryBean("test_user");
        queryBean.getCompare().eq("status", 1).getQueryBean();

        Page page = new Page(1, 3);
        Page result = queryBean.getPage(page);
        assertEquals("总记录数应为 10", 10L, result.getTotal());
        assertEquals("当前页记录数应为 3", 3, result.getRows().size());
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
            Integer age = (Integer) row.get("age");
            if (prevAge != null) {
                assertTrue("应按 age 降序排列", age <= prevAge);
            }
            prevAge = age;
        }
    }
}
