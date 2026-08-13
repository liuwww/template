package io.github.liuwww.db.sql.util;

import io.github.liuwww.db.sql.DbType;

public class DefaultPaginationStrategy implements PaginationStrategy {

    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        return sql + " LIMIT " + offset + ", " + limit;
    }

    @Override
    public String buildCountSql(String sql) {
        return "SELECT COUNT(*) FROM (" + sql + ") AS T";
    }

    @Override
    public boolean supports(DbType dbType) {
        return true;
    }
}
