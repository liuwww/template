package io.github.liuwww.db.test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import io.github.liuwww.db.config.impl.GenericDatabaseConfigCenter;
import io.github.liuwww.db.context.CoreDbContext;
import io.github.liuwww.db.context.DbContext;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.executor.impl.JdbcDatabaseExecutor;

public abstract class BaseTest {

    protected DataSource dataSource;
    protected DatabaseExecutor executor;
    protected static CoreDbContext coreDbContext;

    @Before
    public void setUp() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        ds.setUser("sa");
        ds.setPassword("");
        this.dataSource = ds;
        this.executor = new JdbcDatabaseExecutor(dataSource);

        // 先创建测试表
        initTestTables();

        // 初始化DbContext（CoreDbContext是单例，只能实例化一次）
        if (coreDbContext == null) {
            coreDbContext = new CoreDbContext();
            coreDbContext.addDataSource(dataSource);
            coreDbContext.setDatabaseExecutor(executor);
            coreDbContext.init();
        } else {
            // 如果已经存在，添加数据源并刷新
            GenericDatabaseConfigCenter configCenter = (GenericDatabaseConfigCenter) coreDbContext.getConfigCenter();
            configCenter.addDataSource(dataSource);
            configCenter.loadTableStructureFromDataSource(dataSource);
        }
        // 每次测试都确保 DbContext 指向当前 coreDbContext，防止其他测试污染
        DbContext.setCurrentContext(coreDbContext);
    }

    @After
    public void tearDown() throws Exception {
        cleanupTestData();
    }

    private void initTestTables() throws Exception {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS test_user (" +
                            "id VARCHAR(32) PRIMARY KEY, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "age INT, " +
                            "email VARCHAR(200), " +
                            "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")");

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS test_order (" +
                            "id VARCHAR(32) PRIMARY KEY, " +
                            "user_id VARCHAR(32) NOT NULL, " +
                            "order_no VARCHAR(100) NOT NULL, " +
                            "amount DECIMAL(10,2), " +
                            "status INT DEFAULT 0, " +
                            "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")");
        }
    }

    private void cleanupTestData() throws Exception {
        if (dataSource == null) {
            return;
        }
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM test_order");
            stmt.execute("DELETE FROM test_user");
        } catch (Exception e) {
            // 忽略清理错误
        }
    }

    protected void printMap(Map<String, Object> map) {
        if (map == null) {
            System.out.println("null");
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + entry.getValue());
        }
    }
}