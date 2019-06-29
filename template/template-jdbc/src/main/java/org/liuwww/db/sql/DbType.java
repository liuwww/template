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
    H2 {
        @Override
        public String toString()
        {
            return "h2";
        }
    },
    PostgreSQL {
        @Override
        public String toString()
        {
            return "postgresql";
        }
    },
    OTHER {
        @Override
        public String toString()
        {
            return "other";
        }
    };

    public static DbType getByName(String dbType)
    {
        try
        {
            return DbType.valueOf(dbType);
        }
        catch (Exception e)
        {
            return OTHER;
        }
    }

}
