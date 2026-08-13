package io.github.liuwww.db.sql;

/**
 * 数据库数据类型枚举
 * <p>
 * 统一不同数据库的数据类型定义，兼容 MySQL、Oracle、PostgreSQL、SQL Server 等主流数据库。
 * 使用别名匹配机制，支持不同数据库相同语义的不同类型名称。
 * </p>
 */
public enum SqlDataType {
    VARCHAR("varchar", "varchar2", "character varying", "nvarchar", "national varchar"),
    CHAR("char", "character", "nchar", "national char"),
    TEXT("text", "clob", "ntext", "longtext", "mediumtext", "tinytext", "long"),
    TINYINT("tinyint", "int1", "byteint"),
    SMALLINT("smallint", "int2", "smallint"),
    INTEGER("integer", "int", "int4", "serial", "int identity"),
    BIGINT("bigint", "int8", "bigserial"),
    FLOAT("float", "float4", "real"),
    DOUBLE("double", "double precision"),
    DECIMAL("decimal", "dec", "numeric", "number"),
    BOOLEAN("boolean", "bool", "bit"),
    DATE("date"),
    TIME("time"),
    DATETIME("datetime", "timestamp", "datetime2", "smalldatetime"),
    TIMESTAMP("timestamp without time zone", "timestamp with time zone"),
    YEAR("year"),
    BINARY("binary"),
    VARBINARY("varbinary", "raw"),
    BLOB("blob", "image", "bytea", "long raw", "bfile"),
    CURSOR("cursor"),
    ROWID("rowid", "uid"),
    UUID("uuid", "uniqueidentifier"),
    JSON("json", "jsonb"),
    XML("xml"),
    ARRAY("array", "varray"),
    OBJECT("object", "struct"),
    OTHER("other", "java_object");

    private final String[] aliases;

    SqlDataType(String... aliases) {
        this.aliases = aliases;
    }

    /**
     * 根据类型名称查找对应的 SqlDataType 枚举
     * <p>
     * 使用包含匹配，支持带参数的数据库类型名称，如 varchar(255)、number(10,2) 等。
     * </p>
     *
     * @param typeName 数据库返回的类型名称
     * @return 对应的 SqlDataType 枚举，如果找不到返回 OTHER
     */
    public static SqlDataType fromTypeName(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return OTHER;
        }
        String upper = typeName.toUpperCase(java.util.Locale.ROOT).trim();
        
        for (SqlDataType type : values()) {
            for (String alias : type.aliases) {
                if (upper.equals(alias.toUpperCase(java.util.Locale.ROOT)) || upper.startsWith(alias.toUpperCase(java.util.Locale.ROOT) + "(")) {
                    return type;
                }
            }
        }
        return OTHER;
    }

    /**
     * 判断是否为字符串类型
     */
    public boolean isStringType() {
        return this == VARCHAR || this == CHAR || this == TEXT;
    }

    /**
     * 判断是否为数值类型
     */
    public boolean isNumberType() {
        return this == TINYINT || this == SMALLINT || this == INTEGER 
                || this == BIGINT || this == FLOAT || this == DOUBLE 
                || this == DECIMAL;
    }

    /**
     * 判断是否为整数类型
     */
    public boolean isIntegerType() {
        return this == TINYINT || this == SMALLINT || this == INTEGER || this == BIGINT;
    }

    /**
     * 判断是否为浮点类型
     */
    public boolean isFloatType() {
        return this == FLOAT || this == DOUBLE || this == DECIMAL;
    }

    /**
     * 判断是否为日期时间类型
     */
    public boolean isDateTimeType() {
        return this == DATE || this == TIME || this == DATETIME || this == TIMESTAMP || this == YEAR;
    }

    /**
     * 判断是否为二进制类型
     */
    public boolean isBinaryType() {
        return this == BINARY || this == VARBINARY || this == BLOB;
    }

    /**
     * 获取该类型的主名称
     */
    public String getPrimaryName() {
        return name().toLowerCase();
    }

    /**
     * 获取所有别名
     */
    public String[] getAliases() {
        return aliases;
    }
}