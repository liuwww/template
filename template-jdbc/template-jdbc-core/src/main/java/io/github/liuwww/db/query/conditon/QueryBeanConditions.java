package io.github.liuwww.db.query.conditon;

import io.github.liuwww.db.condition.AbstractCompare;
import io.github.liuwww.db.condition.CompareOpe;
import io.github.liuwww.db.condition.Condition;
import io.github.liuwww.db.condition.OneCondition;
import io.github.liuwww.db.condition.Condition.ConditionRel;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.query.QueryBean;
import io.github.liuwww.db.sql.Column;

public class QueryBeanConditions extends AbstractCompare<QueryBeanCompare> implements QueryBeanCompare
{
    protected QueryBean queryBean;

    protected TableMetaData tmd;

    protected boolean isOne = false;

    public QueryBeanConditions(QueryBean queryBean)
    {
        this.queryBean = queryBean;
    }

    public QueryBeanConditions(QueryBean queryBean, TableMetaData tmd, boolean isOne)
    {
        this.queryBean = queryBean;
        this.tmd = tmd;
        this.isOne = isOne;
    }

    @Override
    protected String getColumn(String field)
    {
        if (!isOne || tmd == null)
        {
            return field;
        }
        Column column = tmd.getColumn(field);
        if (column == null)
        {
            return null;
        }
        return column.getColumnName();
    }

    @Override
    protected Condition getCondition(String field, CompareOpe ope, Object val, ConditionRel rel)
    {
        Condition condition = super.getCondition(field, ope, val, rel);
        if (condition != null)
        {
            if (!isOne || tmd == null)
            {
                if (condition instanceof OneCondition)
                {
                    ((OneCondition) condition).setColumnField(false);
                }
            }
        }
        return condition;
    }

    @Override
    public TableMetaData getTableMetaData()
    {
        return tmd;
    }

    public void setTableMetaData(TableMetaData tmd)
    {
        this.tmd = tmd;
    }

    public boolean isOne()
    {
        return isOne;
    }

    public void setOne(boolean isOne)
    {
        this.isOne = isOne;
    }

    @Override
    public QueryBean getQueryBean()
    {
        return this.queryBean;
    }

}
