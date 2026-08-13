package io.github.liuwww.db.config.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import io.github.liuwww.db.config.TableStructureContainer;
import io.github.liuwww.db.config.TableStructureLoader;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.sql.Column;
import io.github.liuwww.db.sql.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceTableStructureLoader implements TableStructureLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceTableStructureLoader.class);

    private List<DataSource> dataSourceList = new ArrayList<>();

    public DataSourceTableStructureLoader() {
    }

    public void addDataSource(DataSource dataSource) {
        if (!dataSourceList.contains(dataSource)) {
            dataSourceList.add(dataSource);
        }
    }

    @Override
    public TableStructureContainer loadStructureContainer() {
        TableStructureContainer container = new DataSourceContextTableStructureContainer();
        for (DataSource ds : dataSourceList) {
            loadFromDataSource(ds, container);
        }
        return container;
    }

    private void loadFromDataSource(DataSource dataSource, TableStructureContainer container) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            String schema = conn.getSchema();

            try (ResultSet tables = metaData.getTables(catalog, schema, "%", new String[] { "TABLE", "VIEW" })) {
                while (tables.next()) {
                    String originalTableName = tables.getString("TABLE_NAME");
                    String tableName = originalTableName != null ? originalTableName.toLowerCase() : null;
                    String tableType = tables.getString("TABLE_TYPE");

                    if (tableName != null) {
                        TableMetaData tableMetaData = new TableMetaData();
                        setTableMetaDataProperties(tableMetaData, tableName, tableType, dataSource, metaData);

                        try (ResultSet columns = metaData.getColumns(catalog, schema, originalTableName, "%")) {
                            while (columns.next()) {
                                Column column = new Column();
                                column.setColumnName(columns.getString("COLUMN_NAME"));
                                column.setDataType(columns.getInt("DATA_TYPE"));
                                column.setDataTypeName(columns.getString("TYPE_NAME"));
                                column.setColumnSize(columns.getInt("COLUMN_SIZE"));
                                column.setNullable(columns.getInt("NULLABLE") == 1);
                                column.setRemarks(columns.getString("REMARKS"));

                                tableMetaData.getColumnList().add(column);
                                tableMetaData.putNameMap(column.getName(), column);
                                tableMetaData.putColumnMap(column.getColumnName(), column);

                                try (ResultSet primaryKeys = metaData.getPrimaryKeys(catalog, schema,
                                        originalTableName)) {
                                    while (primaryKeys.next()) {
                                        String pkColumnName = primaryKeys.getString("COLUMN_NAME");
                                        if (pkColumnName != null
                                                && pkColumnName.equalsIgnoreCase(column.getColumnName())) {
                                            setTableIdColumn(tableMetaData, column, column.getColumnName());
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        container.addTableMetaData(tableName, tableMetaData);

                        if (logger.isDebugEnabled()) {
                            logger.debug("加载表结构: {}", tableName);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("从数据源加载表结构失败", e);
            throw new RuntimeException("从数据源加载表结构失败", e);
        }
    }

    private void setTableMetaDataProperties(TableMetaData tableMetaData, String tableName, String tableType,
            DataSource dataSource, DatabaseMetaData metaData) throws SQLException {
        tableMetaData.setTableName(tableName);
        tableMetaData.setTableType(tableType);
        tableMetaData.setDataSource(dataSource);

        String productName = metaData.getDatabaseProductName().toLowerCase();
        DbType dbType;
        if (productName.contains("mysql")) {
            dbType = DbType.MYSQL;
        } else if (productName.contains("oracle")) {
            dbType = DbType.ORACLE;
        } else if (productName.contains("postgresql")) {
            dbType = DbType.PostgreSQL;
        } else if (productName.contains("microsoft") || productName.contains("sql server")) {
            dbType = DbType.SQLSERVER;
        } else {
            dbType = DbType.OTHER;
        }

        tableMetaData.setDbType(dbType);
    }

    private void setTableIdColumn(TableMetaData tableMetaData, Column column, String columnName) {
        tableMetaData.setIdColumn(column);
        tableMetaData.setIdColumnName(columnName);
    }
}