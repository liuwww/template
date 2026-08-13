package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.github.liuwww.db.config.DatabaseConfigCenter;
import io.github.liuwww.db.context.CoreDbContext;
import io.github.liuwww.db.context.DbContext;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.executor.impl.JdbcDatabaseExecutor;

public class TableStructureLoadTest {

    private DataSource dataSource;
    private DatabaseExecutor executor;
    private CoreDbContext dbContext;

    @Before
    public void setUp() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test_load;DB_CLOSE_DELAY=-1;MODE=MySQL");
        ds.setUser("sa");
        ds.setPassword("");
        this.dataSource = ds;
        this.executor = new JdbcDatabaseExecutor(dataSource);

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS test_user");
            stmt.execute(
                    "CREATE TABLE test_user (" +
                            "id VARCHAR(32) PRIMARY KEY, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "age INT, " +
                            "email VARCHAR(200)" +
                            ")");
        }

        this.dbContext = new CoreDbContext();
        this.dbContext.addDataSource(dataSource);
        this.dbContext.setDatabaseExecutor(executor);
        this.dbContext.init();

        DbContext.setCurrentContext(this.dbContext);
    }

    @After
    public void tearDown() throws Exception {
        DbContext.setCurrentContext(null);
    }

    @Test
    public void testTableStructureLoad() {
        DatabaseConfigCenter configCenter = dbContext.getConfigCenter();
        
        System.out.println("=== 测试表结构加载 ===");
        System.out.println("所有表名: " + configCenter.getTableNames());
        
        TableMetaData tmd = configCenter.getTableMetaData("test_user");
        System.out.println("test_user 的 TableMetaData: " + tmd);
        
        assertNotNull("TableMetaData should be loaded", tmd);
        assertEquals("test_user", tmd.getTableName());
    }

    @Test
    public void testTableStructureLoadUpperCase() {
        TableMetaData tmd = dbContext.getConfigCenter().getTableMetaData("TEST_USER");
        assertNotNull("TableMetaData should be loaded with uppercase name", tmd);
        assertEquals("test_user", tmd.getTableName());
    }
}