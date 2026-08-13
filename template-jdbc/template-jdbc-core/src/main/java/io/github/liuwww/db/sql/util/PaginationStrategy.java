package io.github.liuwww.db.sql.util;

import io.github.liuwww.db.sql.DbType;

public interface PaginationStrategy {

    String buildPaginationSql(String sql, int offset, int limit);

    String buildCountSql(String sql);

    boolean supports(DbType dbType);
}
