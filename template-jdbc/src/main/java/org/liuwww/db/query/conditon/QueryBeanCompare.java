package org.liuwww.db.query.conditon;

import org.liuwww.db.condition.Compare;
import org.liuwww.db.query.QueryBean;

public interface QueryBeanCompare extends Compare<QueryBeanCompare>
{
    public QueryBean getQueryBean();
}
