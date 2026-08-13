package io.github.liuwww.db.config;

import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import io.github.liuwww.common.Idgen.DatabaseAutoIncrementIdGenerator;
import io.github.liuwww.common.Idgen.IdGenerator;
import io.github.liuwww.common.Idgen.NanoIdGenerator;
import io.github.liuwww.common.Idgen.SequentialUUIDGenerator;
import io.github.liuwww.common.Idgen.SnowflakeIdGenerator;
import io.github.liuwww.common.Idgen.UUIDGenerator;
import io.github.liuwww.db.config.impl.GenericDatabaseConfigCenter;
import io.github.liuwww.db.config.impl.JsonTableStructureLoader;
import io.github.liuwww.db.config.TableStructureLoaderType;
import io.github.liuwww.db.config.TableStructureLoader;
import io.github.liuwww.db.context.CoreDbContext;
import io.github.liuwww.db.context.DbContext;
import io.github.liuwww.db.dao.IQueryDao;
import io.github.liuwww.db.dao.impl.SpringQueryDao;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.executor.SpringDatabaseExecutor;
import io.github.liuwww.db.service.IDataTemplate;
import io.github.liuwww.db.service.IQueryTemplate;
import io.github.liuwww.db.service.impl.DataTemplate;
import io.github.liuwww.db.service.impl.QueryTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Template JDBC Spring 自动配置类。
 * <p>
 * 引入 {@code template-jdbc-spring} 依赖后自动装配，无需手动 {@code @Import}。
 * 所有核心 Bean 均通过 {@code @ConditionalOnMissingBean} 保护，用户可自定义覆盖。
 * </p>
 * <p>
 * 表结构加载延迟到所有 Bean 初始化完成后执行（{@link SmartInitializingSingleton}），
 * 避免在 Bean 创建阶段数据库表尚未就绪的问题。
 * </p>
 * <p>
 * 可通过 {@code application.properties} 配置参数，前缀 {@code template-jdbc}，
 * 详见 {@link TemplateJdbcProperties}。
 * </p>
 *
 * @author liuwww
 * @date 2026-03-01
 */
@Configuration
@ConditionalOnClass({DataSource.class, JdbcTemplate.class})
@EnableConfigurationProperties(TemplateJdbcProperties.class)
public class TemplateJdbcConfig implements SmartInitializingSingleton, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(TemplateJdbcConfig.class);

    @Autowired
    private TemplateJdbcProperties properties;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean(JdbcTemplate.class)
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean(DatabaseExecutor.class)
    public SpringDatabaseExecutor databaseExecutor(JdbcTemplate jdbcTemplate) {
        return new SpringDatabaseExecutor(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(IQueryDao.class)
    public IQueryDao queryDao() {
        return new SpringQueryDao();
    }

    @Bean
    @ConditionalOnMissingBean(GenericDatabaseConfigCenter.class)
    public GenericDatabaseConfigCenter databaseConfigCenter(
            ConfigurableListableBeanFactory beanFactory, DatabaseExecutor executor) {
        GenericDatabaseConfigCenter configCenter = new GenericDatabaseConfigCenter();
        configCenter.setDatabaseExecutor(executor);

        // 自动注册所有 DataSource bean，用 bean name 作为标识
        String[] dsNames = beanFactory.getBeanNamesForType(DataSource.class);
        for (String dsName : dsNames) {
            DataSource ds = beanFactory.getBean(dsName, DataSource.class);
            configCenter.addDataSource(ds, dsName);
        }

        // 不在此处 init()，延迟到 afterSingletonsInstantiated() 执行
        return configCenter;
    }

    @Bean
    @ConditionalOnMissingBean(CoreDbContext.class)
    public CoreDbContext coreDbContext(GenericDatabaseConfigCenter configCenter, DatabaseExecutor executor) {
        CoreDbContext context = new CoreDbContext(configCenter);
        context.setDatabaseExecutor(executor);
        DbContext.setCurrentContext(context);
        return context;
    }

    @Bean
    @ConditionalOnMissingBean(IDataTemplate.class)
    public IDataTemplate dataTemplate(DatabaseConfigCenter configCenter, Optional<IdGenerator> idGenerator) {
        return new DataTemplate(configCenter, idGenerator.orElse(null));
    }

    @Bean
    @ConditionalOnMissingBean(IQueryTemplate.class)
    public IQueryTemplate queryTemplate(DatabaseConfigCenter configCenter, IQueryDao queryDao) {
        return new QueryTemplate(configCenter, queryDao);
    }

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public IdGenerator idGenerator() {
        return createIdGenerator(properties.getIdGenerator());
    }

    /**
     * 所有单例 Bean 初始化完成后，配置 loaderType 并加载表结构。
     * <p>
     * 延迟加载避免了 Bean 创建阶段数据库表尚未就绪的问题。
     * 如果数据库表在应用启动后才创建（如测试环境），可手动调用
     * {@code DatabaseConfigCenter.refresh()} 重新加载。
     * </p>
     */
    @Override
    public void afterSingletonsInstantiated() {
        if (logger.isInfoEnabled()) {
            logger.info("开始加载表结构，loaderType={}", properties.getLoaderType());
        }
        GenericDatabaseConfigCenter configCenter = applicationContext.getBean(GenericDatabaseConfigCenter.class);
        configureLoader(configCenter);
        configCenter.init();
        if (logger.isInfoEnabled()) {
            logger.info("表结构加载完成，共 {} 个表", configCenter.getTableNames().size());
        }
    }

    private void configureLoader(GenericDatabaseConfigCenter configCenter) {
        String loaderType = properties.getLoaderType();
        if (loaderType == null) {
            return;
        }
        switch (loaderType.toLowerCase()) {
            case "json":
                configureJsonLoader(configCenter);
                configCenter.setCurrentLoaderType(TableStructureLoaderType.JSON);
                break;
            case "composite":
                configCenter.setCurrentLoaderType(TableStructureLoaderType.COMPOSITE);
                break;
            case "datasource":
            default:
                configCenter.setCurrentLoaderType(TableStructureLoaderType.DATASOURCE);
                break;
        }
    }

    private void configureJsonLoader(GenericDatabaseConfigCenter configCenter) {
        TableStructureLoader loader = configCenter.getLoader(TableStructureLoaderType.JSON);
        if (!(loader instanceof JsonTableStructureLoader)) {
            return;
        }
        JsonTableStructureLoader jsonLoader = (JsonTableStructureLoader) loader;
        TemplateJdbcProperties.JsonConfig jsonConfig = properties.getJson();

        // 多数据源模式
        Map<String, String> datasources = jsonConfig.getDatasources();
        if (datasources != null && !datasources.isEmpty()) {
            jsonLoader.setDataSourceJsonSources(datasources);
            return;
        }

        // 单数据源模式（退化处理）
        if (jsonConfig.getClasspath() != null && !jsonConfig.getClasspath().trim().isEmpty()) {
            jsonLoader.setClasspathResource(jsonConfig.getClasspath());
        }
        if (jsonConfig.getFile() != null && !jsonConfig.getFile().trim().isEmpty()) {
            jsonLoader.setFilePath(jsonConfig.getFile());
        }
    }

    private IdGenerator createIdGenerator(TemplateJdbcProperties.IdGeneratorConfig config) {
        String type = config.getType();
        if (type == null) {
            type = "snowflake";
        }
        switch (type.toLowerCase()) {
            case "snowflake": {
                TemplateJdbcProperties.SnowflakeConfig sc = config.getSnowflake();
                return new SnowflakeIdGenerator(sc.getWorkerId(), sc.getDatacenterId(),
                        sc.getIdShift(), sc.getTwepoch());
            }
            case "uuid":
                return new UUIDGenerator();
            case "sequential-uuid":
                return new SequentialUUIDGenerator();
            case "nano-id": {
                TemplateJdbcProperties.NanoIdConfig nc = config.getNanoId();
                return new NanoIdGenerator(nc.getSize());
            }
            case "database-auto-increment":
                return new DatabaseAutoIncrementIdGenerator();
            default:
                if (logger.isWarnEnabled()) {
                    logger.warn("未知的 ID 生成器类型: {}，回退到 snowflake", type);
                }
                return new SnowflakeIdGenerator(0, 0);
        }
    }
}
