package org.liuwww.db.query.conditon;

import org.liuwww.db.condition.AbstractCompare;
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

    public TableMetaData getTmd()
    {
        return tmd;
    }

    public void setTmd(TableMetaData tmd)
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
