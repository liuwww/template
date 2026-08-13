package io.github.liuwww.db.transaction.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import io.github.liuwww.common.execption.DbException;
import io.github.liuwww.db.transaction.TransactionCallback;
import io.github.liuwww.db.transaction.TransactionManager;

public class JdbcTransactionManager implements TransactionManager {

    private DataSource dataSource;
    private ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private ThreadLocal<Boolean> inTransaction = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Override
    public void init(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void begin() {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new DbException("当前线程已在事务中");
        }
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
            inTransaction.set(Boolean.TRUE);
        } catch (SQLException e) {
            // setAutoCommit 失败时关闭已获取的连接，避免泄漏
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    // ignore
                }
            }
            throw new DbException("开启事务失败", e);
        }
    }

    @Override
    public void commit() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new DbException("当前没有活跃的事务");
        }
        try {
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DbException("提交事务失败", e);
        } finally {
            closeConnection();
        }
    }

    @Override
    public void rollback() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new DbException("当前没有活跃的事务");
        }
        try {
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DbException("回滚事务失败", e);
        } finally {
            closeConnection();
        }
    }

    @Override
    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        if (inTransaction.get()) {
            return callback.doInTransaction();
        }
        begin();
        try {
            T result = callback.doInTransaction();
            commit();
            return result;
        } catch (RuntimeException e) {
            rollback();
            throw e;
        } catch (Exception e) {
            rollback();
            throw new DbException("事务执行异常", e);
        }
    }

    @Override
    public boolean isInTransaction() {
        return inTransaction.get();
    }

    public Connection getCurrentConnection() {
        return currentConnection.get();
    }

    private void closeConnection() {
        Connection conn = currentConnection.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore close error
            }
            currentConnection.remove();
            inTransaction.set(Boolean.FALSE);
        }
    }
}
