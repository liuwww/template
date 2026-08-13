package io.github.liuwww.db.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.github.liuwww.db.config.DatabaseConfigCenter;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.dao.IQueryDao;
import io.github.liuwww.db.dao.impl.QueryDao;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.query.DefaultQueryBeanFactory;
import io.github.liuwww.db.service.IQueryTemplate;
import io.github.liuwww.db.sql.SqlBean;
import io.github.liuwww.db.sql.SqlBeanUtil;
import io.github.liuwww.common.entity.Order;
import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.common.execption.DbException;

/**
 * 查询模板实现类
 * <p>
 * 实现了 {@link IQueryTemplate} 接口，提供统一的数据库查询操作。
 * 支持实体对象和 Map 两种查询方式，支持分页查询和统计查询。
 * </p>
 * <p>
 * 通过注入 {@link DatabaseConfigCenter} 覆盖父类的默认行为，
 * 避免对静态 {@link io.github.liuwww.db.context.DbContext} 的依赖，
 * 从而在多线程、多数据源场景下无需切换全局上下文即可正常工作。
 * </p>
 *
 * @author liuwww
 * @since 2017-05-11
 */
public class QueryTemplate extends DefaultQueryBeanFactory implements IQueryTemplate {

    /** 查询数据访问 DAO */
    private IQueryDao queryDao;

    /** 数据库配置中心 */
    private DatabaseConfigCenter configCenter;

    public QueryTemplate() {
        this.queryDao = new QueryDao();
    }

    public QueryTemplate(IQueryDao queryDao) {
        this.queryDao = queryDao;
    }

    public QueryTemplate(DatabaseConfigCenter configCenter) {
        this.configCenter = configCenter;
        this.queryDao = new QueryDao();
    }

    public QueryTemplate(DatabaseConfigCenter configCenter, IQueryDao queryDao) {
        this.configCenter = configCenter;
        this.queryDao = queryDao;
    }

    /**
     * 覆盖父类方法，返回注入的 queryDao 实例。
     */
    @Override
    protected IQueryDao getQueryDao() {
        return this.queryDao;
    }

    /**
     * 覆盖父类方法，返回注入的配置中心，避免依赖静态 {@link io.github.liuwww.db.context.DbContext}。
     */
    @Override
    protected DatabaseConfigCenter getConfigCenter() {
        return this.configCenter;
    }

    /**
     * 覆盖父类方法，直接通过注入的配置中心获取表元数据，
     * 并在表不存在时抛出明确异常。
     */
    @Override
    protected TableMetaData getTableMetaData(String tableName) {
        if (this.configCenter == null) {
            throw new DbException("DatabaseConfigCenter 未初始化，请通过构造函数注入");
        }
        TableMetaData tmd = this.configCenter.getTableMetaData(tableName);
        if (tmd == null) {
            throw new DbException("表[" + tableName + "]不存在或未加载元数据");
        }
        return tmd;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getBeanList(TableEntity<T> entity) {
        TableMetaData tmd = getTableMetaData(entity.tableName());
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null, null, tmd);
        checkSqlBeanDatabaseExecutor(entity.tableName(), null, bean);
        return (List<T>) queryDao.getBeanList(bean, entity.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(TableEntity<T> entity) {
        TableMetaData tmd = getTableMetaData(entity.tableName());
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null, null, tmd);
        checkSqlBeanDatabaseExecutor(entity.tableName(), null, bean);
        return (T) queryDao.getBean(bean, entity.getClass());
    }

    @Override
    public <T> Page getPage(TableEntity<T> entity, Page page) {
        TableMetaData tmd = getTableMetaData(entity.tableName());
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null, new Order(page.getSortName(), page.getSortOrder()), tmd);
        checkSqlBeanDatabaseExecutor(entity.tableName(), null, bean);
        return queryDao.getPage(bean, page, entity.getClass());
    }

    @Override
    public <T> long getCount(TableEntity<T> entity) {
        TableMetaData tmd = getTableMetaData(entity.tableName());
        SqlBean bean = SqlBeanUtil.getSqlBean(entity, null, null, tmd);
        checkSqlBeanDatabaseExecutor(entity.tableName(), null, bean);
        return queryDao.getCount(bean);
    }

    @Override
    public Page getPage(String tableName, Map<String, Object> paramMap) {
        Page pageInfo = new Page();
        Object pageNum = paramMap.get("pageNum");
        if (pageNum != null) {
            int _page = Integer.parseInt(pageNum.toString());
            pageInfo.setPageNum(_page);
        }
        Object pageSize = paramMap.get("pageSize");
        if (pageSize != null) {
            int _pageSize = Integer.parseInt(pageSize.toString());
            pageInfo.setPageSize(_pageSize);
        }
        return getPage(tableName, pageInfo, paramMap);
    }

    @Override
    public Page getPage(String tableName, Page pageInfo, Map<String, Object> paramMap) {
        TableMetaData tmd = getTableMetaData(tableName);
        SqlBean bean = SqlBeanUtil.getQuerySqlBean(tableName, paramMap, null, true, tmd);
        checkSqlBeanDatabaseExecutor(tableName, null, bean);
        return queryDao.getPage(bean, pageInfo);
    }

    @Override
    public Map<String, Object> getMap(String tableName, Serializable id) {
        TableMetaData tmd = getTableMetaData(tableName);
        SqlBean bean = SqlBeanUtil.getQuerySqlBean(tableName,
                java.util.Collections.singletonMap(tmd.getIdColumnName(), id), null, true, tmd);
        checkSqlBeanDatabaseExecutor(tableName, null, bean);
        return queryDao.getMap(bean);
    }

    @Override
    public <T> T getBean(String tableName, Serializable id, Class<T> clazz) {
        TableMetaData tmd = getTableMetaData(tableName);
        SqlBean bean = SqlBeanUtil.getQuerySqlBean(tableName,
                java.util.Collections.singletonMap(tmd.getIdColumnName(), id), null, true, tmd);
        checkSqlBeanDatabaseExecutor(tableName, null, bean);
        return queryDao.getBean(bean, clazz);
    }

    @Override
    public List<Map<String, Object>> getMapList(String tableName, Map<String, Object> condition) {
        TableMetaData tmd = getTableMetaData(tableName);
        SqlBean bean = SqlBeanUtil.getQuerySqlBean(tableName, condition, null, true, tmd);
        checkSqlBeanDatabaseExecutor(tableName, null, bean);
        return queryDao.getList(bean);
    }

    /**
     * 根据条件查询，返回实体对象列表
     */
    public <T> List<T> getList(String tableName, Map<String, Object> condition, Class<T> clazz) {
        TableMetaData tmd = getTableMetaData(tableName);
        SqlBean bean = SqlBeanUtil.getQuerySqlBean(tableName, condition, null, true, tmd);
        checkSqlBeanDatabaseExecutor(tableName, null, bean);
        return queryDao.getBeanList(bean, clazz);
    }

    /**
     * 根据条件查询记录数量
     */
    public long getCount(String tableName, Map<String, Object> condition) {
        TableMetaData tmd = getTableMetaData(tableName);
        SqlBean bean = SqlBeanUtil.getCountSqlBean(tableName, condition, tmd);
        checkSqlBeanDatabaseExecutor(tableName, null, bean);
        return queryDao.getCount(bean);
    }

    /**
     * 检查并设置 SqlBean 的 DatabaseExecutor。
     * <p>
     * 委托给父类 {@link #applyDatabaseExecutor} 完成实际工作，这里仅补充错误检查，
     * 确保 IQueryTemplate 上的查询方法在执行前一定有可用的执行器。
     * </p>
     *
     * @param tableName        表名
     * @param databaseExecutor 指定的数据库执行器
     * @param bean             SqlBean 对象
     * @throws DbException 当无法获取 DatabaseExecutor 实例时抛出
     */
    private void checkSqlBeanDatabaseExecutor(String tableName, DatabaseExecutor databaseExecutor, SqlBean bean) {
        applyDatabaseExecutor(bean, databaseExecutor);
        if (bean.getDatabaseExecutor() == null) {
            throw new DbException("无法获取 DatabaseExecutor 实例，表：" + tableName);
        }
    }
}
