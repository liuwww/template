package org.liuwww.db.sql;

public enum DbType {
    ORACLE {
        @Override
        public String toString()
        {
            return "oracle";
        }
    },
    MYSQL {
        @Override
        public String toString()
        {
            return "mysql";
        }
    },
    DB2 {
        @Override
        public String toString()
        {
            return "db2";
        }
    },
    SQLSERVER {
        @Override
        public String toString()
        {
            return "sqlserver";
        }
    },
    SYBASE {
        @Override
        public String toString()
        {
            return "sybase";
        }
    },
    OTHER {
        @Override
        public String toString()
        {
            return "other";
        }
    };

}
