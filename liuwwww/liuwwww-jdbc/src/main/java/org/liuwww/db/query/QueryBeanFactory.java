package org.liuwww.db.query;

import org.liuwww.db.sql.SqlBean;
import org.liuwww.db.sql.Table;
import org.springframework.jdbc.core.JdbcTemplate;

public interface QueryBeanFactory
{
    /**
     * @desc：根据实体类创建基于表的查询
     * @Date:2019年2月21日上午10:37:29
     * @author liuwww
     * @param entity实体类对应数据库表
     * @return
     */
    public <T> QueryBean createQueryBean(Entity<T> entity);

    /**
     * @desc:根据实体类创建基于表的查询，在多数据源的环境下，可以指定数据源
     * @Date:2019年2月21日上午10:38:30
     * @author liuwww
     * @param entity
     * @param jdbcTemplate 
     * @return
     */
    public <T> QueryBean createQueryBean(Entity<T> entity, JdbcTemplate jdbcTemplate);

    /**
     * @desc:根据给定的sql创建查询
     * @Date:2019年2月21日上午10:40:20
     * @author liuwww
     * @param file classpath下sql文件夹中XML的filename,不包含后缀
     * @param tag XML中sql所在的标签name
     * @return
     */
    public QueryBean createQueryBean(String file, String tag);

    /**
     * @desc:根据给定的sql创建查询，在多数据源的环境下，可以指定数据源
     * @Date:2019年2月21日上午10:43:44
     * @author liuwww
     * @param file classpath下sql文件夹中XML的filename,不包含后缀
     * @param tag XML中sql所在的标签name
     * @param jdbcTemplate
     * @return
     */
    public QueryBean createQueryBean(String file, String tag, JdbcTemplate jdbcTemplate);

    /**
     * @desc:根据给定的sql创建查询
     * @Date:2019年2月21日上午10:54:38
     * @author liuwww
     * @param file classpath下sql文件夹中XML的filename,不包含后缀
     * @param tag XML中sql所在的标签name
     * @param params 给定sql中的参数
     * @return
     */
    public QueryBean createQueryBean(String file, String tag, Object... params);

    /**
     * @desc:根据给定的sql创建查询,在多数据源环境下可以指定数据源
     * @Date:2019年2月21日上午10:55:39
     * @author liuwww
     * @param file classpath下sql文件夹中XML的filename,不包含后缀
     * @param tag XML中sql所在的标签name
     * @param params 给定sql中的参数
     * @param jdbcTemplate
     * @return
     */
    public QueryBean createQueryBean(String file, String tag, JdbcTemplate jdbcTemplate, Object... params);

    /**
     * @desc:根据给定的表或视图创建查询
     * @Date:2019年2月27日上午9:51:57
     * @author liuwww
     * @param tableName 表名或视图名
     * @return
     */
    public QueryBean createQueryBean(String tableName);

    /**
     * @desc:根据给定的表或视图创建查询，在多数据源环境下可以指定数据源
     * @Date:2019年2月27日上午9:51:57
     * @author liuwww
     * @param tableName 表名或视图名
     * @return
     */
    public QueryBean createQueryBean(String tableName, JdbcTemplate jdbcTemplate);

    /**
     * @desc:直接根据给定的具体的查询条件创建查询
     * @Date:2019年2月27日上午9:54:20
     * @author liuwww
     * @param sqlBean
     * @return
     */
    public QueryBean createQueryBean(SqlBean sqlBean);

    /**
     * @desc:根据给定的表或视图创建查询，可以带条件，可以指定查询的字段
     * @Date:2019年2月27日上午9:55:44
     * @author liuwww
     * @param table
     * @return
     */
    public QueryBean createQueryBean(Table table);
}
