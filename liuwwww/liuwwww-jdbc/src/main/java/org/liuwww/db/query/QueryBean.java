package org.liuwww.db.query;

import java.util.List;
import java.util.Map;

import org.liuwww.db.page.Page;
import org.liuwww.db.query.conditon.QueryBeanCompare;
import org.liuwww.db.sql.SqlBean;

public interface QueryBean
{
    public <T> T getBean(Class<T> clazz);

    public <T> List<T> getBeanList(Class<T> clazz);

    public <T> List<T> getBeanList(Class<T> clazz, int maxNum);

    public Map<String, Object> getMap();

    public List<Map<String, Object>> getMapList();

    public List<Map<String, Object>> getMapList(int maxNum);

    public Page getPage(Page page);

    public <T> Page getPage(Page page, Class<T> clazz);

    public QueryBean setTheOrderBy(String orderBy);

    public QueryBean addOrderBy(String orderBy);

    public QueryBeanCompare getCompare();

    public int getCount();

    public SqlBean getSqlBean();
}
