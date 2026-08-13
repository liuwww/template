package io.github.liuwww.db.config.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import io.github.liuwww.db.config.DatabaseConfigCenter;
import io.github.liuwww.db.config.TableStructureContainer;
import io.github.liuwww.db.config.TableStructureLoader;
import io.github.liuwww.db.config.TableStructureLoaderType;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.executor.DatabaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用数据库配置中心实现
 * 使用策略模式管理不同的表结构加载方式
 *
 * @author lww
 */
public class GenericDatabaseConfigCenter implements DatabaseConfigCenter {

    private static final Logger logger = LoggerFactory.getLogger(GenericDatabaseConfigCenter.class);

    private TableStructureContainer tableStructureContainer;

    private List<DataSource> dataSourceList;

    /** beanName -> DataSource 映射，用于 JSON 多数据源模式 */
    private final Map<String, DataSource> dataSourceBindings = new LinkedHashMap<>();

    private DatabaseExecutor databaseExecutor;

    private Map<TableStructureLoaderType, TableStructureLoader> loaderMap;

    private TableStructureLoaderType currentLoaderType;

    public GenericDatabaseConfigCenter() {
        this.dataSourceList = new ArrayList<>();
        this.loaderMap = new HashMap<>();
        registerLoader(TableStructureLoaderType.DATASOURCE, new DataSourceTableStructureLoader());
        registerLoader(TableStructureLoaderType.JSON, new JsonTableStructureLoader());
        registerLoader(TableStructureLoaderType.COMPOSITE, new CompositeTableStructureLoader());
        this.currentLoaderType = TableStructureLoaderType.DATASOURCE;
    }

    @Override
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("开始初始化数据库配置中心...");
        }

        TableStructureLoader loader = loaderMap.get(currentLoaderType);
        if (loader == null) {
            throw new IllegalStateException("未找到加载器类型: " + currentLoaderType
                    + "，请先通过 registerLoader 注册");
        }

        // JSON 模式：将 beanName -> DataSource 绑定传递给 loader
        if (loader instanceof JsonTableStructureLoader) {
            ((JsonTableStructureLoader) loader).setDataSourceBindings(dataSourceBindings);
        }

        this.tableStructureContainer = loader.loadStructureContainer();

        int tableCount = tableStructureContainer.getTableNames().size();
        if (tableCount == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn("配置中心初始化完成但未加载到任何表，请检查 loaderType={} 的配置", currentLoaderType);
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("数据库配置中心初始化完成，共加载 {} 个表", tableCount);
        }
    }

    /**
     * 设置当前使用的加载器类型
     *
     * @param loaderType 加载器类型
     */
    public void setCurrentLoaderType(TableStructureLoaderType loaderType) {
        this.currentLoaderType = loaderType;
    }

    /**
     * 获取当前使用的加载器类型
     *
     * @return 加载器类型
     */
    public TableStructureLoaderType getCurrentLoaderType() {
        return currentLoaderType;
    }

    /**
     * 从数据源加载表结构
     *
     * @param dataSource 数据源
     */
    public void loadTableStructureFromDataSource(DataSource dataSource) {
        addDataSource(dataSource);
        refresh();
    }

    /**
     * 注册加载器
     *
     * @param type   加载器类型
     * @param loader 加载器实现
     */
    public void registerLoader(TableStructureLoaderType type, TableStructureLoader loader) {
        loaderMap.put(type, loader);
    }

    /**
     * 获取指定类型的加载器
     *
     * @param type 加载器类型
     * @return 加载器实现
     */
    public TableStructureLoader getLoader(TableStructureLoaderType type) {
        return loaderMap.get(type);
    }

    /**
     * 向 COMPOSITE 加载器添加子加载器。
     * <p>
     * 适用于多数据源环境下不同数据源使用不同加载方式的场景。
     * 调用前应确保 COMPOSITE 加载器已注册（默认构造时已注册）。
     * </p>
     *
     * @param loader 子加载器
     * @throws IllegalStateException 如果 COMPOSITE 加载器未注册或类型不匹配
     */
    public void addCompositeLoader(TableStructureLoader loader) {
        TableStructureLoader composite = loaderMap.get(TableStructureLoaderType.COMPOSITE);
        if (composite instanceof CompositeTableStructureLoader) {
            ((CompositeTableStructureLoader) composite).addLoader(loader);
        } else {
            throw new IllegalStateException("COMPOSITE 加载器未注册或类型不匹配");
        }
    }

    /**
     * 添加数据源
     *
     * @param dataSource 数据源
     */
    public void addDataSource(DataSource dataSource) {
        addDataSource(dataSource, null);
    }

    /**
     * 添加数据源并关联 bean name。
     * <p>
     * beanName 用于 JSON 多数据源模式下按数据源匹配 JSON 文件。
     * 单数据源场景可传 null。
     * </p>
     *
     * @param dataSource 数据源
     * @param beanName   Spring 容器中的 bean name，用于多数据源 JSON 匹配
     */
    public void addDataSource(DataSource dataSource, String beanName) {
        if (!dataSourceList.contains(dataSource)) {
            dataSourceList.add(dataSource);
        }
        if (beanName != null) {
            dataSourceBindings.put(beanName, dataSource);
        }
        TableStructureLoader loader = loaderMap.get(TableStructureLoaderType.DATASOURCE);
        if (loader instanceof DataSourceTableStructureLoader) {
            ((DataSourceTableStructureLoader) loader).addDataSource(dataSource);
        }
    }

    /**
     * 获取 beanName -> DataSource 映射。
     *
     * @return 不可变映射
     */
    public Map<String, DataSource> getDataSourceBindings() {
        return new LinkedHashMap<>(dataSourceBindings);
    }

    @Override
    public TableMetaData getTableMetaData(String tableName) {
        if (tableName == null) {
            return null;
        }
        return tableStructureContainer.getTableMetaData(tableName);
    }

    @Override
    public TableMetaData getTableMetaData(String tableName, DataSource dataSource) {
        return tableStructureContainer.getTableMetaData(tableName, dataSource);
    }

    @Override
    public List<String> getTableNames() {
        return tableStructureContainer.getTableNames();
    }

    @Override
    public Map<String, TableMetaData> getAllTableMetaData() {
        return tableStructureContainer.getAllTableMetaData();
    }

    @Override
    public void refresh() {
        if (logger.isDebugEnabled()) {
            logger.debug("开始刷新数据库配置中心...");
        }

        TableStructureContainer oldContainer = this.tableStructureContainer;

        TableStructureLoader loader = loaderMap.get(currentLoaderType);
        if (loader == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("刷新失败：未找到加载器");
            }
            return;
        }

        // JSON 模式：将 beanName -> DataSource 绑定传递给 loader
        if (loader instanceof JsonTableStructureLoader) {
            ((JsonTableStructureLoader) loader).setDataSourceBindings(dataSourceBindings);
        }

        TableStructureContainer newContainer = loader.loadStructureContainer();

        if (newContainer != null && newContainer.getTableNames().size() > 0) {
            this.tableStructureContainer = newContainer;
            oldContainer.clear();
            if (logger.isDebugEnabled()) {
                logger.debug("刷新成功，共加载 {} 个表", tableStructureContainer.getTableNames().size());
            }
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("刷新失败，保持原有数据");
            }
        }
    }

    @Override
    public void setDatabaseExecutor(DatabaseExecutor databaseExecutor) {
        this.databaseExecutor = databaseExecutor;
    }

    @Override
    public DatabaseExecutor getDatabaseExecutor() {
        return this.databaseExecutor;
    }

    /**
     * 获取所有数据源
     *
     * @return 数据源列表
     */
    public List<DataSource> getDataSources() {
        return new ArrayList<>(dataSourceList);
    }

    // ========== 导出 API ==========

    /**
     * 导出所有表结构到单个 JSON 文件。
     * <p>
     * 适用于单数据源场景，或不需要区分数据源的场景。
     * </p>
     *
     * @param filePath 输出文件路径
     */
    public void exportToJson(String filePath) {
        List<TableMetaData> tables = new ArrayList<>(getAllTableMetaData().values());
        JsonTableStructureLoader.exportToFile(tables, filePath);
    }

    /**
     * 按数据源 beanName 分组导出到目录，每个数据源一个文件（{beanName}.json）。
     * <p>
     * 适用于多数据源场景。导出后可配置 {@code template-jdbc.json.datasources.{beanName}}
     * 指向对应文件，实现 JSON 模式按数据源加载。
     * </p>
     * <p>
     * 未关联 beanName 的表（beanName 为 null）会导出到 {@code default.json}。
     * </p>
     *
     * @param dirPath 输出目录路径
     */
    public void exportByDataSource(String dirPath) {
        // 构建 DataSource -> beanName 反向映射
        Map<DataSource, String> dsToBeanName = new HashMap<>();
        for (Map.Entry<String, DataSource> entry : dataSourceBindings.entrySet()) {
            dsToBeanName.put(entry.getValue(), entry.getKey());
        }

        // 按数据源分组
        Map<String, List<TableMetaData>> tablesByBeanName = new LinkedHashMap<>();
        for (TableMetaData tmd : getAllTableMetaData().values()) {
            DataSource ds = tmd.getDataSource();
            String beanName = dsToBeanName.get(ds);
            if (beanName == null) {
                beanName = "default";
            }
            tablesByBeanName.computeIfAbsent(beanName, k -> new ArrayList<>()).add(tmd);
        }

        JsonTableStructureLoader.exportByDataSource(tablesByBeanName, dirPath);
    }
}