package org.liuwww.db.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.liuwww.db.condition.Condition;
import org.liuwww.db.context.DbContext;
import org.liuwww.db.dao.IQueryDao;
import org.liuwww.db.page.Page;
import org.liuwww.db.query.conditon.QueryBeanCompare;
import org.liuwww.db.query.conditon.QueryBeanConditions;
import org.liuwww.db.sql.DefaultSqlBean;
import org.liuwww.db.sql.SqlBean;
import org.liuwww.db.sql.SqlBeanUtil;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.PagerUtils;

import org.liuwww.common.entity.Order;
import org.liuwww.common.execption.DbException;
import org.liuwww.common.util.DbNameConverter;
import org.liuwww.common.util.StringUtil;

public class DefaultQueryBean extends DefaultSqlBean implements QueryBean, SqlBean
{
    protected IQueryDao queryDao;

    protected QueryBeanCompare conditions;

    public DefaultQueryBean(String sql, Object[] params)
    {
        super(sql, params);
        setConditions();
    }

    public DefaultQueryBean(IQueryDao queryDao, SqlBean bean)
    {
        super(null, null, null, bean.getDbType(), bean.getTables(), bean.getJdbcTemplate());
        this.queryDao = queryDao;
        if (bean instanceof DefaultSqlBean)
        {
            DefaultSqlBean dbean = (DefaultSqlBean) bean;
            this.sql = dbean.getOriginalSql();
            this.orderBy = dbean.getOriginalOrderby();
            this.params = dbean.getParamsList();
        }
        else
        {
            setOrderBy(bean.getOrderBy());
            setSql(bean.getSql());
            setParams(bean.getParams());
        }
        setConditions();
    }

    protected SqlBean assemblySqlBean(Order order, int maxNum)
    {

        StringBuilder sql = new StringBuilder(this.sql);
        StringBuilder orderBy = this.orderBy;
        if (conditions.size() > 0)
        {
            for (Condition c : conditions)
            {
                if (c.isValid())
                {
                    sql.append(" ").append(c.getSqlFragment(dbType));
                }
            }
        }

        String groupBy = getGroupBy();
        if (StringUtil.isNotBlank(groupBy))
        {
            sql.append(" group by ").append(groupBy);
        }

        if (this.orderBy != null && this.orderBy.length() > 0)
        {
            sql.append(" order by ").append(orderBy);
            if (order != null)
            {
                SqlBeanUtil.addSqlOrderBy(sql, order, tmd, true);
            }
        }
        else
        {
            if (order != null)
            {
                SqlBeanUtil.addSqlOrderBy(sql, order, tmd, false);
            }
        }

        List<Object> params = null;
        List<Object> cparams = conditions.getParamList();

        if (this.params != null)
        {
            params = new ArrayList<Object>(this.params);
        }
        if (cparams != null)
        {
            if (params == null)
            {
                params = new ArrayList<Object>(cparams);
            }
            else
            {
                params.addAll(cparams);
            }
        }

        CharSequence sql_ = sql;
        if (maxNum > 0)
        {
            DbType type = DbType.of(dbType.toString());
            sql_ = PagerUtils.limit(sql.toString(), type, 0, maxNum);
        }
        return new DefaultSqlBean(sql_, params, orderBy, dbType, tables, jdbcTemplate);

    }

    public IQueryDao getQueryDao()
    {
        return queryDao;
    }

    public void setQueryDao(IQueryDao queryDao)
    {
        this.queryDao = queryDao;
    }

    protected void setConditions()
    {
        this.conditions = new QueryBeanConditions(this);
    }

    @Override
    public <T> T getBean(Class<T> clazz)
    {
        return getQueryDao().getBean(assemblySqlBean(null, 0), clazz);
    }

    @Override
    public <T> List<T> getBeanList(Class<T> clazz)
    {
        return getBeanList(clazz, 0);
    }

    @Override
    public Map<String, Object> getMap()
    {
        return getQueryDao().getMap(assemblySqlBean(null, 0));
    }

    @Override
    public List<Map<String, Object>> getMapList()
    {
        return getMapList(0);
    }

    @Override
    public Page getPage(Page page)
    {
        return getQueryDao().getPage(assemblySqlBean(new Order(page.getSortName(), page.getSortOrder()), 0), page);
    }

    @Override
    public QueryBean addOrderBy(String orderBy)
    {
        if (orderBy != null)
        {
            if (this.tmd != null)
            {
                orderBy = DbNameConverter.convertToDb(orderBy);
            }
            if (this.orderBy == null)
            {
                this.orderBy = new StringBuilder(orderBy);
            }
            else if (this.orderBy.length() == 0)
            {
                this.orderBy.append(orderBy);
            }
            else
            {
                this.orderBy.append(",").append(orderBy);
            }
        }
        return this;
    }

    @Override
    public QueryBeanCompare getCompare()
    {
        return this.conditions;
    }

    @Override
    public long getCount()
    {
        return getQueryDao().getCount(assemblySqlBean(null, 0));
    }

    public void setTableQuery(boolean isOneTable)
    {
        QueryBeanConditions qconditons = (QueryBeanConditions) this.conditions;
        qconditons.setOne(isOneTable);
        if (isOneTable)
        {
            if (tables == null || tables.length == 0)
            {
                throw new DbException("单表查询或单视图查询的关联表未设置！");
            }
            tmd = DbContext.getTableMetaContext().getTableMetaData(tables[0]);
            qconditons.setTmd(tmd);
        }
        else
        {
            qconditons.setOne(false);
            qconditons.setTmd(null);
        }
    }

    @Override
    public QueryBean setTheOrderBy(String orderBy)
    {
        setOrderBy(orderBy);
        return this;
    }

    @Override
    public <T> Page getPage(Page page, Class<T> clazz)
    {
        return getQueryDao().getPage(assemblySqlBean(new Order(page.getSortName(), page.getSortOrder()), 0), page,
                clazz);
    }

    @Override
    public SqlBean getSqlBean()
    {
        return this;
    }

    @Override
    public <T> List<T> getBeanList(Class<T> clazz, int maxNum)
    {
        return getQueryDao().getBeanList(assemblySqlBean(null, maxNum), clazz);
    }

    @Override
    public List<Map<String, Object>> getMapList(int maxNum)
    {
        return getQueryDao().getList(assemblySqlBean(null, maxNum));
    }

}
