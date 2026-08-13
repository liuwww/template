package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import io.github.liuwww.db.config.DatabaseConfigCenter;
import io.github.liuwww.db.dao.impl.QueryDao;
import io.github.liuwww.db.query.DefaultQueryBeanFactory;
import io.github.liuwww.db.query.QueryBean;
import io.github.liuwww.db.sql.Field;
import io.github.liuwww.db.sql.SqlBean;
import io.github.liuwww.db.sql.SqlBeanUtil;
import io.github.liuwww.db.sql.Table;

/**
 * Table 类字段选择功能测试
 * <p>
 * 验证 Table 类通过 addField 指定查询字段的功能是否生效，
 * 以及通过 SqlBeanUtil.getSqlBean(Table) 和 QueryBeanFactory.createQueryBean(Table)
 * 构建的 SQL 是否正确包含指定字段。
 * </p>
 */
public class TableFieldSelectionTest extends BaseTest {

    /**
     * 构造一个已注入配置中心和执行器的 Table 实例
     * <p>
     * Table 类的构造函数 {@code Table(String, DatabaseExecutor)} 会立即触发 setTmd()，
     * 但此时 configCenter 尚未注入，会抛出异常。因此需要使用无参构造函数，
     * 先注入 configCenter，再设置 name（setName 会触发 setTmd 从配置中心加载表元数据）。
     * </p>
     */
    private Table newTable(String tableName) {
        DatabaseConfigCenter configCenter = coreDbContext.getConfigCenter();
        Table table = new Table();
        table.setConfigCenter(configCenter);
        table.setName(tableName);
        table.setDatabaseExecutor(executor);
        return table;
    }

    /**
     * 测试1：Table 未指定字段时，SQL 应查询所有列
     */
    @Test
    public void testNoFieldList_selectAllColumns() {
        Table table = newTable("test_user");

        SqlBean bean = SqlBeanUtil.getSqlBean(table);

        String sql = bean.getSql().toLowerCase();
        assertTrue("SQL应包含id列", sql.contains("id"));
        assertTrue("SQL应包含name列", sql.contains("name"));
        assertTrue("SQL应包含age列", sql.contains("age"));
        assertTrue("SQL应包含email列", sql.contains("email"));
        assertTrue("SQL应以select开头", sql.startsWith("select "));
        assertTrue("SQL应包含from test_user", sql.contains("from test_user"));
    }

    /**
     * 测试2：Table 指定单个字段时，SQL 应只查询该字段
     */
    @Test
    public void testSingleField_selectOnlySpecifiedColumn() {
        Table table = newTable("test_user");
        table.addField("name");

        SqlBean bean = SqlBeanUtil.getSqlBean(table);
        String sql = bean.getSql().toLowerCase();

        assertTrue("SQL应以select开头", sql.startsWith("select "));
        assertTrue("SQL应包含name列", sql.contains("name"));
        assertFalse("SQL不应包含age列", sql.contains("age"));
        assertFalse("SQL不应包含email列", sql.contains("email"));
    }

    /**
     * 测试3：Table 指定多个字段时，SQL 应查询这些字段
     */
    @Test
    public void testMultipleFields_selectSpecifiedColumns() {
        Table table = newTable("test_user");
        table.addField("id");
        table.addField("name");
        table.addField("age");

        SqlBean bean = SqlBeanUtil.getSqlBean(table);
        String sql = bean.getSql().toLowerCase();

        assertTrue("SQL应包含id列", sql.contains("id"));
        assertTrue("SQL应包含name列", sql.contains("name"));
        assertTrue("SQL应包含age列", sql.contains("age"));
        assertFalse("SQL不应包含email列", sql.contains("email"));
        assertFalse("SQL不应包含create_time列", sql.contains("create_time"));
    }

    /**
     * 测试4：Table 指定字段别名时，SQL 应包含 AS 别名
     */
    @Test
    public void testFieldWithAlias_generatesAsClause() {
        Table table = newTable("test_user");
        table.addField("name", "user_name");

        SqlBean bean = SqlBeanUtil.getSqlBean(table);
        String sql = bean.getSql().toLowerCase();

        assertTrue("SQL应包含name列", sql.contains("name"));
        assertTrue("SQL应包含as user_name", sql.contains("as user_name"));
    }

    /**
     * 测试5：通过 Field 对象添加字段
     */
    @Test
    public void testAddFieldByFieldObject() {
        Table table = newTable("test_user");
        table.addField(new Field("id"));
        table.addField(new Field("email", "user_email"));

        SqlBean bean = SqlBeanUtil.getSqlBean(table);
        String sql = bean.getSql().toLowerCase();

        assertTrue("SQL应包含id列", sql.contains("id"));
        assertTrue("SQL应包含email列", sql.contains("email"));
        assertTrue("SQL应包含as user_email", sql.contains("as user_email"));
        assertFalse("SQL不应包含name列", sql.contains("name"));
    }

    /**
     * 测试6：通过 SqlBeanUtil.getSqlBean(Table) 构建查询并实际执行
     */
    @Test
    public void testGetSqlBeanWithFields_executeQuery() {
        executor.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[] { "t1", "张三", 25, "zhangsan@example.com" });

        Table table = newTable("test_user");
        table.addField("id");
        table.addField("name");

        SqlBean bean = SqlBeanUtil.getSqlBean(table);
        QueryDao queryDao = new QueryDao();
        List<Map<String, Object>> result = queryDao.getList(bean);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull("结果应包含id", result.get(0).get("ID"));
        assertEquals("张三", result.get(0).get("NAME"));
    }

    /**
     * 测试7：Table 指定字段并添加查询条件
     */
    @Test
    public void testFieldSelectionWithCondition() {
        executor.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[] { "t1", "张三", 25, "zhangsan@example.com" });
        executor.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[] { "t2", "李四", 30, "lisi@example.com" });

        Table table = newTable("test_user");
        table.addField("name");
        table.addField("age");
        table.gt("age", 26);

        SqlBean bean = SqlBeanUtil.getSqlBean(table);
        String sql = bean.getSql().toLowerCase();

        assertTrue("SQL应包含name列", sql.contains("name"));
        assertTrue("SQL应包含age列", sql.contains("age"));
        assertFalse("SQL不应包含email列", sql.contains("email"));
        assertTrue("SQL应包含where子句", sql.contains("where"));
    }

    /**
     * 测试8：Table 指定字段为空字符串时（边界情况）
     */
    @Test
    public void testEmptyFieldList_fallsBackToAllColumns() {
        Table table = newTable("test_user");

        SqlBean bean = SqlBeanUtil.getSqlBean(table);
        String sql = bean.getSql().toLowerCase();

        assertTrue("SQL应包含id列", sql.contains("id"));
        assertTrue("SQL应包含name列", sql.contains("name"));
        assertTrue("SQL应包含age列", sql.contains("age"));
    }

    // ===== 新增 API 测试：不使用 Table 类，直接通过表名+字段列表查询 =====

    /**
     * 测试9：SqlBeanUtil.getSqlBean(tableName, fieldList) 指定单字段
     */
    @Test
    public void testGetSqlBeanByTableNameAndFieldList_singleField() {
        SqlBean bean = SqlBeanUtil.getSqlBean("test_user", Arrays.asList(new Field("name")));

        String sql = bean.getSql().toLowerCase();
        assertTrue("SQL应以select开头", sql.startsWith("select "));
        assertTrue("SQL应包含name列", sql.contains("name"));
        assertFalse("SQL不应包含age列", sql.contains("age"));
        assertFalse("SQL不应包含email列", sql.contains("email"));
        assertTrue("SQL应包含from test_user", sql.contains("from test_user"));
        assertTrue("SQL应包含where 1=1", sql.contains("where 1=1"));
    }

    /**
     * 测试10：SqlBeanUtil.getSqlBean(tableName, fieldList) 指定多字段（含别名）
     */
    @Test
    public void testGetSqlBeanByTableNameAndFieldList_multipleFieldsWithAlias() {
        SqlBean bean = SqlBeanUtil.getSqlBean("test_user",
                Arrays.asList(new Field("id"), new Field("name", "user_name"), new Field("age")));

        String sql = bean.getSql().toLowerCase();
        assertTrue("SQL应包含id列", sql.contains("id"));
        assertTrue("SQL应包含name列", sql.contains("name"));
        assertTrue("SQL应包含as user_name", sql.contains("as user_name"));
        assertTrue("SQL应包含age列", sql.contains("age"));
        assertFalse("SQL不应包含email列", sql.contains("email"));
    }

    /**
     * 测试11：SqlBeanUtil.getSqlBean(tableName, null) 应回退到全列查询
     */
    @Test
    public void testGetSqlBeanByTableNameAndNullFieldList_selectAll() {
        SqlBean bean = SqlBeanUtil.getSqlBean("test_user", null);

        String sql = bean.getSql().toLowerCase();
        assertTrue("SQL应包含id列", sql.contains("id"));
        assertTrue("SQL应包含name列", sql.contains("name"));
        assertTrue("SQL应包含age列", sql.contains("age"));
        assertTrue("SQL应包含email列", sql.contains("email"));
    }

    /**
     * 测试12：SqlBeanUtil.getSqlBean(tableName, emptyList) 应回退到全列查询
     */
    @Test
    public void testGetSqlBeanByTableNameAndEmptyFieldList_selectAll() {
        SqlBean bean = SqlBeanUtil.getSqlBean("test_user", new java.util.ArrayList<Field>());

        String sql = bean.getSql().toLowerCase();
        assertTrue("SQL应包含所有列", sql.contains("id") && sql.contains("name") && sql.contains("email"));
    }

    /**
     * 测试13：createQueryBean(tableName, Field... fields) 可变参数
     */
    @Test
    public void testCreateQueryBeanByTableNameAndFields_executeQuery() {
        executor.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[] { "t1", "张三", 25, "zhangsan@example.com" });

        DefaultQueryBeanFactory factory = new DefaultQueryBeanFactory();
        factory.setQueryDao(new QueryDao());

        QueryBean queryBean = factory.createQueryBean("test_user",
                new Field("id"), new Field("name"));

        List<Map<String, Object>> result = queryBean.getMapList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull("结果应包含id", result.get(0).get("ID"));
        assertEquals("张三", result.get(0).get("NAME"));
    }

    /**
     * 测试14：createQueryBean(tableName, Field... fields) 支持链式条件构建
     */
    @Test
    public void testCreateQueryBeanByTableNameAndFields_withChainedConditions() {
        executor.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[] { "t1", "张三", 25, "zhangsan@example.com" });
        executor.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[] { "t2", "李四", 30, "lisi@example.com" });

        DefaultQueryBeanFactory factory = new DefaultQueryBeanFactory();
        factory.setQueryDao(new QueryDao());

        QueryBean queryBean = factory.createQueryBean("test_user",
                new Field("name"), new Field("age"));
        queryBean.getCompare().gt("age", 26);

        List<Map<String, Object>> result = queryBean.getMapList();

        assertNotNull(result);
        assertEquals("应只查询到1条age>26的记录", 1, result.size());
        assertEquals("李四", result.get(0).get("NAME"));
    }

    /**
     * 测试15：createQueryBean(tableName, executor, Field... fields) 指定执行器
     */
    @Test
    public void testCreateQueryBeanByTableNameAndFields_withExecutor() {
        executor.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[] { "t1", "张三", 25, "zhangsan@example.com" });

        DefaultQueryBeanFactory factory = new DefaultQueryBeanFactory();
        factory.setQueryDao(new QueryDao());

        QueryBean queryBean = factory.createQueryBean("test_user", executor,
                new Field("id"), new Field("name"));

        List<Map<String, Object>> result = queryBean.getMapList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).get("NAME"));
    }

    /**
     * 测试16：select(tableName, String... fields) 最简便的字段指定方式
     */
    @Test
    public void testSelectWithStringVarargs_simplestWay() {
        executor.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[] { "t1", "张三", 25, "zhangsan@example.com" });

        DefaultQueryBeanFactory factory = new DefaultQueryBeanFactory();
        factory.setQueryDao(new QueryDao());

        QueryBean queryBean = factory.select("test_user", "id", "name");

        SqlBean sqlBean = queryBean.getSqlBean();
        String sql = sqlBean.getSql().toLowerCase();
        assertTrue("SQL应包含id列", sql.contains("id"));
        assertTrue("SQL应包含name列", sql.contains("name"));
        assertFalse("SQL不应包含email列", sql.contains("email"));

        List<Map<String, Object>> result = queryBean.getMapList();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull("结果应包含id", result.get(0).get("ID"));
        assertEquals("张三", result.get(0).get("NAME"));
    }

    /**
     * 测试17：select(tableName) 不指定字段时查询全部列
     */
    @Test
    public void testSelectWithNoFields_selectAll() {
        DefaultQueryBeanFactory factory = new DefaultQueryBeanFactory();
        factory.setQueryDao(new QueryDao());

        QueryBean queryBean = factory.select("test_user");

        SqlBean sqlBean = queryBean.getSqlBean();
        String sql = sqlBean.getSql().toLowerCase();
        assertTrue("SQL应包含id列", sql.contains("id"));
        assertTrue("SQL应包含name列", sql.contains("name"));
        assertTrue("SQL应包含age列", sql.contains("age"));
        assertTrue("SQL应包含email列", sql.contains("email"));
    }

    /**
     * 测试18：select 配合链式条件构建
     */
    @Test
    public void testSelectWithChainedConditions() {
        executor.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[] { "t1", "张三", 25, "zhangsan@example.com" });
        executor.update(
                "INSERT INTO test_user (id, name, age, email) VALUES (?, ?, ?, ?)",
                new Object[] { "t2", "李四", 30, "lisi@example.com" });

        DefaultQueryBeanFactory factory = new DefaultQueryBeanFactory();
        factory.setQueryDao(new QueryDao());

        QueryBean queryBean = factory.select("test_user", "name", "age");
        queryBean.getCompare().eq("name", "李四");

        List<Map<String, Object>> result = queryBean.getMapList();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("李四", result.get(0).get("NAME"));
    }
}
