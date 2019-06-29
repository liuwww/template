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

    /**
     * @desc:maxNum大于0时生效查询最多maxNum条数据，0或0以下查询所有数据
     * @author liuwww
     * @param clazz
     * @param maxNum
     * @return
     */
    public <T> List<T> getBeanList(Class<T> clazz, int maxNum);

    public Map<String, Object> getMap();

    public List<Map<String, Object>> getMapList();

    /**
     * @desc:maxNum大于0时生效查询最多maxNum条数据，0或0以下查询所有数据
     * @author liuwww
     * @param maxNum
     * @return
     */
    public List<Map<String, Object>> getMapList(int maxNum);

    public Page getPage(Page page);

    public <T> Page getPage(Page page, Class<T> clazz);

    public QueryBean setTheOrderBy(String orderBy);

    public QueryBean addOrderBy(String orderBy);

    public QueryBeanCompare getCompare();

    public long getCount();

    public SqlBean getSqlBean();
}
