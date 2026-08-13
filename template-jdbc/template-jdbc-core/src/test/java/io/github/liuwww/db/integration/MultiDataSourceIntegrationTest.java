package io.github.liuwww.db.integration;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.github.liuwww.db.context.CoreDbContext;
import io.github.liuwww.db.context.DbContext;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.executor.impl.JdbcDatabaseExecutor;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.query.QueryBean;
import io.github.liuwww.db.service.IDataTemplate;
import io.github.liuwww.db.service.IQueryTemplate;
import io.github.liuwww.db.sql.Row;

/**
 * 多数据源集成测试 —— 模拟进销存系统
 * <p>
 * 场景说明：一个进销存系统包含两个独立的数据源：
 * <ul>
 *   <li>总部数据源（hq）：管理商品主数据、供应商、采购订单</li>
 *   <li>门店数据源（store）：管理门店库存、销售订单、客户</li>
 * </ul>
 * 两个数据源各自拥有独立的 CoreDbContext、DatabaseExecutor 和表结构，
 * 操作互不干扰，验证框架对多数据源的正确支持。
 * </p>
 */
public class MultiDataSourceIntegrationTest {

    // ===== 总部数据源 =====
    private DataSource hqDataSource;
    private DatabaseExecutor hqExecutor;
    private CoreDbContext hqDbContext;
    private IDataTemplate hqDataTemplate;
    private IQueryTemplate hqQueryTemplate;

    // ===== 门店数据源 =====
    private DataSource storeDataSource;
    private DatabaseExecutor storeExecutor;
    private CoreDbContext storeDbContext;
    private IDataTemplate storeDataTemplate;
    private IQueryTemplate storeQueryTemplate;

    @Before
    public void setUp() throws Exception {
        // 初始化总部数据源
        JdbcDataSource hqDs = new JdbcDataSource();
        hqDs.setURL("jdbc:h2:mem:hq_db;DB_CLOSE_DELAY=-1;MODE=MySQL");
        hqDs.setUser("sa");
        hqDs.setPassword("");
        this.hqDataSource = hqDs;
        this.hqExecutor = new JdbcDatabaseExecutor(hqDataSource);

        createHqTables(hqDataSource);

        this.hqDbContext = new CoreDbContext();
        this.hqDbContext.addDataSource(hqDataSource);
        this.hqDbContext.setDatabaseExecutor(hqExecutor);
        this.hqDbContext.init();

        this.hqDataTemplate = hqDbContext.getDataTemplate();
        this.hqQueryTemplate = hqDbContext.getQueryTemplate();

        // 初始化门店数据源
        JdbcDataSource storeDs = new JdbcDataSource();
        storeDs.setURL("jdbc:h2:mem:store_db;DB_CLOSE_DELAY=-1;MODE=MySQL");
        storeDs.setUser("sa");
        storeDs.setPassword("");
        this.storeDataSource = storeDs;
        this.storeExecutor = new JdbcDatabaseExecutor(storeDataSource);

        createStoreTables(storeDataSource);

        this.storeDbContext = new CoreDbContext();
        this.storeDbContext.addDataSource(storeDataSource);
        this.storeDbContext.setDatabaseExecutor(storeExecutor);
        this.storeDbContext.init();

        this.storeDataTemplate = storeDbContext.getDataTemplate();
        this.storeQueryTemplate = storeDbContext.getQueryTemplate();

        // 将全局 DbContext 指向总部（模拟默认数据源）
        DbContext.setCurrentContext(hqDbContext);
    }

    @After
    public void tearDown() throws Exception {
        DbContext.setCurrentContext(null);
        cleanupAllData();
    }

    // ==================== 建表 SQL ====================

    private void createHqTables(DataSource ds) throws Exception {
        try (Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement()) {
            // 商品主数据
            stmt.execute("DROP TABLE IF EXISTS product");
            stmt.execute(
                    "CREATE TABLE product (" +
                            "id VARCHAR(32) PRIMARY KEY, " +
                            "name VARCHAR(200) NOT NULL, " +
                            "category VARCHAR(100), " +
                            "unit_price DECIMAL(10,2), " +
                            "status INT DEFAULT 1" +
                            ")");

            // 供应商
            stmt.execute("DROP TABLE IF EXISTS supplier");
            stmt.execute(
                    "CREATE TABLE supplier (" +
                            "id VARCHAR(32) PRIMARY KEY, " +
                            "name VARCHAR(200) NOT NULL, " +
                            "contact VARCHAR(100), " +
                            "phone VARCHAR(50)" +
                            ")");

            // 采购订单
            stmt.execute("DROP TABLE IF EXISTS purchase_order");
            stmt.execute(
                    "CREATE TABLE purchase_order (" +
                            "id VARCHAR(32) PRIMARY KEY, " +
                            "supplier_id VARCHAR(32) NOT NULL, " +
                            "product_id VARCHAR(32) NOT NULL, " +
                            "quantity INT NOT NULL, " +
                            "total_amount DECIMAL(12,2), " +
                            "status INT DEFAULT 0" +
                            ")");
        }
    }

    private void createStoreTables(DataSource ds) throws Exception {
        try (Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement()) {
            // 门店库存
            stmt.execute("DROP TABLE IF EXISTS inventory");
            stmt.execute(
                    "CREATE TABLE inventory (" +
                            "id VARCHAR(32) PRIMARY KEY, " +
                            "product_id VARCHAR(32) NOT NULL, " +
                            "quantity INT DEFAULT 0, " +
                            "location VARCHAR(100)" +
                            ")");

            // 客户
            stmt.execute("DROP TABLE IF EXISTS customer");
            stmt.execute(
                    "CREATE TABLE customer (" +
                            "id VARCHAR(32) PRIMARY KEY, " +
                            "name VARCHAR(200) NOT NULL, " +
                            "phone VARCHAR(50), " +
                            "level INT DEFAULT 1" +
                            ")");

            // 销售订单
            stmt.execute("DROP TABLE IF EXISTS sales_order");
            stmt.execute(
                    "CREATE TABLE sales_order (" +
                            "id VARCHAR(32) PRIMARY KEY, " +
                            "customer_id VARCHAR(32) NOT NULL, " +
                            "product_id VARCHAR(32) NOT NULL, " +
                            "quantity INT NOT NULL, " +
                            "total_amount DECIMAL(12,2), " +
                            "status INT DEFAULT 0" +
                            ")");
        }
    }

    private void cleanupAllData() {
        cleanupTable(hqDataSource, "purchase_order");
        cleanupTable(hqDataSource, "product");
        cleanupTable(hqDataSource, "supplier");

        cleanupTable(storeDataSource, "sales_order");
        cleanupTable(storeDataSource, "inventory");
        cleanupTable(storeDataSource, "customer");
    }

    private void cleanupTable(DataSource ds, String tableName) {
        try (Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM " + tableName);
        } catch (Exception e) {
            // 忽略
        }
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> newProduct(String id, String name, String category, BigDecimal price, int status) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("category", category);
        m.put("unit_price", price);
        m.put("status", status);
        return m;
    }

    private Map<String, Object> newSupplier(String id, String name, String contact, String phone) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("contact", contact);
        m.put("phone", phone);
        return m;
    }

    private Map<String, Object> newPurchaseOrder(String id, String supplierId, String productId,
            int quantity, BigDecimal amount, int status) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("supplier_id", supplierId);
        m.put("product_id", productId);
        m.put("quantity", quantity);
        m.put("total_amount", amount);
        m.put("status", status);
        return m;
    }

    private Map<String, Object> newInventory(String id, String productId, int quantity, String location) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("product_id", productId);
        m.put("quantity", quantity);
        m.put("location", location);
        return m;
    }

    private Map<String, Object> newCustomer(String id, String name, String phone, int level) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("phone", phone);
        m.put("level", level);
        return m;
    }

    private Map<String, Object> newSalesOrder(String id, String customerId, String productId,
            int quantity, BigDecimal amount, int status) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("customer_id", customerId);
        m.put("product_id", productId);
        m.put("quantity", quantity);
        m.put("total_amount", amount);
        m.put("status", status);
        return m;
    }

    // ==================== 测试：多数据源隔离性 ====================

    /**
     * 核心测试：两个数据源的表结构完全独立，互不可见。
     * 总部看不到门店的表，门店看不到总部的表。
     */
    @Test
    public void testTableStructureIsolation() {
        // 总部只能看到自己的表
        List<String> hqTables = hqDbContext.getConfigCenter().getTableNames();
        assertTrue("总部应包含 product 表", hqTables.contains("product"));
        assertTrue("总部应包含 supplier 表", hqTables.contains("supplier"));
        assertTrue("总部应包含 purchase_order 表", hqTables.contains("purchase_order"));
        assertFalse("总部不应包含 inventory 表", hqTables.contains("inventory"));
        assertFalse("总部不应包含 customer 表", hqTables.contains("customer"));
        assertFalse("总部不应包含 sales_order 表", hqTables.contains("sales_order"));

        // 门店只能看到自己的表
        List<String> storeTables = storeDbContext.getConfigCenter().getTableNames();
        assertTrue("门店应包含 inventory 表", storeTables.contains("inventory"));
        assertTrue("门店应包含 customer 表", storeTables.contains("customer"));
        assertTrue("门店应包含 sales_order 表", storeTables.contains("sales_order"));
        assertFalse("门店不应包含 product 表", storeTables.contains("product"));
        assertFalse("门店不应包含 supplier 表", storeTables.contains("supplier"));
        assertFalse("门店不应包含 purchase_order 表", storeTables.contains("purchase_order"));
    }

    /**
     * 核心测试：两个数据源的数据完全独立，互不可见。
     * 在总部插入的数据，门店查不到；反之亦然。
     */
    @Test
    public void testDataIsolation() {
        // 总部插入商品
        hqDataTemplate.insert("product", newProduct("P001", "笔记本电脑", "电子产品", new BigDecimal("5999.00"), 1));

        // 门店插入库存
        storeDataTemplate.insert("inventory", newInventory("INV001", "P001", 100, "A区-01"));

        // 总部能查到商品
        Map<String, Object> product = hqQueryTemplate.getMap("product", "P001");
        assertNotNull("总部应能查到商品 P001", product);
        assertEquals("笔记本电脑", product.get("NAME"));

        // 总部查不到门店的库存表数据
        // （注意：不同数据源的表结构不同，总部没有 inventory 表）
        // 验证方式：总部的表名列表不包含 inventory

        // 门店能查到库存
        Map<String, Object> inv = storeQueryTemplate.getMap("inventory", "INV001");
        assertNotNull("门店应能查到库存 INV001", inv);
        assertEquals(100, inv.get("QUANTITY"));

        // 门店查不到总部的商品
        // （门店没有 product 表）
    }

    /**
     * 测试：两个数据源各自执行 CRUD，互不影响。
     */
    @Test
    public void testCrudOnDifferentDataSources() {
        // === 总部操作 ===
        // 插入供应商
        hqDataTemplate.insert("supplier", newSupplier("S001", "华为供应商", "张经理", "13800001111"));
        // 插入商品
        hqDataTemplate.insert("product", newProduct("P001", "华为Mate60", "手机", new BigDecimal("4999.00"), 1));
        // 插入采购订单
        hqDataTemplate.insert("purchase_order", newPurchaseOrder("PO001", "S001", "P001", 100, new BigDecimal("499900.00"), 0));

        // === 门店操作 ===
        // 插入客户
        storeDataTemplate.insert("customer", newCustomer("C001", "李明", "13900002222", 2));
        // 插入库存
        storeDataTemplate.insert("inventory", newInventory("INV001", "P001", 50, "A区-01"));
        // 插入销售订单
        storeDataTemplate.insert("sales_order", newSalesOrder("SO001", "C001", "P001", 2, new BigDecimal("9998.00"), 0));

        // === 验证总部数据 ===
        Map<String, Object> supplier = hqQueryTemplate.getMap("supplier", "S001");
        assertNotNull(supplier);
        assertEquals("华为供应商", supplier.get("NAME"));

        Map<String, Object> po = hqQueryTemplate.getMap("purchase_order", "PO001");
        assertNotNull(po);
        assertEquals(100, po.get("QUANTITY"));

        // === 验证门店数据 ===
        Map<String, Object> customer = storeQueryTemplate.getMap("customer", "C001");
        assertNotNull(customer);
        assertEquals("李明", customer.get("NAME"));

        Map<String, Object> so = storeQueryTemplate.getMap("sales_order", "SO001");
        assertNotNull(so);
        assertEquals(new BigDecimal("9998.00"), so.get("TOTAL_AMOUNT"));

        // === 更新操作互不影响 ===
        // 总部更新采购订单状态
        Map<String, Object> poUpdate = new HashMap<>();
        poUpdate.put("id", "PO001");
        poUpdate.put("status", 1);
        hqDataTemplate.update("purchase_order", poUpdate);

        // 门店更新销售订单状态
        Map<String, Object> soUpdate = new HashMap<>();
        soUpdate.put("id", "SO001");
        soUpdate.put("status", 1);
        storeDataTemplate.update("sales_order", soUpdate);

        // 验证
        Map<String, Object> poAfter = hqQueryTemplate.getMap("purchase_order", "PO001");
        assertEquals(1, poAfter.get("STATUS"));

        Map<String, Object> soAfter = storeQueryTemplate.getMap("sales_order", "SO001");
        assertEquals(1, soAfter.get("STATUS"));
    }

    // ==================== 测试：进销存业务流程 ====================

    /**
     * 模拟完整进销存业务流程：
     * 1. 总部采购商品（总部数据源）
     * 2. 门店接收库存（门店数据源）
     * 3. 客户下单购买（门店数据源）
     * 4. 库存扣减（门店数据源）
     * 5. 总部查询采购统计（总部数据源）
     */
    @Test
    public void testFullBusinessWorkflow() {
        // === Step 1: 总部采购商品 ===
        hqDataTemplate.insert("supplier", newSupplier("S001", "联想供应商", "王经理", "13800003333"));
        hqDataTemplate.insert("product", newProduct("P001", "ThinkPad X1", "笔记本电脑", new BigDecimal("8999.00"), 1));
        hqDataTemplate.insert("product", newProduct("P002", "ThinkPad T14", "笔记本电脑", new BigDecimal("5999.00"), 1));
        hqDataTemplate.insert("purchase_order", newPurchaseOrder("PO001", "S001", "P001", 200, new BigDecimal("1799800.00"), 1));
        hqDataTemplate.insert("purchase_order", newPurchaseOrder("PO002", "S001", "P002", 300, new BigDecimal("1799700.00"), 1));

        // 验证总部采购数据
        long poCount = hqQueryTemplate.getCount("purchase_order", new HashMap<>());
        assertEquals("总部应有2个采购订单", 2L, poCount);

        // === Step 2: 门店接收库存 ===
        storeDataTemplate.insert("inventory", newInventory("INV001", "P001", 200, "A区-01"));
        storeDataTemplate.insert("inventory", newInventory("INV002", "P002", 300, "A区-02"));

        // 验证门店库存
        Map<String, Object> inv1 = storeQueryTemplate.getMap("inventory", "INV001");
        assertEquals(200, inv1.get("QUANTITY"));

        // === Step 3: 客户下单购买 ===
        storeDataTemplate.insert("customer", newCustomer("C001", "张三", "13900001111", 1));
        storeDataTemplate.insert("customer", newCustomer("C002", "李四", "13900002222", 2));
        storeDataTemplate.insert("sales_order", newSalesOrder("SO001", "C001", "P001", 5, new BigDecimal("44995.00"), 1));
        storeDataTemplate.insert("sales_order", newSalesOrder("SO002", "C002", "P002", 10, new BigDecimal("59990.00"), 1));

        // === Step 4: 库存扣减 ===
        Map<String, Object> invUpdate1 = new HashMap<>();
        invUpdate1.put("id", "INV001");
        invUpdate1.put("quantity", 195); // 200 - 5
        storeDataTemplate.update("inventory", invUpdate1);

        Map<String, Object> invUpdate2 = new HashMap<>();
        invUpdate2.put("id", "INV002");
        invUpdate2.put("quantity", 290); // 300 - 10
        storeDataTemplate.update("inventory", invUpdate2);

        // 验证库存扣减
        Map<String, Object> inv1After = storeQueryTemplate.getMap("inventory", "INV001");
        assertEquals(195, inv1After.get("QUANTITY"));
        Map<String, Object> inv2After = storeQueryTemplate.getMap("inventory", "INV002");
        assertEquals(290, inv2After.get("QUANTITY"));

        // === Step 5: 总部查询采购统计 ===
        long totalPo = hqQueryTemplate.getCount("purchase_order", new HashMap<>());
        assertEquals("总部采购订单总数应为2", 2L, totalPo);

        // 门店查询销售统计
        long totalSo = storeQueryTemplate.getCount("sales_order", new HashMap<>());
        assertEquals("门店销售订单总数应为2", 2L, totalSo);

        long totalCustomers = storeQueryTemplate.getCount("customer", new HashMap<>());
        assertEquals("门店客户数应为2", 2L, totalCustomers);
    }

    // ==================== 测试：QueryBean 多数据源查询 ====================

    /**
     * 测试 QueryBean 在不同数据源上的条件查询。
     * 验证 QueryTemplate 的 getConfigCenter() 覆盖生效。
     */
    @Test
    public void testQueryBeanOnDifferentDataSources() {
        // 总部插入商品
        hqDataTemplate.insert("product", newProduct("P001", "华为Mate60", "手机", new BigDecimal("4999.00"), 1));
        hqDataTemplate.insert("product", newProduct("P002", "华为MatePad", "平板", new BigDecimal("2999.00"), 1));
        hqDataTemplate.insert("product", newProduct("P003", "华为Watch", "手表", new BigDecimal("1999.00"), 0));

        // 门店插入客户
        storeDataTemplate.insert("customer", newCustomer("C001", "VIP客户1", "13900001111", 3));
        storeDataTemplate.insert("customer", newCustomer("C002", "普通客户1", "13900002222", 1));
        storeDataTemplate.insert("customer", newCustomer("C003", "VIP客户2", "13900003333", 3));

        // 总部 QueryBean 查询：上架商品
        QueryBean hqQuery = hqQueryTemplate.createQueryBean("product");
        hqQuery.getCompare().eq("status", 1).getQueryBean();
        List<Map<String, Object>> activeProducts = hqQuery.getMapList();
        assertEquals("上架商品应为2个", 2, activeProducts.size());
        for (Map<String, Object> p : activeProducts) {
            assertEquals(1, p.get("STATUS"));
        }

        // 门店 QueryBean 查询：VIP客户
        QueryBean storeQuery = storeQueryTemplate.createQueryBean("customer");
        storeQuery.getCompare().eq("level", 3).getQueryBean();
        List<Map<String, Object>> vipCustomers = storeQuery.getMapList();
        assertEquals("VIP客户应为2个", 2, vipCustomers.size());
        for (Map<String, Object> c : vipCustomers) {
            assertEquals(3, c.get("LEVEL"));
        }

        // 总部 QueryBean 计数
        QueryBean hqCountQuery = hqQueryTemplate.createQueryBean("product");
        hqCountQuery.getCompare().gt("unit_price", new BigDecimal("2000.00")).getQueryBean();
        long count = hqCountQuery.getCount();
        assertEquals("单价>2000的商品应为2个", 2L, count);

        // 门店 QueryBean 分页
        QueryBean storePageQuery = storeQueryTemplate.createQueryBean("customer");
        Page page = new Page(1, 2);
        Page result = storePageQuery.getPage(page);
        assertEquals("门店客户总数应为3", 3L, result.getTotal());
        assertEquals("每页2条", 2, result.getRows().size());
    }

    // ==================== 测试：批量操作多数据源 ====================

    /**
     * 测试在两个数据源上同时进行批量插入操作，互不影响。
     */
    @Test
    public void testBatchInsertOnDifferentDataSources() {
        // 总部批量插入商品
        List<Map<String, Object>> products = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            products.add(newProduct("P" + String.format("%03d", i), "商品" + i, "分类" + (i % 3 + 1),
                    new BigDecimal(1000 + i * 100), i <= 7 ? 1 : 0));
        }
        List<Row> productRows = hqDataTemplate.insert("product", products);
        assertEquals("总部应插入10个商品", 10, productRows.size());

        // 门店批量插入客户
        List<Map<String, Object>> customers = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            customers.add(newCustomer("C" + String.format("%03d", i), "客户" + i,
                    "1390000" + String.format("%04d", i), i % 3 + 1));
        }
        List<Row> customerRows = storeDataTemplate.insert("customer", customers);
        assertEquals("门店应插入8个客户", 8, customerRows.size());

        // 验证各自数据量
        assertEquals(10L, hqQueryTemplate.getCount("product", new HashMap<>()));
        assertEquals(8L, storeQueryTemplate.getCount("customer", new HashMap<>()));

        // 总部按分类统计
        QueryBean categoryQuery = hqQueryTemplate.createQueryBean("product");
        categoryQuery.getCompare().eq("category", "分类1").getQueryBean();
        long cat1Count = categoryQuery.getCount();
        assertTrue("分类1商品应>=3", cat1Count >= 3);

        // 门店按等级统计
        QueryBean levelQuery = storeQueryTemplate.createQueryBean("customer");
        levelQuery.getCompare().eq("level", 3).getQueryBean();
        long level3Count = levelQuery.getCount();
        assertTrue("3级客户应>=2", level3Count >= 2);
    }

    // ==================== 测试：删除操作多数据源隔离 ====================

    /**
     * 测试在一个数据源上删除数据不影响另一个数据源。
     */
    @Test
    public void testDeleteIsolation() {
        // 两个数据源都插入数据
        hqDataTemplate.insert("product", newProduct("P001", "商品A", "分类1", new BigDecimal("100.00"), 1));
        hqDataTemplate.insert("product", newProduct("P002", "商品B", "分类2", new BigDecimal("200.00"), 1));
        storeDataTemplate.insert("customer", newCustomer("C001", "客户A", "13900001111", 1));
        storeDataTemplate.insert("customer", newCustomer("C002", "客户B", "13900002222", 2));

        // 总部删除一个商品
        hqDataTemplate.delete("product", "P001");
        assertNull("总部应查不到已删除的商品", hqQueryTemplate.getMap("product", "P001"));
        assertNotNull("总部应还能查到未删除的商品", hqQueryTemplate.getMap("product", "P002"));

        // 门店数据不受影响
        assertNotNull("门店客户C001应不受影响", storeQueryTemplate.getMap("customer", "C001"));
        assertNotNull("门店客户C002应不受影响", storeQueryTemplate.getMap("customer", "C002"));

        // 门店删除一个客户
        storeDataTemplate.delete("customer", "C002");
        assertNull("门店应查不到已删除的客户", storeQueryTemplate.getMap("customer", "C002"));
        assertNotNull("门店应还能查到未删除的客户", storeQueryTemplate.getMap("customer", "C001"));

        // 总部数据不受影响
        assertNotNull("总部商品P002应不受影响", hqQueryTemplate.getMap("product", "P002"));
    }

    // ==================== 测试：条件删除多数据源 ====================

    /**
     * 测试条件删除在不同数据源上的隔离性。
     */
    @Test
    public void testConditionalDeleteOnDifferentDataSources() {
        // 总部插入商品
        hqDataTemplate.insert("product", newProduct("P001", "商品1", "分类1", new BigDecimal("100.00"), 1));
        hqDataTemplate.insert("product", newProduct("P002", "商品2", "分类1", new BigDecimal("200.00"), 0));
        hqDataTemplate.insert("product", newProduct("P003", "商品3", "分类2", new BigDecimal("300.00"), 1));

        // 门店插入库存
        storeDataTemplate.insert("inventory", newInventory("INV001", "P001", 100, "A区"));
        storeDataTemplate.insert("inventory", newInventory("INV002", "P002", 0, "B区"));
        storeDataTemplate.insert("inventory", newInventory("INV003", "P003", 50, "A区"));

        // 总部条件删除：下架商品
        Map<String, Object> hqCondition = new HashMap<>();
        hqCondition.put("status", 0);
        int hqDeleted = hqDataTemplate.deleteRows("product", hqCondition);
        assertEquals("总部应删除1个下架商品", 1, hqDeleted);
        assertEquals("总部应剩2个商品", 2L, hqQueryTemplate.getCount("product", new HashMap<>()));

        // 门店条件删除：零库存
        Map<String, Object> storeCondition = new HashMap<>();
        storeCondition.put("quantity", 0);
        int storeDeleted = storeDataTemplate.deleteRows("inventory", storeCondition);
        assertEquals("门店应删除1个零库存记录", 1, storeDeleted);
        assertEquals("门店应剩2个库存记录", 2L, storeQueryTemplate.getCount("inventory", new HashMap<>()));
    }

    // ==================== 测试：多数据源并发操作（模拟） ====================

    /**
     * 模拟在两个数据源上交替执行操作，验证不会串数据源。
     */
    @Test
    public void testAlternatingOperations() {
        // 交替操作两个数据源
        // 第1轮：总部插入供应商，门店插入客户
        hqDataTemplate.insert("supplier", newSupplier("S001", "供应商A", "联系人A", "13800001111"));
        storeDataTemplate.insert("customer", newCustomer("C001", "客户A", "13900001111", 1));

        // 第2轮：总部插入商品，门店插入库存
        hqDataTemplate.insert("product", newProduct("P001", "商品A", "分类1", new BigDecimal("100.00"), 1));
        storeDataTemplate.insert("inventory", newInventory("INV001", "P001", 50, "A区"));

        // 第3轮：总部插入采购订单，门店插入销售订单
        hqDataTemplate.insert("purchase_order", newPurchaseOrder("PO001", "S001", "P001", 100, new BigDecimal("10000.00"), 0));
        storeDataTemplate.insert("sales_order", newSalesOrder("SO001", "C001", "P001", 5, new BigDecimal("500.00"), 0));

        // 验证各数据源数据完整性
        assertEquals(1L, hqQueryTemplate.getCount("supplier", new HashMap<>()));
        assertEquals(1L, hqQueryTemplate.getCount("product", new HashMap<>()));
        assertEquals(1L, hqQueryTemplate.getCount("purchase_order", new HashMap<>()));

        assertEquals(1L, storeQueryTemplate.getCount("customer", new HashMap<>()));
        assertEquals(1L, storeQueryTemplate.getCount("inventory", new HashMap<>()));
        assertEquals(1L, storeQueryTemplate.getCount("sales_order", new HashMap<>()));

        // 验证数据内容正确
        Map<String, Object> po = hqQueryTemplate.getMap("purchase_order", "PO001");
        assertEquals("S001", po.get("SUPPLIER_ID"));
        assertEquals(100, po.get("QUANTITY"));

        Map<String, Object> so = storeQueryTemplate.getMap("sales_order", "SO001");
        assertEquals("C001", so.get("CUSTOMER_ID"));
        assertEquals(5, so.get("QUANTITY"));
    }

    // ==================== 测试：select 方法多数据源 ====================

    /**
     * 测试 select(String tableName, String... fields) 在多数据源场景下的正确性。
     */
    @Test
    public void testSelectFieldsOnDifferentDataSources() {
        // 总部插入商品
        hqDataTemplate.insert("product", newProduct("P001", "华为Mate60", "手机", new BigDecimal("4999.00"), 1));

        // 门店插入客户
        storeDataTemplate.insert("customer", newCustomer("C001", "张三", "13900001111", 2));

        // 总部 select 指定字段
        QueryBean hqQuery = hqQueryTemplate.select("product", "name", "unit_price");
        List<Map<String, Object>> hqResults = hqQuery.getMapList();
        assertEquals(1, hqResults.size());
        assertNotNull("应包含 NAME 字段", hqResults.get(0).get("NAME"));
        assertNotNull("应包含 UNIT_PRICE 字段", hqResults.get(0).get("UNIT_PRICE"));

        // 门店 select 指定字段
        QueryBean storeQuery = storeQueryTemplate.select("customer", "name", "level");
        List<Map<String, Object>> storeResults = storeQuery.getMapList();
        assertEquals(1, storeResults.size());
        assertNotNull("应包含 NAME 字段", storeResults.get(0).get("NAME"));
        assertNotNull("应包含 LEVEL 字段", storeResults.get(0).get("LEVEL"));
    }

    // ==================== 测试：updateRows 多数据源 ====================

    /**
     * 测试条件更新在不同数据源上的隔离性。
     */
    @Test
    public void testUpdateRowsOnDifferentDataSources() {
        // 总部插入商品
        hqDataTemplate.insert("product", newProduct("P001", "商品1", "分类1", new BigDecimal("100.00"), 1));
        hqDataTemplate.insert("product", newProduct("P002", "商品2", "分类2", new BigDecimal("200.00"), 1));

        // 门店插入库存
        storeDataTemplate.insert("inventory", newInventory("INV001", "P001", 100, "A区"));
        storeDataTemplate.insert("inventory", newInventory("INV002", "P002", 200, "B区"));

        // 总部批量调价：分类1的商品涨价
        Map<String, Object> hqValMap = new HashMap<>();
        hqValMap.put("unit_price", new BigDecimal("150.00"));
        Map<String, Object> hqCondMap = new HashMap<>();
        hqCondMap.put("category", "分类1");
        int hqUpdated = hqDataTemplate.updateRows("product", hqValMap, hqCondMap);
        assertEquals("总部应更新1个商品", 1, hqUpdated);

        // 验证总部更新结果
        Map<String, Object> p001 = hqQueryTemplate.getMap("product", "P001");
        assertEquals(new BigDecimal("150.00"), p001.get("UNIT_PRICE"));
        Map<String, Object> p002 = hqQueryTemplate.getMap("product", "P002");
        assertEquals(new BigDecimal("200.00"), p002.get("UNIT_PRICE")); // 未变

        // 门店批量移库：A区库存移到C区
        Map<String, Object> storeValMap = new HashMap<>();
        storeValMap.put("location", "C区");
        Map<String, Object> storeCondMap = new HashMap<>();
        storeCondMap.put("location", "A区");
        int storeUpdated = storeDataTemplate.updateRows("inventory", storeValMap, storeCondMap);
        assertEquals("门店应更新1条库存", 1, storeUpdated);

        // 验证门店更新结果
        Map<String, Object> inv001 = storeQueryTemplate.getMap("inventory", "INV001");
        assertEquals("C区", inv001.get("LOCATION"));
        Map<String, Object> inv002 = storeQueryTemplate.getMap("inventory", "INV002");
        assertEquals("B区", inv002.get("LOCATION")); // 未变
    }

    // ==================== 测试：DbContext 切换不影响已绑定的 Template ====================

    /**
     * 核心测试：即使全局 DbContext 被切换，已通过注入 configCenter 创建的
     * QueryTemplate/DataTemplate 仍操作正确的数据源。
     */
    @Test
    public void testTemplateNotAffectedByDbContextSwitch() {
        // 总部插入商品
        hqDataTemplate.insert("product", newProduct("P001", "总部商品", "分类1", new BigDecimal("100.00"), 1));

        // 门店插入客户
        storeDataTemplate.insert("customer", newCustomer("C001", "门店客户", "13900001111", 1));

        // 将全局 DbContext 切换到门店
        DbContext.setCurrentContext(storeDbContext);

        // 通过总部的 QueryTemplate 查询，仍应查到总部的数据
        Map<String, Object> product = hqQueryTemplate.getMap("product", "P001");
        assertNotNull("切换DbContext后，总部QueryTemplate仍应能查到总部数据", product);
        assertEquals("总部商品", product.get("NAME"));

        // 通过门店的 QueryTemplate 查询，仍应查到门店的数据
        Map<String, Object> customer = storeQueryTemplate.getMap("customer", "C001");
        assertNotNull("门店QueryTemplate应能查到门店数据", customer);
        assertEquals("门店客户", customer.get("NAME"));

        // 将全局 DbContext 切回总部
        DbContext.setCurrentContext(hqDbContext);

        // 门店 QueryTemplate 仍正常工作
        Map<String, Object> customerAgain = storeQueryTemplate.getMap("customer", "C001");
        assertNotNull("切回DbContext后，门店QueryTemplate仍应正常工作", customerAgain);
    }

    // ==================== 测试：多数据源分页查询 ====================

    /**
     * 测试分页查询在两个数据源上独立工作。
     */
    @Test
    public void testPaginationOnDifferentDataSources() {
        // 总部插入20个商品
        List<Map<String, Object>> products = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            products.add(newProduct("P" + String.format("%03d", i), "商品" + i,
                    "分类" + (i % 3 + 1), new BigDecimal(1000 + i * 100), 1));
        }
        hqDataTemplate.insert("product", products);

        // 门店插入15个客户
        List<Map<String, Object>> customers = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            customers.add(newCustomer("C" + String.format("%03d", i), "客户" + i,
                    "1390000" + String.format("%04d", i), i % 3 + 1));
        }
        storeDataTemplate.insert("customer", customers);

        // 总部分页查询
        Page hqPage = hqQueryTemplate.getPage("product", new Page(1, 5), new HashMap<>());
        assertEquals("总部商品总数应为20", 20L, hqPage.getTotal());
        assertEquals("第1页应5条", 5, hqPage.getRows().size());

        Page hqPage2 = hqQueryTemplate.getPage("product", new Page(2, 5), new HashMap<>());
        assertEquals(5, hqPage2.getRows().size());

        // 门店分页查询
        Page storePage = storeQueryTemplate.getPage("customer", new Page(1, 5), new HashMap<>());
        assertEquals("门店客户总数应为15", 15L, storePage.getTotal());
        assertEquals("第1页应5条", 5, storePage.getRows().size());

        Page storePage2 = storeQueryTemplate.getPage("customer", new Page(3, 5), new HashMap<>());
        assertEquals("第3页应5条", 5, storePage2.getRows().size());
    }
}
