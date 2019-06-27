package org.liuwww.db.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.liuwww.db.page.Page;
import org.liuwww.db.query.Entity;
import org.liuwww.db.query.QueryBeanFactory;

/**
 * 普通通用查询service 接口<br/>
 * 通过读取指定配置的sql与给定的条件重组sql语句，然后查询出所需的数据
 * @author liuwww 2017年4月27日下午3:02:32
 */
public interface IQueryTemplate extends QueryBeanFactory
{

    /**
     * @Desc:根据bean的值进行等着查询
     * @Date 2017年5月25日下午5:29:26
     * @author liuwww
     * @param entity
     * @return
     */
    public <T> List<T> getBeanList(Entity<T> entity);

    /**
     * @Desc:根据bean的值进行等着查询
     * @Date 2017年5月25日下午5:29:26
     * @author liuwww
     * @param entity
     * @return
     */
    public <T> T getBean(Entity<T> entity);

    /**
     * @Desc:分页查询，rows中的范型类型为entity类型
     * @Date 2017年5月25日下午5:54:12
     * @author liuwww
     * @param entity
     * @param pageInfo
     * @return
     */
    public <T> Page getPage(Entity<T> entity, Page page);

    /**
     * @Desc:按照表或视图分页查询，rows中的范型类型为map类型
     * @Date 2017年6月5日下午2:28:44
     * @author liuwww
     * @param tableName 表或者视图名称
     * @param paramMap 参数
     * @return
     */
    public Page getPage(String tableName, Map<String, Object> paramMap);

    /**
     * @Desc:按照表或视图分页查询
     * @Date 2017年6月5日下午3:04:54
     * @author liuwww
     * @param tableName 表或者视图名称
     * @param pageInfo
     * @param paramMap 参数 参数为表字段或者视图字段（表的话，字段可以是数据库字段或者实体字段，视图的话必须是视图的字段 ）
     * @return
     */
    public Page getPage(String tableName, Page pageInfo, Map<String, Object> paramMap);

    /**
     * @Desc:
     * @Date 2017年6月5日下午3:10:43
     * @author liuwww
     * @param tableName
     * @param paramMap
     * @return
     */
    public List<Map<String, Object>> getMapList(String tableName, Map<String, Object> paramMap);

    /**
     * @Desc:查询个数
     * @Date 2017年5月27日下午1:52:55
     * @author liuwww
     * @param entity
     * @return
     */
    public <T> long getCount(Entity<T> entity);

    /**
     * @Desc:通过id查询
     * @Date 2018年3月7日上午10:05:57
     * @author liuwww
     * @param tableName
     * @param id
     * @return
     */
    public Map<String, Object> getMap(String tableName, Serializable id);

    /**
     * @Desc:通过id查询
     * @Date 2018年3月7日上午10:06:54
     * @author liuwww
     * @param tableName
     * @param id
     * @param clazz
     */
    public <T> T getBean(String tableName, Serializable id, Class<T> clazz);

}
