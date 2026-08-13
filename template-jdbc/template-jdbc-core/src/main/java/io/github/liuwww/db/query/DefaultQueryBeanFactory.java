package io.github.liuwww.db.query;

import java.util.List;

import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.db.config.DatabaseConfigCenter;
import io.github.liuwww.db.context.DbContext;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.dao.IQueryDao;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.sql.Field;
import io.github.liuwww.db.sql.SqlBean;
import io.github.liuwww.db.sql.SqlBeanUtil;
import io.github.liuwww.db.sql.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的 QueryBeanFactory 实现。
 * <p>
 * 本类通过若干 {@code protected} 方法暴露扩展点，子类（如 {@code QueryTemplate}）
 * 可以通过注入 {@link DatabaseConfigCenter} 来覆盖默认行为，避免对静态 {@link DbContext}
 * 的强依赖，从而更好地支持多数据源场景和单元测试。
 * </p>
 * <p>
 * 主要扩展点：
 * <ul>
 *   <li>{@link #getConfigCenter()} - 获取配置中心，默认回退到 {@link DbContext}</li>
 *   <li>{@link #getTableMetaData(String)} - 获取表元数据</li>
 *   <li>{@link #resolveDatabaseExecutor(DatabaseExecutor)} - 解析数据库执行器</li>
 * </ul>
 * </p>
 *
 * @author liuwww
 */
public class DefaultQueryBeanFactory implements QueryBeanFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultQueryBeanFactory.class);

    protected IQueryDao queryDao;

    /**
     * 设置查询 DAO，用于测试或手动装配场景
     *
     * @param queryDao 查询 DAO
     */
    public void setQueryDao(IQueryDao queryDao) {
        this.queryDao = queryDao;
    }

    protected IQueryDao getQueryDao() {
        return queryDao;
    }

    // ===== 扩展点：子类可重载以下方法以支持多数据源、依赖注入等场景 =====

    /**
     * 获取数据库配置中心。
     * <p>
     * 默认实现回退到 {@link DbContext} 的静态上下文，便于在无 Spring 环境下使用。
     * 子类应重载此方法，返回通过构造函数注入的 {@link DatabaseConfigCenter} 实例，
     * 以避免对全局静态状态的依赖。
     * </p>
     *
     * @return 配置中心实例；若未配置则返回 null
     */
    protected DatabaseConfigCenter getConfigCenter() {
        try {
            return DbContext.getConfigCenter();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("获取 DatabaseConfigCenter 失败: {}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * 获取表元数据。
     * <p>
     * 默认实现通过 {@link #getConfigCenter()} 获取配置中心，再从中读取表元数据。
     * 子类可重载以支持按数据源路由等更复杂的场景。
     * </p>
     *
     * @param tableName 表名
     * @return 表元数据；若不存在则返回 null
     */
    protected TableMetaData getTableMetaData(String tableName) {
        DatabaseConfigCenter configCenter = getConfigCenter();
        if (configCenter != null) {
            return configCenter.getTableMetaData(tableName);
        }
        return null;
    }

    /**
     * 解析数据库执行器。
     * <p>
     * 当调用方传入非 null 的 {@code executor} 时，直接返回该执行器；
     * 否则从 {@link #getConfigCenter()} 获取默认执行器。
     * 子类可重载以支持更复杂的多数据源路由策略。
     * </p>
     *
     * @param executor 调用方指定的执行器，可为 null
     * @return 解析后的执行器；若都无法获取则返回 null
     */
    protected DatabaseExecutor resolveDatabaseExecutor(DatabaseExecutor executor) {
        if (executor != null) {
            return executor;
        }
        DatabaseConfigCenter configCenter = getConfigCenter();
        if (configCenter != null) {
            return configCenter.getDatabaseExecutor();
        }
        return null;
    }

    /**
     * 将解析后的执行器应用到 SqlBean（若 SqlBean 尚未设置执行器）。
     *
     * @param bean             SqlBean 实例
     * @param databaseExecutor 调用方指定的执行器，可为 null
     */
    protected void applyDatabaseExecutor(SqlBean bean, DatabaseExecutor databaseExecutor) {
        if (bean == null) {
            return;
        }
        if (bean.getDatabaseExecutor() != null) {
            // SqlBean 已有执行器，保留不动
            return;
        }
        DatabaseExecutor resolved = resolveDatabaseExecutor(databaseExecutor);
        if (resolved != null) {
            bean.setDatabaseExecutor(resolved);
        }
    }

    // ===== QueryBeanFactory 接口实现 =====

    @Override
    public <T> QueryBean createQueryBean(TableEntity<T> entity) {
        return createQueryBean(entity, null);
    }

    @Override
    public QueryBean createQueryBean(String fileName, String tag) {
        return createQueryBean(fileName, tag, (DatabaseExecutor) null, (Object[]) null);
    }

    @Override
    public QueryBean createQueryBean(String fileName, String tag, Object... params) {
        return createQueryBean(fileName, tag, null, params);
    }

    @Override
    public QueryBean createQueryBean(String tableName) {
        return createQueryBean(tableName, (DatabaseExecutor) null);
    }

    @Override
    public QueryBean createQueryBean(SqlBean sqlBean) {
        // 对裸 SqlBean 也尝试补充执行器，便于直接执行
        applyDatabaseExecutor(sqlBean, null);
        return new DefaultQueryBean(getQueryDao(), sqlBean);
    }

    @Override
    public <T> QueryBean createQueryBean(TableEntity<T> entity, DatabaseExecutor databaseExecutor) {
        TableMetaData tmd = getTableMetaData(entity.tableName());
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, databaseExecutor, null, tmd);
        // 即便 getSqlBean 内部已设置 executor，这里仍兜底处理
        applyDatabaseExecutor(bean, databaseExecutor);
        DefaultQueryBean queryBean = new DefaultQueryBean(getQueryDao(), bean);
        queryBean.setTableQuery(true, tmd);
        return queryBean;
    }

    @Override
    public QueryBean createQueryBean(String file, String tag, DatabaseExecutor databaseExecutor) {
        return createQueryBean(file, tag, databaseExecutor, (Object[]) null);
    }

    @Override
    public QueryBean createQueryBean(String file, String tag, DatabaseExecutor databaseExecutor, Object... params) {
        SqlBean bean = SqlBeanUtil.getOriginalSqlBean(file, tag);
        bean.setParams(params);
        applyDatabaseExecutor(bean, databaseExecutor);
        return new DefaultQueryBean(getQueryDao(), bean);
    }

    @Override
    public QueryBean createQueryBean(String tableName, DatabaseExecutor databaseExecutor) {
        TableMetaData tmd = getTableMetaData(tableName);
        SqlBean bean = SqlBeanUtil.getSqlBean(tableName, null, null, tmd);
        bean.setTables(new String[] { tableName });
        applyDatabaseExecutor(bean, databaseExecutor);
        DefaultQueryBean queryBean = new DefaultQueryBean(getQueryDao(), bean);
        queryBean.setTableQuery(true, tmd);
        return queryBean;
    }

    @Override
    public QueryBean createQueryBean(Table table) {
        TableMetaData tmd = table.getTableMetaData();
        if (tmd == null) {
            tmd = getTableMetaData(table.getName());
        }
        SqlBean bean = SqlBeanUtil.getSqlBean(table, tmd);
        // Table 可能自带 executor；若未带则从配置中心兜底
        applyDatabaseExecutor(bean, table.getDatabaseExecutor());
        DefaultQueryBean queryBean = new DefaultQueryBean(getQueryDao(), bean);
        queryBean.setTableQuery(true, tmd);
        return queryBean;
    }

    @Override
    public QueryBean createQueryBean(String tableName, Field... fields) {
        return createQueryBean(tableName, (DatabaseExecutor) null, fields);
    }

    @Override
    public QueryBean select(String tableName, String... fields) {
        if (fields == null || fields.length == 0) {
            return createQueryBean(tableName, (Field[]) null);
        }
        Field[] fieldArr = new Field[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldArr[i] = new Field(fields[i]);
        }
        return createQueryBean(tableName, fieldArr);
    }

    @Override
    public QueryBean createQueryBean(String tableName, DatabaseExecutor databaseExecutor, Field... fields) {
        List<Field> fieldList = (fields == null || fields.length == 0) ? null : java.util.Arrays.asList(fields);
        TableMetaData tmd = getTableMetaData(tableName);
        SqlBean bean = SqlBeanUtil.getSqlBean(tableName, fieldList, tmd);
        applyDatabaseExecutor(bean, databaseExecutor);
        DefaultQueryBean queryBean = new DefaultQueryBean(getQueryDao(), bean);
        queryBean.setTableQuery(true, tmd);
        return queryBean;
    }
}
