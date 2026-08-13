package io.github.liuwww.db.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.liuwww.db.condition.Condition;
import io.github.liuwww.db.dao.IQueryDao;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.query.conditon.QueryBeanCompare;
import io.github.liuwww.db.query.conditon.QueryBeanConditions;
import io.github.liuwww.db.sql.DefaultSqlBean;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.SqlBean;
import io.github.liuwww.db.sql.SqlBeanUtil;
import io.github.liuwww.db.sql.util.PaginationFactory;
import io.github.liuwww.db.context.TableMetaData;

import io.github.liuwww.common.entity.Order;
import io.github.liuwww.common.execption.DbException;
import io.github.liuwww.common.util.DbNameConverter;
import io.github.liuwww.common.util.StringUtil;

public class DefaultQueryBean extends DefaultSqlBean implements QueryBean, SqlBean {
    protected IQueryDao queryDao;

    protected QueryBeanCompare conditions;

    public DefaultQueryBean(String sql, Object[] params) {
        super(sql, params);
        setConditions();
    }

    public DefaultQueryBean(IQueryDao queryDao, SqlBean bean) {
        super(null, null, null, bean.getDbType(), bean.getTables(), bean.getDatabaseExecutor());
        this.queryDao = queryDao;
        if (bean instanceof DefaultSqlBean) {
            DefaultSqlBean dbean = (DefaultSqlBean) bean;
            // 通过 concrete getter 获取原始可变对象，避免直接字段赋值
            // sql 和 params 改为拷贝（接口方法），消除共享可变状态的潜在风险
            StringBuilder origSql = dbean.getOriginalSql();
            if (origSql != null) {
                setSql(origSql.toString());
            }
            List<Object> origParams = dbean.getParamsList();
            if (origParams != null) {
                setParams(origParams.toArray());
            }
            // orderBy 保留原共享语义，通过 concrete setter 赋值
            setOrderBy(dbean.getOriginalOrderby());
        } else {
            setOrderBy(bean.getOrderBy());
            setSql(bean.getSql());
            setParams(bean.getParams());
        }
        // 通过接口方法拷贝表元数据
        setTableMetaData(bean.getTableMetaData());
        setConditions();
    }

    protected SqlBean assemblySqlBean(Order order, int maxNum) {

        // 通过 concrete getter 访问原始可变对象，避免直接字段读取
        StringBuilder origSql = getOriginalSql();
        StringBuilder sql = origSql != null ? new StringBuilder(origSql) : new StringBuilder();
        StringBuilder orderBy = getOriginalOrderby();
        if (conditions.size() > 0) {
            for (Condition c : conditions) {
                if (c.isValid()) {
                    sql.append(" ").append(c.getSqlFragment(getDbType()));
                }
            }
        }

        String groupBy = getGroupBy();
        if (StringUtil.isNotBlank(groupBy)) {
            sql.append(" group by ").append(groupBy);
        }

        if (orderBy != null && orderBy.length() > 0) {
            sql.append(" order by ").append(orderBy);
            if (order != null) {
                SqlBeanUtil.addSqlOrderBy(sql, order, getTableMetaData(), true);
            }
        } else {
            if (order != null) {
                SqlBeanUtil.addSqlOrderBy(sql, order, getTableMetaData(), false);
            }
        }

        List<Object> params = null;
        List<Object> cparams = conditions.getParamList();
        List<Object> origParams = getParamsList();

        if (origParams != null) {
            params = new ArrayList<Object>(origParams);
        }
        if (cparams != null) {
            if (params == null) {
                params = new ArrayList<Object>(cparams);
            } else {
                params.addAll(cparams);
            }
        }

        String sql_ = sql.toString();
        if (maxNum > 0) {
            sql_ = PaginationFactory.limit(sql.toString(), getDbType(), 0, maxNum);
        }
        DefaultSqlBean resultBean = new DefaultSqlBean(sql_, params, orderBy, getDbType(), getTables(), null);
        // 传递 DatabaseExecutor，确保查询执行时可用
        resultBean.setDatabaseExecutor(getDatabaseExecutor());
        resultBean.setTableMetaData(getTableMetaData());
        return resultBean;

    }

    public IQueryDao getQueryDao() {
        return queryDao;
    }

    public void setQueryDao(IQueryDao queryDao) {
        this.queryDao = queryDao;
    }

    protected void setConditions() {
        this.conditions = new QueryBeanConditions(this);
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return getQueryDao().getBean(assemblySqlBean(null, 0), clazz);
    }

    @Override
    public <T> List<T> getBeanList(Class<T> clazz) {
        return getBeanList(clazz, 0);
    }

    @Override
    public Map<String, Object> getMap() {
        return getQueryDao().getMap(assemblySqlBean(null, 0));
    }

    @Override
    public List<Map<String, Object>> getMapList() {
        return getMapList(0);
    }

    @Override
    public Page getPage(Page page) {
        return getQueryDao().getPage(assemblySqlBean(new Order(page.getSortName(), page.getSortOrder()), 0), page);
    }

    @Override
    public QueryBean addOrderBy(String orderBy) {
        if (orderBy != null) {
            if (getTableMetaData() != null) {
                // 仅对字段名进行下划线转换，保留排序方向（ASC/DESC）
                String trimmed = orderBy.trim();
                String fieldPart = trimmed;
                String directionPart = "";
                int lastSpace = trimmed.lastIndexOf(' ');
                if (lastSpace > 0) {
                    String tail = trimmed.substring(lastSpace + 1).toUpperCase(java.util.Locale.ROOT);
                    if ("ASC".equals(tail) || "DESC".equals(tail)) {
                        fieldPart = trimmed.substring(0, lastSpace).trim();
                        directionPart = " " + tail;
                    }
                }
                // 防止 SQL 注入：校验字段名合法性
                Order.validateField(fieldPart);
                orderBy = DbNameConverter.convertToDb(fieldPart) + directionPart;
            } else {
                // 无 tmd 时对完整表达式进行校验（允许逗号分隔多字段）
                validateOrderByExpression(orderBy);
            }
            // 通过 concrete getter 读取原始 StringBuilder，避免直接字段访问
            StringBuilder orderByBuilder = getOriginalOrderby();
            if (orderByBuilder == null) {
                setOrderBy(new StringBuilder(orderBy));
            } else if (orderByBuilder.length() == 0) {
                orderByBuilder.append(orderBy);
            } else {
                orderByBuilder.append(",").append(orderBy);
            }
        }
        return this;
    }

    @Override
    public QueryBeanCompare getCompare() {
        return this.conditions;
    }

    @Override
    public long getCount() {
        return getQueryDao().getCount(assemblySqlBean(null, 0));
    }

    public void setTableQuery(boolean isOneTable, TableMetaData tableMetaData) {
        QueryBeanConditions qconditons = (QueryBeanConditions) this.conditions;
        qconditons.setOne(isOneTable);
        if (isOneTable) {
            String[] tableArr = getTables();
            if (tableArr == null || tableArr.length == 0) {
                throw new DbException("单表查询或单视图查询的关联表未设置！");
            }
            if (tableMetaData == null) {
                throw new DbException("单表查询需要提供 TableMetaData，表：" + tableArr[0]);
            }
            setTableMetaData(tableMetaData);
            qconditons.setTableMetaData(getTableMetaData());
        } else {
            qconditons.setOne(false);
            qconditons.setTableMetaData(null);
        }
    }

    @Override
    public QueryBean setTheOrderBy(String orderBy) {
        if (orderBy != null) {
            // 防止 SQL 注入：校验排序表达式
            validateOrderByExpression(orderBy);
        }
        setOrderBy(orderBy);
        return this;
    }

    /**
     * 校验排序表达式合法性，防止 SQL 注入。
     * 支持逗号分隔的多字段，如 "name ASC, age DESC"。
     *
     * @param orderBy 排序表达式
     */
    private static void validateOrderByExpression(String orderBy) {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return;
        }
        String[] parts = orderBy.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String fieldPart = trimmed;
            int lastSpace = trimmed.lastIndexOf(' ');
            if (lastSpace > 0) {
                String tail = trimmed.substring(lastSpace + 1).toUpperCase(java.util.Locale.ROOT);
                if ("ASC".equals(tail) || "DESC".equals(tail)) {
                    fieldPart = trimmed.substring(0, lastSpace).trim();
                }
            }
            Order.validateField(fieldPart);
        }
    }

    @Override
    public <T> Page getPage(Page page, Class<T> clazz) {
        return getQueryDao().getPage(assemblySqlBean(new Order(page.getSortName(), page.getSortOrder()), 0), page,
                clazz);
    }

    @Override
    public SqlBean getSqlBean() {
        return this;
    }

    @Override
    public <T> List<T> getBeanList(Class<T> clazz, int maxNum) {
        return getQueryDao().getBeanList(assemblySqlBean(null, maxNum), clazz);
    }

    @Override
    public List<Map<String, Object>> getMapList(int maxNum) {
        return getQueryDao().getList(assemblySqlBean(null, maxNum));
    }

}
