package org.liuwww.db.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.liuwww.db.sql.Row;
import org.springframework.jdbc.core.JdbcTemplate;
import org.liuwww.common.entity.TableEntity;
import org.liuwww.common.execption.DbException;

public interface IDataTemplate
{
    /**
     * @Desc:插入单条数据
     * @Date 2017年5月11日下午5:23:38
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @return
     * @throws DbException
     */
    public Row insert(String tableName, Map<String, Object> fieldVals) throws DbException;

    /**
     * @Desc:插入单条数据,可以指定数据源
     * @Date 2017年5月11日下午5:23:38
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public Row insert(String tableName, Map<String, Object> fieldVals, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc:插入多条数据
     * @Date 2017年5月11日下午5:23:38
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @return
     * @throws DbException
     */
    public List<Row> insert(String tableName, List<Map<String, Object>> fieldValList) throws DbException;

    /**
     * @Desc:插入多条数据,可以指定数据源
     * @Date 2017年5月11日下午5:23:38
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public List<Row> insert(String tableName, List<Map<String, Object>> fieldValList, JdbcTemplate jdbcTemplate)
            throws DbException;

    /**
     * @Desc:插入单条数据
     * @Date 2017年5月19日下午5:32:23
     * @author liuwww
     * @param entity
     * @return
     * @throws DbException
     */
    public <T> T insert(TableEntity<T> entity) throws DbException;

    /**
     * @Desc:插入单条数据，可以指定数据源
     * @Date 2017年5月19日下午5:32:23
     * @author liuwww
     * @param entity
     * @param jdbcTemplate
     * @return 
     * @throws DbException
     */
    public <T> T insert(TableEntity<T> entity, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc:插入多条数据
     * @Date 2017年5月19日下午5:36:16
     * @author liuwww
     * @param entityList
     * @throws DbException
     */
    public <T> List<T> insert(List<? extends TableEntity<T>> entityList) throws DbException;

    /**
     * @Desc:插入多条数据,可以指定数据源
     * @Date 2017年5月19日下午5:36:16
     * @author liuwww
     * @param entityList
     * @param jdbcTemplate
     * @throws DbException
     */
    public <T> List<T> insert(List<? extends TableEntity<T>> entityList, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc:根据id更新单条数据
     * @Date 2017年5月11日下午5:23:55
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @return
     * @throws DbException
     */
    public int update(String tableName, Map<String, Object> fieldVals) throws DbException;

    /**
     * @Desc:根据id更新单条数据，可以指定数据源
     * @Date 2017年5月11日下午5:23:55
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public int update(String tableName, Map<String, Object> fieldVals, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc:根据id更新多条条数据
     * @Date 2017年5月11日下午5:23:55
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @return
     * @throws DbException
     */
    public int update(String tableName, List<Map<String, Object>> fieldMapList) throws DbException;

    /**
     * @Desc:根据id更新多条条数据，可以指定数据源
     * @Date 2017年5月11日下午5:23:55
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public int update(String tableName, List<Map<String, Object>> fieldMapList, JdbcTemplate jdbcTemplate)
            throws DbException;

    /**
     * @Desc:更新单条数据
     * @Date 2017年5月19日下午5:34:30
     * @author liuwww
     * @param entity
     * @return
     * @throws DbException
     */
    public <T> int update(TableEntity<T> entity) throws DbException;

    /**
     * @Desc:更新单条数据，可以指定数据源
     * @Date 2017年5月19日下午5:34:30
     * @author liuwww
     * @param entity
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public <T> int update(TableEntity<T> entity, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc:更新多条数据
     * @Date 2017年5月19日下午5:37:55
     * @author liuwww
     * @param entityList
     * @return
     * @throws DbException
     */
    public <T> int update(List<? extends TableEntity<T>> entityList) throws DbException;

    /**
     * @Desc:更新多条数据，可以指定数据源
     * @Date 2017年5月19日下午5:37:55
     * @author liuwww
     * @param entityList
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public <T> int update(List<? extends TableEntity<T>> entityList, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc: 根据条件统一更新多条数据
     * @Date 2017年5月11日下午5:28:08
     * @author liuwww
     * @param tableName
     * @param ValMap 要修改的字段和值
     * @param paramMap 条件，参数中每个有效的参数都将做为条件
     * @return
     * @throws DbException
     */
    public int updateRows(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap)
            throws DbException;

    /**
     * @Desc: 根据条件统一更新多条数据，可以指定数据源
     * @Date 2017年5月11日下午5:28:08
     * @author liuwww
     * @param tableName
     * @param ValMap 要修改的字段和值
     * @param paramMap 条件，参数中每个有效的参数都将做为条件
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public int updateRows(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap,
            JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc:根据id删除单条数据
     * @Date 2017年5月11日下午5:24:09
     * @author liuwww
     * @param tableName
     * @param idMap 含有id值的map
     * @return
     * @throws DbException
     */
    public int delete(String tableName, Map<String, Object> idMap) throws DbException;

    /**
     * @Desc:根据id删除单条数据
     * @Date 2017年5月11日下午5:24:09
     * @author liuwww
     * @param tableName
     * @param idMap 含有id值的map
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public int delete(String tableName, Map<String, Object> idMap, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc:根据id删除多条数据
     * @Date 2017年5月11日下午5:24:09
     * @author liuwww
     * @param tableName
     * @param idMapList 含有id值的mapList
     * @return
     * @throws DbException
     */
    public int delete(String tableName, List<Map<String, Object>> idMapList);

    /**
     * @Desc:根据id删除多条数据，可以指定数据源
     * @Date 2017年5月11日下午5:24:09
     * @author liuwww
     * @param tableName
     * @param idMapList 含有id值的mapList
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public int delete(String tableName, List<Map<String, Object>> idMapList, JdbcTemplate jdbcTemplate);

    /**
     * @Desc:根据条件删除数据
     * @Date 2017年5月11日下午5:25:34
     * @author liuwww
     * @param tableName
     * @param paramMap 参数中每个有效的参数都将做为条件
     * @return
     * @throws DbException
     */
    public int deleteRows(String tableName, Map<String, Object> paramMap) throws DbException;

    /**
     * @Desc:根据条件删除数据,可以指定数据源
     * @Date 2017年5月11日下午5:25:34
     * @author liuwww
     * @param tableName
     * @param paramMap 参数中每个有效的参数都将做为条件
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public int deleteRows(String tableName, Map<String, Object> paramMap, JdbcTemplate jdbcTemplate)
            throws DbException;

    /**
     * @Desc:删除一条数据
     * @Date 2017年5月19日下午5:43:31
     * @author liuwww
     * @param entity
     * @return
     * @throws DbException
     */
    public <T> int delete(TableEntity<T> entity) throws DbException;

    /**
     * @Desc:删除一条数据,可以指定数据源
     * @Date 2017年5月19日下午5:43:31
     * @author liuwww
     * @param entity
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public <T> int delete(TableEntity<T> entity, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc:删除多条数据
     * @Date 2017年5月19日下午5:43:47
     * @author liuwww
     * @param enityList
     * @return
     * @throws DbException
     */
    public <T> int delete(List<? extends TableEntity<T>> enityList) throws DbException;

    /**
     * @Desc:删除多条数据，可以指定数据源
     * @Date 2017年5月19日下午5:43:47
     * @author liuwww
     * @param enityList
     * @param jdbcTemplate
     * @return
     * @throws DbException
     */
    public <T> int delete(List<? extends TableEntity<T>> enityList, JdbcTemplate jdbcTemplate) throws DbException;

    /**
     * @Desc:删除给定id的表数据
     * @Date 2018年3月5日下午1:43:35
     * @author liuwww
     * @param table
     * @param id
     * @return
     * @throws DbException
     */
    public int delete(String table, Serializable id) throws DbException;

    /**
     * @Desc:删除给定id的表数据
     * @Date 2018年3月5日下午1:43:35
     * @author liuwww
     * @param table
     * @param ids
     * @return
     * @throws DbException
     */
    public int delete(String table, Serializable[] ids) throws DbException;

    /**
     * @desc:根据id删除数据
     * @Date:2019年2月28日上午11:05:25
     * @author liuwww
     * @param tableName
     * @param id
     * @param jdbcTemplate
     * @return
     */
    public int delete(String tableName, Serializable id, JdbcTemplate jdbcTemplate);

    /**
     * @desc:根据id删除数据
     * @Date:2019年2月28日上午11:05:25
     * @author liuwww
     * @param tableName
     * @param ids
     * @param jdbcTemplate
     * @return
     */
    public int delete(String tableName, Serializable[] ids, JdbcTemplate jdbcTemplate);

}
