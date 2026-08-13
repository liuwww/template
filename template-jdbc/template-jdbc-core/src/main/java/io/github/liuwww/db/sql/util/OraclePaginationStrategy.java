package io.github.liuwww.db.sql.util;

import io.github.liuwww.db.sql.DbType;

public class OraclePaginationStrategy implements PaginationStrategy {

    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        StringBuilder sb = new StringBuilder(sql.length() + 50);
        sb.append("SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( ");
        sb.append(sql);
        sb.append(" ) TMP WHERE ROWNUM <= ");
        sb.append(offset + limit);
        sb.append(" ) WHERE ROW_ID > ");
        sb.append(offset);
        return sb.toString();
    }

    @Override
    public String buildCountSql(String sql) {
        return "SELECT COUNT(*) FROM (" + sql + ")";
    }

    @Override
    public boolean supports(DbType dbType) {
        return DbType.ORACLE == dbType;
    }
}
