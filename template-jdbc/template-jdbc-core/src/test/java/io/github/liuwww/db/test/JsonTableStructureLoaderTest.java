package io.github.liuwww.db.test;

import java.io.File;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import io.github.liuwww.db.config.TableStructureContainer;
import io.github.liuwww.db.config.impl.JsonTableStructureLoader;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.sql.Column;
import io.github.liuwww.db.sql.DbType;

import static org.junit.Assert.*;

/**
 * {@link JsonTableStructureLoader} 单元测试。
 * <p>
 * 覆盖：单表/多表加载、单主键/联合主键、多输入源、DbType 解析、
 * 列默认值、JSON 解析器边界（转义、嵌套、空结构）、异常处理。
 * </p>
 *
 * @author liuwww
 */
public class JsonTableStructureLoaderTest {

    private static final String SINGLE_TABLE_JSON =
            "{\"tables\":[" +
            "{\"tableName\":\"t_user\",\"dbType\":\"mysql\",\"tableType\":\"TABLE\"," +
            "\"comment\":\"用户表\",\"idColumnName\":\"id\"," +
            "\"columns\":[" +
            "{\"columnName\":\"id\",\"dataTypeName\":\"BIGINT\",\"columnSize\":20,\"nullable\":false,\"remarks\":\"主键\"}," +
            "{\"columnName\":\"name\",\"dataTypeName\":\"VARCHAR\",\"columnSize\":100,\"nullable\":false}" +
            "]}" +
            "]}";

    private static final String UNION_KEY_JSON =
            "{\"tables\":[" +
            "{\"tableName\":\"t_union\",\"dbType\":\"oracle\",\"idColumnName\":\"id,tenant_id\"," +
            "\"columns\":[" +
            "{\"columnName\":\"id\",\"dataTypeName\":\"BIGINT\",\"nullable\":false}," +
            "{\"columnName\":\"tenant_id\",\"dataTypeName\":\"VARCHAR\",\"columnSize\":50,\"nullable\":false}" +
            "]}" +
            "]}";

    // ===== 基本加载 =====

    @Test
    public void testLoadSingleTable() {
        JsonTableStructureLoader loader = new JsonTableStructureLoader(SINGLE_TABLE_JSON);
        TableStructureContainer container = loader.loadStructureContainer();

        assertEquals(1, container.getTableNames().size());
        TableMetaData tmd = container.getTableMetaData("t_user");
        assertNotNull(tmd);
        assertEquals("t_user", tmd.getTableName());
        assertEquals(DbType.MYSQL, tmd.getDbType());
        assertEquals("TABLE", tmd.getTableType());
        assertEquals("用户表", tmd.getComment());
        assertEquals(2, tmd.getColumnList().size());
    }

    @Test
    public void testLoadColumnAttributes() {
        JsonTableStructureLoader loader = new JsonTableStructureLoader(SINGLE_TABLE_JSON);
        TableStructureContainer container = loader.loadStructureContainer();
        TableMetaData tmd = container.getTableMetaData("t_user");

        Column idCol = tmd.getColumnByColumn("id");
        assertNotNull(idCol);
        assertEquals("id", idCol.getColumnName());
        assertEquals(20, idCol.getColumnSize());
        assertFalse(idCol.isNullable());
        assertEquals("主键", idCol.getRemarks());

        // 第二列没有 remarks
        Column nameCol = tmd.getColumnByColumn("name");
        assertNotNull(nameCol);
        assertEquals(100, nameCol.getColumnSize());
        assertFalse(nameCol.isNullable());
        assertNull(nameCol.getRemarks());
    }

    @Test
    public void testSinglePrimaryKey() {
        JsonTableStructureLoader loader = new JsonTableStructureLoader(SINGLE_TABLE_JSON);
        TableStructureContainer container = loader.loadStructureContainer();
        TableMetaData tmd = container.getTableMetaData("t_user");

        assertNotNull(tmd.getIdColumn());
        assertEquals("id", tmd.getIdColumn().getColumnName());
        assertEquals(1, tmd.getIdColumns().length);
        assertEquals("id", tmd.getIdNames()[0]);
    }

    // ===== 联合主键 =====

    @Test
    public void testUnionPrimaryKey() {
        JsonTableStructureLoader loader = new JsonTableStructureLoader(UNION_KEY_JSON);
        TableStructureContainer container = loader.loadStructureContainer();
        TableMetaData tmd = container.getTableMetaData("t_union");

        assertNotNull(tmd.getIdColumns());
        assertEquals(2, tmd.getIdColumns().length);
        assertEquals("id", tmd.getIdColumns()[0].getColumnName());
        assertEquals("tenant_id", tmd.getIdColumns()[1].getColumnName());
        assertEquals(2, tmd.getIdNames().length);
        assertEquals("id", tmd.getIdNames()[0]);
        assertEquals("tenant_id", tmd.getIdNames()[1]);
    }

    // ===== 多表加载（classpath 资源）=====

    @Test
    public void testLoadFromClasspath() {
        JsonTableStructureLoader loader = new JsonTableStructureLoader();
        loader.setClasspathResource("test-table-structure.json");
        TableStructureContainer container = loader.loadStructureContainer();

        assertEquals(2, container.getTableNames().size());

        // 第一表：单主键
        TableMetaData userTmd = container.getTableMetaData("t_test_user");
        assertNotNull(userTmd);
        assertEquals(DbType.MYSQL, userTmd.getDbType());
        assertEquals("测试用户表", userTmd.getComment());
        assertEquals(3, userTmd.getColumnList().size());
        assertNotNull(userTmd.getIdColumn());
        assertEquals("id", userTmd.getIdColumn().getColumnName());

        // 第二表：联合主键
        TableMetaData orderTmd = container.getTableMetaData("t_test_order");
        assertNotNull(orderTmd);
        assertEquals(DbType.PostgreSQL, orderTmd.getDbType());
        assertEquals(2, orderTmd.getIdColumns().length);
        assertEquals("order_id", orderTmd.getIdColumns()[0].getColumnName());
        assertEquals("tenant_id", orderTmd.getIdColumns()[1].getColumnName());
    }

    // ===== DbType 解析 =====

    @Test
    public void testDbTypeParsing() {
        String json = "{\"tables\":[" +
                "{\"tableName\":\"t1\",\"dbType\":\"MYSQL\",\"columns\":[]}," +
                "{\"tableName\":\"t2\",\"dbType\":\"oracle\",\"columns\":[]}," +
                "{\"tableName\":\"t3\",\"dbType\":\"PostgreSQL\",\"columns\":[]}," +
                "{\"tableName\":\"t4\",\"dbType\":\"sqlserver\",\"columns\":[]}," +
                "{\"tableName\":\"t5\",\"dbType\":\"unknown_type\",\"columns\":[]}" +
                "]}";
        JsonTableStructureLoader loader = new JsonTableStructureLoader(json);
        TableStructureContainer container = loader.loadStructureContainer();

        assertEquals(DbType.MYSQL, container.getTableMetaData("t1").getDbType());
        assertEquals(DbType.ORACLE, container.getTableMetaData("t2").getDbType());
        assertEquals(DbType.PostgreSQL, container.getTableMetaData("t3").getDbType());
        assertEquals(DbType.SQLSERVER, container.getTableMetaData("t4").getDbType());
        assertEquals(DbType.OTHER, container.getTableMetaData("t5").getDbType());
    }

    // ===== 列默认值 =====

    @Test
    public void testColumnDefaults() {
        String json = "{\"tables\":[" +
                "{\"tableName\":\"t\",\"columns\":[" +
                "{\"columnName\":\"c1\",\"dataTypeName\":\"INT\"}" +
                "]}]}";
        JsonTableStructureLoader loader = new JsonTableStructureLoader(json);
        TableStructureContainer container = loader.loadStructureContainer();
        Column col = container.getTableMetaData("t").getColumnByColumn("c1");

        // nullable 未设置时默认 true
        assertTrue(col.isNullable());
        // columnSize 未设置时默认 0
        assertEquals(0, col.getColumnSize());
        // remarks 未设置时为 null
        assertNull(col.getRemarks());
    }

    // ===== 空内容处理 =====

    @Test
    public void testEmptyJson() {
        JsonTableStructureLoader loader = new JsonTableStructureLoader("");
        TableStructureContainer container = loader.loadStructureContainer();

        assertNotNull(container);
        assertEquals(0, container.getTableNames().size());
    }

    @Test
    public void testNoJsonConfigured() {
        JsonTableStructureLoader loader = new JsonTableStructureLoader();
        TableStructureContainer container = loader.loadStructureContainer();

        assertNotNull(container);
        assertEquals(0, container.getTableNames().size());
    }

    @Test
    public void testEmptyTablesArray() {
        String json = "{\"tables\":[]}";
        JsonTableStructureLoader loader = new JsonTableStructureLoader(json);
        TableStructureContainer container = loader.loadStructureContainer();

        assertEquals(0, container.getTableNames().size());
    }

    // ===== 异常处理 =====

    @Test(expected = RuntimeException.class)
    public void testInvalidJson() {
        String json = "{invalid json}";
        new JsonTableStructureLoader(json).loadStructureContainer();
    }

    @Test(expected = RuntimeException.class)
    public void testMissingTablesArray() {
        String json = "{\"foo\":1}";
        new JsonTableStructureLoader(json).loadStructureContainer();
    }

    @Test(expected = RuntimeException.class)
    public void testMissingTableName() {
        String json = "{\"tables\":[{\"dbType\":\"mysql\",\"columns\":[]}]}";
        new JsonTableStructureLoader(json).loadStructureContainer();
    }

    // ===== JSON 解析器边界 =====

    @Test
    public void testChineseInJsonString() {
        String json = "{\"tables\":[{\"tableName\":\"t_中文表\",\"comment\":\"这是一个中文注释\",\"columns\":[]}]}";
        JsonTableStructureLoader loader = new JsonTableStructureLoader(json);
        TableStructureContainer container = loader.loadStructureContainer();

        TableMetaData tmd = container.getTableMetaData("t_中文表");
        assertNotNull(tmd);
        assertEquals("这是一个中文注释", tmd.getComment());
    }

    @Test
    public void testEscapedQuotesInRemarks() {
        String json = "{\"tables\":[{\"tableName\":\"t\",\"columns\":[" +
                "{\"columnName\":\"c\",\"remarks\":\"含\\\"引号\\\"的注释\"}" +
                "]}]}";
        JsonTableStructureLoader loader = new JsonTableStructureLoader(json);
        TableStructureContainer container = loader.loadStructureContainer();
        Column col = container.getTableMetaData("t").getColumnByColumn("c");

        assertEquals("含\"引号\"的注释", col.getRemarks());
    }

    @Test
    public void testNestedEmptyStructures() {
        String json = "{\"tables\":[{\"tableName\":\"t\",\"columns\":[]}]}";
        JsonTableStructureLoader loader = new JsonTableStructureLoader(json);
        TableStructureContainer container = loader.loadStructureContainer();

        TableMetaData tmd = container.getTableMetaData("t");
        assertNotNull(tmd);
        assertEquals(0, tmd.getColumnList().size());
        assertNull(tmd.getIdColumn());
    }

    @Test
    public void testNumberParsing() {
        String json = "{\"tables\":[{\"tableName\":\"t\",\"columns\":[" +
                "{\"columnName\":\"c1\",\"columnSize\":255,\"dataTypeName\":\"VARCHAR\"}," +
                "{\"columnName\":\"c2\",\"columnSize\":0}" +
                "]}]}";
        JsonTableStructureLoader loader = new JsonTableStructureLoader(json);
        TableStructureContainer container = loader.loadStructureContainer();
        TableMetaData tmd = container.getTableMetaData("t");

        assertEquals(255, tmd.getColumnByColumn("c1").getColumnSize());
        assertEquals(0, tmd.getColumnByColumn("c2").getColumnSize());
    }

    // ===== 表名大小写处理 =====

    @Test
    public void testTableNameCaseInsensitiveLookup() {
        String json = "{\"tables\":[{\"tableName\":\"T_User\",\"columns\":[]}]}";
        JsonTableStructureLoader loader = new JsonTableStructureLoader(json);
        TableStructureContainer container = loader.loadStructureContainer();

        // container 内部用 toLowerCase 存储
        assertNotNull(container.getTableMetaData("t_user"));
        assertNotNull(container.getTableMetaData("T_USER"));
    }

    // ===== 多数据源模式 =====

    @Test
    public void testMultiDataSourceLoading() throws Exception {
        String jsonA = "{\"tables\":[{\"tableName\":\"t_a\",\"columns\":[]}]}";
        String jsonB = "{\"tables\":[{\"tableName\":\"t_b\",\"columns\":[]}]}";

        File fileA = File.createTempFile("dsA", ".json");
        File fileB = File.createTempFile("dsB", ".json");
        java.nio.file.Files.write(fileA.toPath(), jsonA.getBytes());
        java.nio.file.Files.write(fileB.toPath(), jsonB.getBytes());

        DataSource dsA = createMockDataSource();
        DataSource dsB = createMockDataSource();

        Map<String, String> sources = new java.util.LinkedHashMap<>();
        sources.put("dsA", fileA.getAbsolutePath());
        sources.put("dsB", fileB.getAbsolutePath());

        Map<String, DataSource> bindings = new java.util.LinkedHashMap<>();
        bindings.put("dsA", dsA);
        bindings.put("dsB", dsB);

        JsonTableStructureLoader loader = new JsonTableStructureLoader();
        loader.setDataSourceJsonSources(sources);
        loader.setDataSourceBindings(bindings);

        TableStructureContainer container = loader.loadStructureContainer();

        assertEquals(2, container.getTableNames().size());
        assertSame(dsA, container.getTableMetaData("t_a").getDataSource());
        assertSame(dsB, container.getTableMetaData("t_b").getDataSource());

        fileA.delete();
        fileB.delete();
    }

    @Test
    public void testClasspathPrefixInMultiDataSource() {
        Map<String, String> sources = new java.util.LinkedHashMap<>();
        sources.put("dsA", "classpath:test-table-structure.json");

        DataSource dsA = createMockDataSource();
        Map<String, DataSource> bindings = new java.util.LinkedHashMap<>();
        bindings.put("dsA", dsA);

        JsonTableStructureLoader loader = new JsonTableStructureLoader();
        loader.setDataSourceJsonSources(sources);
        loader.setDataSourceBindings(bindings);

        TableStructureContainer container = loader.loadStructureContainer();
        assertTrue(container.getTableNames().size() > 0);
        for (TableMetaData tmd : container.getAllTableMetaData().values()) {
            assertSame(dsA, tmd.getDataSource());
        }
    }

    @Test
    public void testMultiDataSourceEmptyJson() {
        Map<String, String> sources = new java.util.LinkedHashMap<>();
        sources.put("dsA", "classpath:nonexistent-file.json");

        JsonTableStructureLoader loader = new JsonTableStructureLoader();
        loader.setDataSourceJsonSources(sources);
        loader.setDataSourceBindings(new java.util.LinkedHashMap<>());

        // 不存在的 classpath 资源应抛异常
        try {
            loader.loadStructureContainer();
            fail("应抛出 RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Classpath"));
        }
    }

    // ===== 导出功能 =====

    @Test
    public void testExportToFile() throws Exception {
        JsonTableStructureLoader loader = new JsonTableStructureLoader(SINGLE_TABLE_JSON);
        TableStructureContainer container = loader.loadStructureContainer();

        File tempFile = File.createTempFile("export", ".json");
        JsonTableStructureLoader.exportToFile(
                new java.util.ArrayList<>(container.getAllTableMetaData().values()),
                tempFile.getAbsolutePath());

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);

        JsonTableStructureLoader loader2 = new JsonTableStructureLoader();
        loader2.setFilePath(tempFile.getAbsolutePath());
        TableStructureContainer container2 = loader2.loadStructureContainer();

        assertEquals(1, container2.getTableNames().size());
        TableMetaData exported = container2.getTableMetaData("t_user");
        assertNotNull(exported);
        assertEquals("t_user", exported.getTableName());

        tempFile.delete();
    }

    @Test
    public void testExportImportRoundTrip() throws Exception {
        JsonTableStructureLoader loader1 = new JsonTableStructureLoader(SINGLE_TABLE_JSON);
        TableStructureContainer container1 = loader1.loadStructureContainer();

        File tempFile = File.createTempFile("roundtrip", ".json");
        JsonTableStructureLoader.exportToFile(
                new java.util.ArrayList<>(container1.getAllTableMetaData().values()),
                tempFile.getAbsolutePath());

        JsonTableStructureLoader loader2 = new JsonTableStructureLoader();
        loader2.setFilePath(tempFile.getAbsolutePath());
        TableStructureContainer container2 = loader2.loadStructureContainer();

        TableMetaData orig = container1.getTableMetaData("t_user");
        TableMetaData reloaded = container2.getTableMetaData("t_user");

        assertEquals(orig.getTableName(), reloaded.getTableName());
        assertEquals(orig.getDbType(), reloaded.getDbType());
        assertEquals(orig.getComment(), reloaded.getComment());
        assertEquals(orig.getColumnList().size(), reloaded.getColumnList().size());
        assertNotNull(reloaded.getIdColumn());
        assertEquals(orig.getIdColumn().getColumnName(), reloaded.getIdColumn().getColumnName());

        Column origCol = orig.getColumnByColumn("id");
        Column reloadedCol = reloaded.getColumnByColumn("id");
        assertEquals(origCol.getSqlDataType(), reloadedCol.getSqlDataType());
        assertEquals(origCol.getColumnSize(), reloadedCol.getColumnSize());
        assertEquals(origCol.isNullable(), reloadedCol.isNullable());
        assertEquals(origCol.getRemarks(), reloadedCol.getRemarks());

        tempFile.delete();
    }

    @Test
    public void testExportByDataSource() throws Exception {
        DataSource dsA = createMockDataSource();
        DataSource dsB = createMockDataSource();

        TableMetaData tmd1 = new TableMetaData();
        tmd1.setTableName("t_a1");
        tmd1.setDataSource(dsA);

        TableMetaData tmd2 = new TableMetaData();
        tmd2.setTableName("t_b1");
        tmd2.setDataSource(dsB);

        Map<String, java.util.List<TableMetaData>> tablesByBeanName = new java.util.LinkedHashMap<>();
        tablesByBeanName.put("dsA", java.util.Collections.singletonList(tmd1));
        tablesByBeanName.put("dsB", java.util.Collections.singletonList(tmd2));

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "export-test-" + System.nanoTime());
        JsonTableStructureLoader.exportByDataSource(tablesByBeanName, tempDir.getAbsolutePath());

        File fileA = new File(tempDir, "dsA.json");
        File fileB = new File(tempDir, "dsB.json");
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());

        JsonTableStructureLoader loaderA = new JsonTableStructureLoader();
        loaderA.setFilePath(fileA.getAbsolutePath());
        TableStructureContainer containerA = loaderA.loadStructureContainer();
        assertNotNull(containerA.getTableMetaData("t_a1"));

        JsonTableStructureLoader loaderB = new JsonTableStructureLoader();
        loaderB.setFilePath(fileB.getAbsolutePath());
        TableStructureContainer containerB = loaderB.loadStructureContainer();
        assertNotNull(containerB.getTableMetaData("t_b1"));

        // 清理
        fileA.delete();
        fileB.delete();
        tempDir.delete();
    }

    @Test
    public void testExportUnionKeyRoundTrip() throws Exception {
        JsonTableStructureLoader loader1 = new JsonTableStructureLoader(UNION_KEY_JSON);
        TableStructureContainer container1 = loader1.loadStructureContainer();

        File tempFile = File.createTempFile("union-key", ".json");
        JsonTableStructureLoader.exportToFile(
                new java.util.ArrayList<>(container1.getAllTableMetaData().values()),
                tempFile.getAbsolutePath());

        JsonTableStructureLoader loader2 = new JsonTableStructureLoader();
        loader2.setFilePath(tempFile.getAbsolutePath());
        TableStructureContainer container2 = loader2.loadStructureContainer();

        TableMetaData orig = container1.getTableMetaData("t_union");
        TableMetaData reloaded = container2.getTableMetaData("t_union");

        // 验证联合主键导出/导入一致
        assertNotNull(orig.getIdColumns());
        assertEquals(2, orig.getIdColumns().length);
        assertNotNull(reloaded.getIdColumns());
        assertEquals(2, reloaded.getIdColumns().length);
        assertEquals(orig.getIdColumns()[0].getColumnName(), reloaded.getIdColumns()[0].getColumnName());
        assertEquals(orig.getIdColumns()[1].getColumnName(), reloaded.getIdColumns()[1].getColumnName());

        tempFile.delete();
    }

    // ===== 辅助方法 =====

    private static DataSource createMockDataSource() {
        return (DataSource) java.lang.reflect.Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class[]{DataSource.class},
                (proxy, method, args) -> null
        );
    }
}
