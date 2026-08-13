package io.github.liuwww.db.sql;

import java.sql.Types;

import io.github.liuwww.common.util.DbNameConverter;

/**
 * 数据库列元数据
 * <p>
 * 表示数据库表中的一个列，包含列名、数据类型、大小、是否可空等信息。
 * 用于 SQL 构建、结果映射等操作。
 * </p>
 * 
 * @author liuwww
 */
public class Column {

    /** 数据库列名 */
    private String columnName;

    /** SQL 数据类型枚举 */
    private SqlDataType sqlDataType;

    /** JDBC 数据类型常量（java.sql.Types） */
    private int dataType;

    /** 是否可空 */
    private boolean nullable;

    /** 列注释 */
    private String remarks;

    /** 列大小/长度 */
    private int columnSize;

    /**
     * 获取 Java 字段名（驼峰命名）
     * <p>
     * 通过 columnName 转换得到，避免存储重复数据。
     * </p>
     *
     * @return Java 字段名
     */
    public String getName() {
        return columnName != null ? DbNameConverter.convert(columnName) : null;
    }

    /**
     * 获取数据库列名
     *
     * @return 数据库列名
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 设置数据库列名
     *
     * @param columnName 数据库列名
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * 获取是否可空
     *
     * @return 是否可空
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * 设置是否可空
     *
     * @param nullable 是否可空
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * 获取列注释
     *
     * @return 列注释
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * 设置列注释
     *
     * @param remarks 列注释
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 获取列大小/长度
     *
     * @return 列大小
     */
    public int getColumnSize() {
        return columnSize;
    }

    /**
     * 设置列大小/长度
     *
     * @param columnSize 列大小
     */
    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    /**
     * 设置数据类型名称
     * <p>
     * 同时会自动设置对应的 SqlDataType 枚举类型。
     * 此方法优先于 setDataType()，设置后 setDataType() 的推断值不会再覆盖。
     * </p>
     *
     * @param dataTypeName 数据类型名称
     */
    public void setDataTypeName(String dataTypeName) {
        this.sqlDataType = SqlDataType.fromTypeName(dataTypeName);
    }

    /**
     * 获取 SQL 数据类型枚举
     *
     * @return SQL 数据类型枚举
     */
    public SqlDataType getSqlDataType() {
        return sqlDataType;
    }

    /**
     * 获取 JDBC 数据类型常量
     *
     * @return JDBC 数据类型常量（java.sql.Types）
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * 设置 JDBC 数据类型常量
     * <p>
     * 如果 sqlDataType 尚未设置，会自动推断并设置对应的枚举类型。
     * 此方法优先级低于 setDataTypeName()，即如果已通过 setDataTypeName() 设置了类型，则不会覆盖。
     * </p>
     *
     * @param dataType JDBC 数据类型常量
     */
    public void setDataType(int dataType) {
        this.dataType = dataType;
        if (this.sqlDataType == null) {
            this.sqlDataType = inferSqlDataType(dataType);
        }
    }

    /**
     * 根据 JDBC 类型码推断 SqlDataType 枚举
     */
    private SqlDataType inferSqlDataType(int jdbcType) {
        switch (jdbcType) {
            case Types.BIT:
            case Types.TINYINT:
                return SqlDataType.TINYINT;
            case Types.SMALLINT:
                return SqlDataType.SMALLINT;
            case Types.INTEGER:
                return SqlDataType.INTEGER;
            case Types.BIGINT:
                return SqlDataType.BIGINT;
            case Types.FLOAT:
            case Types.REAL:
                return SqlDataType.FLOAT;
            case Types.DOUBLE:
                return SqlDataType.DOUBLE;
            case Types.DECIMAL:
            case Types.NUMERIC:
                return SqlDataType.DECIMAL;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return SqlDataType.VARCHAR;
            case Types.DATE:
                return SqlDataType.DATE;
            case Types.TIME:
                return SqlDataType.TIME;
            case Types.TIMESTAMP:
                return SqlDataType.DATETIME;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return SqlDataType.BINARY;
            case Types.BLOB:
                return SqlDataType.BLOB;
            case Types.CLOB:
                return SqlDataType.TEXT;
            case Types.BOOLEAN:
                return SqlDataType.BOOLEAN;
            default:
                return SqlDataType.OTHER;
        }
    }

    /**
     * 判断是否为数值类型列
     * <p>
     * 支持的数值类型包括：BIGINT、DECIMAL、DOUBLE、FLOAT、INTEGER、NUMERIC。
     * </p>
     *
     * @return 如果是数值类型返回 true，否则返回 false
     */
    public boolean isNumberColumn() {
        if (sqlDataType != null) {
            return sqlDataType.isNumberType();
        }
        return false;
    }

    @Override
    public String toString() {
        return "Column{columnName='" + columnName + "', sqlDataType=" + sqlDataType
                + ", dataType=" + dataType + ", columnSize=" + columnSize + ", nullable=" + nullable + "}";
    }
}