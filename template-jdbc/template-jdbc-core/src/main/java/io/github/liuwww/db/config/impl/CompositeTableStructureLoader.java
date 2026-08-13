package io.github.liuwww.db.config.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.liuwww.db.config.TableStructureContainer;
import io.github.liuwww.db.config.TableStructureLoader;
import io.github.liuwww.db.context.TableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 组合表结构加载器。
 * <p>
 * 聚合多个 {@link TableStructureLoader}，各 loader 独立加载后合并到统一容器。
 * 适用于多数据源环境下不同数据源使用不同加载方式的场景：
 * </p>
 * <pre>{@code
 * CompositeTableStructureLoader composite = new CompositeTableStructureLoader();
 *
 * // 数据源 A：通过 JDBC 反射加载
 * DataSourceTableStructureLoader dsLoader = new DataSourceTableStructureLoader();
 * dsLoader.addDataSource(dsA);
 * composite.addLoader(dsLoader);
 *
 * // 数据源 B：通过 JSON 声明加载（远程库/只读库，不想连接反射元数据）
 * JsonTableStructureLoader jsonLoader = new JsonTableStructureLoader();
 * jsonLoader.setClasspathResource("table-structure-b.json");
 * jsonLoader.setDataSource(dsB);
 * composite.addLoader(jsonLoader);
 * }</pre>
 * <p>
 * 合并策略：后加载的表若与先加载的表同名，将覆盖先加载的。
 * 这与 {@link TableStructureContainer#addTableMetaData(String, TableMetaData)} 的语义一致。
 * </p>
 *
 * @author liuwww
 */
public class CompositeTableStructureLoader implements TableStructureLoader {

    private static final Logger logger = LoggerFactory.getLogger(CompositeTableStructureLoader.class);

    private final List<TableStructureLoader> loaders = new ArrayList<>();

    public CompositeTableStructureLoader() {
    }

    public CompositeTableStructureLoader(TableStructureLoader... loaders) {
        for (TableStructureLoader loader : loaders) {
            addLoader(loader);
        }
    }

    /**
     * 添加子加载器。
     *
     * @param loader 子加载器，null 被忽略
     */
    public void addLoader(TableStructureLoader loader) {
        if (loader != null) {
            loaders.add(loader);
        }
    }

    @Override
    public TableStructureContainer loadStructureContainer() {
        TableStructureContainer container = new DataSourceContextTableStructureContainer();

        for (TableStructureLoader loader : loaders) {
            try {
                TableStructureContainer sub = loader.loadStructureContainer();
                if (sub == null) {
                    continue;
                }
                Map<String, TableMetaData> all = sub.getAllTableMetaData();
                for (Map.Entry<String, TableMetaData> entry : all.entrySet()) {
                    container.addTableMetaData(entry.getKey(), entry.getValue());
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("子加载器 {} 合并 {} 个表", loader.getClass().getSimpleName(), all.size());
                }
            } catch (Exception e) {
                logger.error("子加载器 {} 加载失败", loader.getClass().getSimpleName(), e);
                throw new RuntimeException("子加载器加载失败: " + loader.getClass().getSimpleName(), e);
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("组合加载完成，共加载 {} 个表", container.getTableNames().size());
        }
        return container;
    }
}
