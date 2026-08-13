package io.github.liuwww.db.sql;

import io.github.liuwww.common.util.StringUtil;

public enum DbType {
    ORACLE {
        @Override
        public String toString() {
            return "oracle";
        }
    },
    MYSQL {
        @Override
        public String toString() {
            return "mysql";
        }
    },
    DB2 {
        @Override
        public String toString() {
            return "db2";
        }
    },
    SQLSERVER {
        @Override
        public String toString() {
            return "sqlserver";
        }
    },
    SYBASE {
        @Override
        public String toString() {
            return "sybase";
        }
    },
    H2 {
        @Override
        public String toString() {
            return "h2";
        }
    },
    PostgreSQL {
        @Override
        public String toString() {
            return "postgresql";
        }
    },
    OTHER {
        @Override
        public String toString() {
            return "other";
        }
    };

    public static DbType getByName(String dbType) {
        for (DbType v : values()) {
            if (StringUtil.equalsIgnoreCase(v.toString(), dbType)) {
                return v;
            }
        }
        return DbType.OTHER;
    }

}
