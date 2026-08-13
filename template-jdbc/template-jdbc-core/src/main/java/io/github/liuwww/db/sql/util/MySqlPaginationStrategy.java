package io.github.liuwww.db.sql.util;

import io.github.liuwww.db.sql.DbType;

public class MySqlPaginationStrategy implements PaginationStrategy {

    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        StringBuilder sb = new StringBuilder(sql.length() + 20);
        sb.append(sql);
        sb.append(" LIMIT ");
        if (offset > 0) {
            sb.append(offset).append(", ");
        }
        sb.append(limit);
        return sb.toString();
    }

    @Override
    public String buildCountSql(String sql) {
        return "SELECT COUNT(*) FROM (" + sql + ") AS T";
    }

    @Override
    public boolean supports(DbType dbType) {
        return DbType.MYSQL == dbType;
    }
}
