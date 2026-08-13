package io.github.liuwww.db.sql.util;

import java.util.ArrayList;
import java.util.List;

import io.github.liuwww.db.sql.DbType;

public class PaginationFactory {

    private static final List<PaginationStrategy> STRATEGIES = new ArrayList<>();

    static {
        STRATEGIES.add(new MySqlPaginationStrategy());
        STRATEGIES.add(new OraclePaginationStrategy());
        STRATEGIES.add(new H2PaginationStrategy());
        STRATEGIES.add(new PostgreSqlPaginationStrategy());
        STRATEGIES.add(new DefaultPaginationStrategy());
    }

    public static PaginationStrategy getStrategy(DbType dbType) {
        for (PaginationStrategy strategy : STRATEGIES) {
            if (strategy.supports(dbType)) {
                return strategy;
            }
        }
        return new DefaultPaginationStrategy();
    }

    public static String limit(String sql, DbType dbType, int offset, int limit) {
        return getStrategy(dbType).buildPaginationSql(sql, offset, limit);
    }

    public static String count(String sql, DbType dbType) {
        return getStrategy(dbType).buildCountSql(sql);
    }
}
