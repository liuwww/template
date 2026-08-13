package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.sql.Column;
import io.github.liuwww.db.sql.DbType;

/**
 * TableMetaData 测试类
 */
public class TableMetaDataTest {

    @Test
    public void testTableMetaDataSettersAndGetters() {
        TableMetaData tmd = new TableMetaData();
        tmd.setTableName("test_user");
        tmd.setDbType(DbType.MYSQL);
        tmd.setTableType("TABLE");
        tmd.setComment("用户表");
        tmd.setIdColumnName("id");

        assertEquals("test_user", tmd.getTableName());
        assertEquals(DbType.MYSQL, tmd.getDbType());
        assertEquals("TABLE", tmd.getTableType());
        assertEquals("用户表", tmd.getComment());
        assertEquals("id", tmd.getIdColumnName());
    }

    @Test
    public void testAddColumn() {
        TableMetaData tmd = new TableMetaData();

        Column column = new Column();
        column.setColumnName("id");
        column.setDataType(java.sql.Types.VARCHAR);
        column.setColumnSize(32);

        tmd.putNameMap("id", column);
        tmd.putColumnMap("id", column);

        Column retrieved = tmd.getColumn("id");
        assertNotNull(retrieved);
        assertEquals("id", retrieved.getColumnName());
    }

    @Test
    public void testGetColumnByField() {
        TableMetaData tmd = new TableMetaData();

        Column column = new Column();
        column.setColumnName("user_id");

        tmd.putNameMap("userId", column);
        tmd.putColumnMap("user_id", column);

        Column byField = tmd.getColumnByField("userId");
        assertNotNull(byField);
        assertEquals("userId", byField.getName());
    }

    @Test
    public void testGetColumnByColumn() {
        TableMetaData tmd = new TableMetaData();

        Column column = new Column();
        column.setColumnName("user_id");

        tmd.putNameMap("userId", column);
        tmd.putColumnMap("user_id", column);

        Column byColumn = tmd.getColumnByColumn("user_id");
        assertNotNull(byColumn);
        assertEquals("user_id", byColumn.getColumnName());
    }

    @Test
    public void testGetColumnCaseInsensitive() {
        TableMetaData tmd = new TableMetaData();

        Column column = new Column();
        column.setColumnName("user_id");

        tmd.putColumnMap("user_id", column);

        Column retrieved = tmd.getColumn("USER_ID");
        assertNotNull(retrieved);
    }

    @Test
    public void testClear() {
        TableMetaData tmd = new TableMetaData();

        Column column = new Column();
        column.setColumnName("id");

        tmd.putNameMap("id", column);
        tmd.putColumnMap("id", column);
        tmd.getColumnList().add(column);

        assertEquals(1, tmd.getColumnList().size());

        tmd.clear();

        assertEquals(0, tmd.getColumnList().size());
    }

    @Test
    public void testIsUnionKey() {
        TableMetaData tmd = new TableMetaData();
        tmd.setIdColumnName("id");

        assertFalse(tmd.isUnionKey());

        TableMetaData tmd2 = new TableMetaData();
        tmd2.setIdColumnName("id1,id2");

        assertTrue(tmd2.isUnionKey());
    }

    @Test
    public void testGetIdColumn() {
        TableMetaData tmd = new TableMetaData();

        Column column = new Column();
        column.setColumnName("id");

        tmd.setIdColumn(column);

        Column idColumn = tmd.getIdColumn();
        assertNotNull(idColumn);
        assertEquals("id", idColumn.getColumnName());
    }

    @Test
    public void testGetIdColumns() {
        TableMetaData tmd = new TableMetaData();
        tmd.setIdColumnName("id1,id2");

        String[] idNames = tmd.getIdNames();
        assertNotNull(idNames);
        assertEquals(2, idNames.length);
        assertEquals("id1", idNames[0]);
        assertEquals("id2", idNames[1]);
    }

    @Test
    public void testToString() {
        TableMetaData tmd = new TableMetaData();
        tmd.setTableName("test_user");
        tmd.setDbType(DbType.MYSQL);

        String str = tmd.toString();
        assertNotNull(str);
        assertTrue(str.contains("test_user"));
        assertTrue(str.contains("mysql"));
    }

    @Test
    public void testGetDbCatalog() {
        assertEquals(null, TableMetaData.getDbCatalog("root", "mysql"));
        assertEquals("ROOT", TableMetaData.getDbCatalog("root", "oracle"));
        assertEquals("public", TableMetaData.getDbCatalog("root", "postgresql"));
    }
}
