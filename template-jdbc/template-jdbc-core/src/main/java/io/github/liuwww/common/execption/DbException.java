package io.github.liuwww.common.execption;

import java.util.Arrays;

public class DbException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String errorCode;
    private String sql;
    private Object[] params;
    private String tableName;

    public DbException() {
    }

    public DbException(String msg) {
        super(msg);
    }

    public DbException(Throwable e) {
        super(e);
    }

    public DbException(String msg, Throwable e) {
        super(msg, e);
    }

    public DbException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DbException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public DbException withSql(String sql, Object[] params) {
        this.sql = sql;
        this.params = params;
        return this;
    }

    public DbException withTable(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public DbException withErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getSql() {
        return sql;
    }

    public Object[] getParams() {
        return params;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (errorCode != null) {
            sb.append("[").append(errorCode).append("] ");
        }
        sb.append(getMessage());
        if (tableName != null) {
            sb.append(" | table: ").append(tableName);
        }
        if (sql != null) {
            sb.append(" | sql: ").append(sql);
        }
        if (params != null) {
            sb.append(" | params: ").append(Arrays.toString(params));
        }
        if (getCause() != null) {
            sb.append(" | cause: ").append(getCause().getMessage());
        }
        return sb.toString();
    }
}
