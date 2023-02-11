package org.liuwww.db.query.conditon;

import org.liuwww.db.condition.AbstractCompare;
import org.liuwww.db.condition.CompareOpe;
import org.liuwww.db.condition.Condition;
import org.liuwww.db.condition.OneCondition;
import org.liuwww.db.condition.Condition.ConditionRel;
import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.query.QueryBean;
import org.liuwww.db.sql.Column;

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
