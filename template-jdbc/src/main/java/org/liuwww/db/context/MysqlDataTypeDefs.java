
package org.liuwww.db.context;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public final class MysqlDataTypeDefs
{
    static final int COM_BINLOG_DUMP = 18;

    static final int COM_CHANGE_USER = 17;

    static final int COM_CLOSE_STATEMENT = 25;

    static final int COM_CONNECT_OUT = 20;

    static final int COM_END = 29;

    static final int COM_EXECUTE = 23;

    static final int COM_FETCH = 28;

    static final int COM_LONG_DATA = 24;

    static final int COM_PREPARE = 22;

    static final int COM_REGISTER_SLAVE = 21;

    static final int COM_RESET_STMT = 26;

    static final int COM_SET_OPTION = 27;

    static final int COM_TABLE_DUMP = 19;

    static final int CONNECT = 11;

    static final int CREATE_DB = 5;

    static final int DEBUG = 13;

    static final int DELAYED_INSERT = 16;

    static final int DROP_DB = 6;

    static final int FIELD_LIST = 4;

    static final int FIELD_TYPE_BIT = 16;

    public static final int FIELD_TYPE_BLOB = 252;

    static final int FIELD_TYPE_DATE = 10;

    static final int FIELD_TYPE_DATETIME = 12;

    // Data Types
    static final int FIELD_TYPE_DECIMAL = 0;

    static final int FIELD_TYPE_DOUBLE = 5;

    static final int FIELD_TYPE_ENUM = 247;

    static final int FIELD_TYPE_FLOAT = 4;

    static final int FIELD_TYPE_GEOMETRY = 255;

    static final int FIELD_TYPE_INT24 = 9;

    static final int FIELD_TYPE_LONG = 3;

    static final int FIELD_TYPE_LONG_BLOB = 251;

    static final int FIELD_TYPE_LONGLONG = 8;

    static final int FIELD_TYPE_MEDIUM_BLOB = 250;

    static final int FIELD_TYPE_NEW_DECIMAL = 246;

    static final int FIELD_TYPE_NEWDATE = 14;

    static final int FIELD_TYPE_NULL = 6;

    static final int FIELD_TYPE_SET = 248;

    static final int FIELD_TYPE_SHORT = 2;

    static final int FIELD_TYPE_STRING = 254;

    static final int FIELD_TYPE_TIME = 11;

    static final int FIELD_TYPE_TIMESTAMP = 7;

    static final int FIELD_TYPE_TINY = 1;

    // Older data types
    static final int FIELD_TYPE_TINY_BLOB = 249;

    static final int FIELD_TYPE_VAR_STRING = 253;

    static final int FIELD_TYPE_VARCHAR = 15;

    // Newer data types
    static final int FIELD_TYPE_YEAR = 13;

    static final int INIT_DB = 2;

    static int mysqlToJavaType(int mysqlType)
    {
        int jdbcType;

        switch (mysqlType)
        {
            case MysqlDataTypeDefs.FIELD_TYPE_NEW_DECIMAL:
            case MysqlDataTypeDefs.FIELD_TYPE_DECIMAL:
                jdbcType = Types.DECIMAL;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_TINY:
                jdbcType = Types.TINYINT;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_SHORT:
                jdbcType = Types.SMALLINT;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_LONG:
                jdbcType = Types.INTEGER;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_FLOAT:
                jdbcType = Types.REAL;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_DOUBLE:
                jdbcType = Types.DOUBLE;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_NULL:
                jdbcType = Types.NULL;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_TIMESTAMP:
                jdbcType = Types.TIMESTAMP;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_LONGLONG:
                jdbcType = Types.BIGINT;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_INT24:
                jdbcType = Types.INTEGER;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_DATE:
                jdbcType = Types.DATE;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_TIME:
                jdbcType = Types.TIME;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_DATETIME:
                jdbcType = Types.TIMESTAMP;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_YEAR:
                jdbcType = Types.DATE;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_NEWDATE:
                jdbcType = Types.DATE;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_ENUM:
                jdbcType = Types.CHAR;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_SET:
                jdbcType = Types.CHAR;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_TINY_BLOB:
                jdbcType = Types.VARBINARY;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_MEDIUM_BLOB:
                jdbcType = Types.LONGVARBINARY;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_LONG_BLOB:
                jdbcType = Types.LONGVARBINARY;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_BLOB:
                jdbcType = Types.LONGVARBINARY;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_VAR_STRING:
            case MysqlDataTypeDefs.FIELD_TYPE_VARCHAR:
                jdbcType = Types.VARCHAR;

                break;

            case MysqlDataTypeDefs.FIELD_TYPE_STRING:
                jdbcType = Types.CHAR;

                break;
            case MysqlDataTypeDefs.FIELD_TYPE_GEOMETRY:
                jdbcType = Types.BINARY;

                break;
            case MysqlDataTypeDefs.FIELD_TYPE_BIT:
                jdbcType = Types.BIT;

                break;
            default:
                jdbcType = Types.VARCHAR;
        }

        return jdbcType;
    }

    protected static int mysqlToJavaType(String mysqlType)
    {
        if (mysqlType.equalsIgnoreCase("BIT"))
        {
            return mysqlToJavaType(FIELD_TYPE_BIT);
        }
        else if (mysqlType.equalsIgnoreCase("TINYINT"))
        {
            return mysqlToJavaType(FIELD_TYPE_TINY);
        }
        else if (mysqlType.equalsIgnoreCase("SMALLINT"))
        {
            return mysqlToJavaType(FIELD_TYPE_SHORT);
        }
        else if (mysqlType.equalsIgnoreCase("MEDIUMINT"))
        {
            return mysqlToJavaType(FIELD_TYPE_INT24);
        }
        else if (mysqlType.equalsIgnoreCase("INT") || mysqlType.equalsIgnoreCase("INTEGER"))
        {
            return mysqlToJavaType(FIELD_TYPE_LONG);
        }
        else if (mysqlType.equalsIgnoreCase("BIGINT"))
        {
            return mysqlToJavaType(FIELD_TYPE_LONGLONG);
        }
        else if (mysqlType.equalsIgnoreCase("INT24"))
        {
            return mysqlToJavaType(FIELD_TYPE_INT24);
        }
        else if (mysqlType.equalsIgnoreCase("REAL"))
        {
            return mysqlToJavaType(FIELD_TYPE_DOUBLE);
        }
        else if (mysqlType.equalsIgnoreCase("FLOAT"))
        {
            return mysqlToJavaType(FIELD_TYPE_FLOAT);
        }
        else if (mysqlType.equalsIgnoreCase("DECIMAL"))
        {
            return mysqlToJavaType(FIELD_TYPE_DECIMAL);
        }
        else if (mysqlType.equalsIgnoreCase("NUMERIC"))
        {
            return mysqlToJavaType(FIELD_TYPE_DECIMAL);
        }
        else if (mysqlType.equalsIgnoreCase("DOUBLE"))
        {
            return mysqlToJavaType(FIELD_TYPE_DOUBLE);
        }
        else if (mysqlType.equalsIgnoreCase("CHAR"))
        {
            return mysqlToJavaType(FIELD_TYPE_STRING);
        }
        else if (mysqlType.equalsIgnoreCase("VARCHAR"))
        {
            return mysqlToJavaType(FIELD_TYPE_VAR_STRING);
        }
        else if (mysqlType.equalsIgnoreCase("DATE"))
        {
            return mysqlToJavaType(FIELD_TYPE_DATE);
        }
        else if (mysqlType.equalsIgnoreCase("TIME"))
        {
            return mysqlToJavaType(FIELD_TYPE_TIME);
        }
        else if (mysqlType.equalsIgnoreCase("YEAR"))
        {
            return mysqlToJavaType(FIELD_TYPE_YEAR);
        }
        else if (mysqlType.equalsIgnoreCase("TIMESTAMP"))
        {
            return mysqlToJavaType(FIELD_TYPE_TIMESTAMP);
        }
        else if (mysqlType.equalsIgnoreCase("DATETIME"))
        {
            return mysqlToJavaType(FIELD_TYPE_DATETIME);
        }
        else if (mysqlType.equalsIgnoreCase("TINYBLOB"))
        {
            return java.sql.Types.BINARY;
        }
        else if (mysqlType.equalsIgnoreCase("BLOB"))
        {
            return java.sql.Types.LONGVARBINARY;
        }
        else if (mysqlType.equalsIgnoreCase("MEDIUMBLOB"))
        {
            return java.sql.Types.LONGVARBINARY;
        }
        else if (mysqlType.equalsIgnoreCase("LONGBLOB"))
        {
            return java.sql.Types.LONGVARBINARY;
        }
        else if (mysqlType.equalsIgnoreCase("TINYTEXT"))
        {
            return java.sql.Types.VARCHAR;
        }
        else if (mysqlType.equalsIgnoreCase("TEXT"))
        {
            return java.sql.Types.LONGVARCHAR;
        }
        else if (mysqlType.equalsIgnoreCase("MEDIUMTEXT"))
        {
            return java.sql.Types.LONGVARCHAR;
        }
        else if (mysqlType.equalsIgnoreCase("LONGTEXT"))
        {
            return java.sql.Types.LONGVARCHAR;
        }
        else if (mysqlType.equalsIgnoreCase("ENUM"))
        {
            return mysqlToJavaType(FIELD_TYPE_ENUM);
        }
        else if (mysqlType.equalsIgnoreCase("SET"))
        {
            return mysqlToJavaType(FIELD_TYPE_SET);
        }
        else if (mysqlType.equalsIgnoreCase("GEOMETRY"))
        {
            return mysqlToJavaType(FIELD_TYPE_GEOMETRY);
        }
        else if (mysqlType.equalsIgnoreCase("BINARY"))
        {
            return Types.BINARY; // no concrete type on the wire
        }
        else if (mysqlType.equalsIgnoreCase("VARBINARY"))
        {
            return Types.VARBINARY; // no concrete type on the wire
        }
        else if (mysqlType.equalsIgnoreCase("BIT"))
        {
            return mysqlToJavaType(FIELD_TYPE_BIT);
        }

        // Punt
        return java.sql.Types.OTHER;
    }

    /**
     * @param mysqlType
     */
    public static String typeToName(int mysqlType)
    {
        switch (mysqlType)
        {
            case MysqlDataTypeDefs.FIELD_TYPE_DECIMAL:
                return "FIELD_TYPE_DECIMAL";

            case MysqlDataTypeDefs.FIELD_TYPE_TINY:
                return "FIELD_TYPE_TINY";

            case MysqlDataTypeDefs.FIELD_TYPE_SHORT:
                return "FIELD_TYPE_SHORT";

            case MysqlDataTypeDefs.FIELD_TYPE_LONG:
                return "FIELD_TYPE_LONG";

            case MysqlDataTypeDefs.FIELD_TYPE_FLOAT:
                return "FIELD_TYPE_FLOAT";

            case MysqlDataTypeDefs.FIELD_TYPE_DOUBLE:
                return "FIELD_TYPE_DOUBLE";

            case MysqlDataTypeDefs.FIELD_TYPE_NULL:
                return "FIELD_TYPE_NULL";

            case MysqlDataTypeDefs.FIELD_TYPE_TIMESTAMP:
                return "FIELD_TYPE_TIMESTAMP";

            case MysqlDataTypeDefs.FIELD_TYPE_LONGLONG:
                return "FIELD_TYPE_LONGLONG";

            case MysqlDataTypeDefs.FIELD_TYPE_INT24:
                return "FIELD_TYPE_INT24";

            case MysqlDataTypeDefs.FIELD_TYPE_DATE:
                return "FIELD_TYPE_DATE";

            case MysqlDataTypeDefs.FIELD_TYPE_TIME:
                return "FIELD_TYPE_TIME";

            case MysqlDataTypeDefs.FIELD_TYPE_DATETIME:
                return "FIELD_TYPE_DATETIME";

            case MysqlDataTypeDefs.FIELD_TYPE_YEAR:
                return "FIELD_TYPE_YEAR";

            case MysqlDataTypeDefs.FIELD_TYPE_NEWDATE:
                return "FIELD_TYPE_NEWDATE";

            case MysqlDataTypeDefs.FIELD_TYPE_ENUM:
                return "FIELD_TYPE_ENUM";

            case MysqlDataTypeDefs.FIELD_TYPE_SET:
                return "FIELD_TYPE_SET";

            case MysqlDataTypeDefs.FIELD_TYPE_TINY_BLOB:
                return "FIELD_TYPE_TINY_BLOB";

            case MysqlDataTypeDefs.FIELD_TYPE_MEDIUM_BLOB:
                return "FIELD_TYPE_MEDIUM_BLOB";

            case MysqlDataTypeDefs.FIELD_TYPE_LONG_BLOB:
                return "FIELD_TYPE_LONG_BLOB";

            case MysqlDataTypeDefs.FIELD_TYPE_BLOB:
                return "FIELD_TYPE_BLOB";

            case MysqlDataTypeDefs.FIELD_TYPE_VAR_STRING:
                return "FIELD_TYPE_VAR_STRING";

            case MysqlDataTypeDefs.FIELD_TYPE_STRING:
                return "FIELD_TYPE_STRING";

            case MysqlDataTypeDefs.FIELD_TYPE_VARCHAR:
                return "FIELD_TYPE_VARCHAR";

            case MysqlDataTypeDefs.FIELD_TYPE_GEOMETRY:
                return "FIELD_TYPE_GEOMETRY";
            default:
                return " Unknown MySQL Type # " + mysqlType;
        }
    }

    private static Map<String, Integer> mysqlToJdbcTypesMap = new HashMap<String, Integer>();
    static
    {
        mysqlToJdbcTypesMap.put("BIT", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_BIT)));
        mysqlToJdbcTypesMap.put("TINYINT", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_TINY)));
        mysqlToJdbcTypesMap.put("SMALLINT", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_SHORT)));
        mysqlToJdbcTypesMap.put("MEDIUMINT", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_INT24)));
        mysqlToJdbcTypesMap.put("INT", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_LONG)));
        mysqlToJdbcTypesMap.put("INTEGER", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_LONG)));
        mysqlToJdbcTypesMap.put("BIGINT", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_LONGLONG)));
        mysqlToJdbcTypesMap.put("INT24", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_INT24)));
        mysqlToJdbcTypesMap.put("REAL", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_DOUBLE)));
        mysqlToJdbcTypesMap.put("FLOAT", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_FLOAT)));
        mysqlToJdbcTypesMap.put("DECIMAL", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_DECIMAL)));
        mysqlToJdbcTypesMap.put("NUMERIC", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_DECIMAL)));
        mysqlToJdbcTypesMap.put("DOUBLE", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_DOUBLE)));
        mysqlToJdbcTypesMap.put("CHAR", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_STRING)));
        mysqlToJdbcTypesMap.put("VARCHAR", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_VAR_STRING)));
        mysqlToJdbcTypesMap.put("DATE", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_DATE)));
        mysqlToJdbcTypesMap.put("TIME", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_TIME)));
        mysqlToJdbcTypesMap.put("YEAR", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_YEAR)));
        mysqlToJdbcTypesMap.put("TIMESTAMP", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_TIMESTAMP)));
        mysqlToJdbcTypesMap.put("DATETIME", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_DATETIME)));
        mysqlToJdbcTypesMap.put("TINYBLOB", Integer.valueOf(java.sql.Types.BINARY));
        mysqlToJdbcTypesMap.put("BLOB", Integer.valueOf(java.sql.Types.LONGVARBINARY));
        mysqlToJdbcTypesMap.put("MEDIUMBLOB", Integer.valueOf(java.sql.Types.LONGVARBINARY));
        mysqlToJdbcTypesMap.put("LONGBLOB", Integer.valueOf(java.sql.Types.LONGVARBINARY));
        mysqlToJdbcTypesMap.put("TINYTEXT", Integer.valueOf(java.sql.Types.VARCHAR));
        mysqlToJdbcTypesMap.put("TEXT", Integer.valueOf(java.sql.Types.LONGVARCHAR));
        mysqlToJdbcTypesMap.put("MEDIUMTEXT", Integer.valueOf(java.sql.Types.LONGVARCHAR));
        mysqlToJdbcTypesMap.put("LONGTEXT", Integer.valueOf(java.sql.Types.LONGVARCHAR));
        mysqlToJdbcTypesMap.put("ENUM", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_ENUM)));
        mysqlToJdbcTypesMap.put("SET", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_SET)));
        mysqlToJdbcTypesMap.put("GEOMETRY", Integer.valueOf(mysqlToJavaType(FIELD_TYPE_GEOMETRY)));
    }
}
