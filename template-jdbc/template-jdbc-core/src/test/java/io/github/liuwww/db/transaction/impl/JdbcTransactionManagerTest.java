package io.github.liuwww.db.transaction.impl;

import static org.junit.Assert.*;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.github.liuwww.db.test.BaseTest;
import io.github.liuwww.db.transaction.TransactionCallback;

public class JdbcTransactionManagerTest extends BaseTest {

    private JdbcTransactionManager transactionManager;

    @Before
    public void setUpBase() throws Exception {
        super.setUp();
        transactionManager = new JdbcTransactionManager();
        transactionManager.init(dataSource);
    }

    @After
    public void tearDownBase() throws Exception {
        transactionManager = null;
        super.tearDown();
    }

    @Test
    public void testBeginTransaction() {
        transactionManager.begin();
        assertTrue(transactionManager.isInTransaction());
        assertNotNull(transactionManager.getCurrentConnection());

        try {
            transactionManager.getCurrentConnection().rollback();
        } catch (Exception e) {
        }
    }

    @Test
    public void testCommitTransaction() throws Exception {
        transactionManager.begin();
        transactionManager.commit();
        assertFalse(transactionManager.isInTransaction());
    }

    @Test
    public void testRollbackTransaction() throws Exception {
        transactionManager.begin();
        transactionManager.rollback();
        assertFalse(transactionManager.isInTransaction());
    }

    @Test
    public void testExecuteInTransactionWithSuccess() {
        String result = transactionManager.executeInTransaction(new TransactionCallback<String>() {
            @Override
            public String doInTransaction() {
                return "success";
            }
        });

        assertEquals("success", result);
        assertFalse(transactionManager.isInTransaction());
    }

    @Test
    public void testExecuteInTransactionWithException() {
        try {
            transactionManager.executeInTransaction(new TransactionCallback<String>() {
                @Override
                public String doInTransaction() {
                    throw new RuntimeException("test error");
                }
            });
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("test error", e.getMessage());
            assertFalse(transactionManager.isInTransaction());
        }
    }

    @Test
    public void testExecuteInTransactionWithLambda() {
        Integer result = transactionManager.executeInTransaction(() -> 42);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test(expected = Exception.class)
    public void testBeginTransactionTwice() {
        transactionManager.begin();
        transactionManager.begin();
    }

    @Test
    public void testCommitWithoutTransaction() {
        try {
            transactionManager.commit();
            fail("Expected DbException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("没有活跃的事务") || e.getMessage().contains("not active"));
        }
    }

    @Test
    public void testRollbackWithoutTransaction() {
        try {
            transactionManager.rollback();
            fail("Expected DbException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("没有活跃的事务") || e.getMessage().contains("not active"));
        }
    }
}
