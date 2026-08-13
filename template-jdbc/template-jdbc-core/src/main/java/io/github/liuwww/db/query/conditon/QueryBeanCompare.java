package io.github.liuwww.db.query.conditon;

import io.github.liuwww.db.condition.Compare;
import io.github.liuwww.db.query.QueryBean;

public interface QueryBeanCompare extends Compare<QueryBeanCompare>
{
    public QueryBean getQueryBean();
}
