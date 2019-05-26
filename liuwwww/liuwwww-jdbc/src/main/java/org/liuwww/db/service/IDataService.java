package org.liuwww.db.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.liuwww.db.query.Entity;
import org.liuwww.db.sql.Row;
import org.springframework.jdbc.core.JdbcTemplate;

import org.liuwww.common.execption.SysException;

public interface IDataService
{
    /**
     * @Desc:插入单条数据
     * @Date 2017年5月11日下午5:23:38
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @return
     * @throws SysException
     */
    public Row insert(String tableName, Map<String, Object> fieldVals) throws SysException;

    /**
     * @Desc:插入单条数据,可以指定数据源
     * @Date 2017年5月11日下午5:23:38
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public Row insert(String tableName, Map<String, Object> fieldVals, JdbcTemplate jdbcTemplate) throws SysException;

    /**
     * @Desc:插入多条数据
     * @Date 2017年5月11日下午5:23:38
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @return
     * @throws SysException
     */
    public List<Row> insert(String tableName, List<Map<String, Object>> fieldValList) throws SysException;

    /**
     * @Desc:插入多条数据,可以指定数据源
     * @Date 2017年5月11日下午5:23:38
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public List<Row> insert(String tableName, List<Map<String, Object>> fieldValList, JdbcTemplate jdbcTemplate)
            throws SysException;

    /**
     * @Desc:插入单条数据
     * @Date 2017年5月19日下午5:32:23
     * @author liuwww
     * @param entity
     * @return
     * @throws SysException
     */
    public <T> T insert(Entity<T> entity) throws SysException;

    /**
     * @Desc:插入单条数据，可以指定数据源
     * @Date 2017年5月19日下午5:32:23
     * @author liuwww
     * @param entity
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public <T> T insert(Entity<T> entity, JdbcTemplate jdbcTemplate) throws SysException;

    /**
     * @Desc:插入多条数据
     * @Date 2017年5月19日下午5:36:16
     * @author liuwww
     * @param entityList
     * @throws SysException
     */
    public <T> List<T> insert(List<? extends Entity<T>> entityList) throws SysException;

    /**
     * @Desc:插入多条数据,可以指定数据源
     * @Date 2017年5月19日下午5:36:16
     * @author liuwww
     * @param entityList
     * @param jdbcTemplate
     * @throws SysException
     */
    public <T> List<T> insert(List<? extends Entity<T>> entityList, JdbcTemplate jdbcTemplate) throws SysException;

    /**
     * @Desc:根据id更新单条数据
     * @Date 2017年5月11日下午5:23:55
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @return
     * @throws SysException
     */
    public int update(String tableName, Map<String, Object> fieldVals) throws SysException;

    /**
     * @Desc:根据id更新单条数据，可以指定数据源
     * @Date 2017年5月11日下午5:23:55
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public int update(String tableName, Map<String, Object> fieldVals, JdbcTemplate jdbcTemplate) throws SysException;

    /**
     * @Desc:根据id更新多条条数据
     * @Date 2017年5月11日下午5:23:55
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @return
     * @throws SysException
     */
    public int update(String tableName, List<Map<String, Object>> fieldMapList) throws SysException;

    /**
     * @Desc:根据id更新多条条数据，可以指定数据源
     * @Date 2017年5月11日下午5:23:55
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public int update(String tableName, List<Map<String, Object>> fieldMapList, JdbcTemplate jdbcTemplate)
            throws SysException;

    /**
     * @Desc:更新单条数据
     * @Date 2017年5月19日下午5:34:30
     * @author liuwww
     * @param entity
     * @return
     * @throws SysException
     */
    public <T> int update(Entity<T> entity) throws SysException;

    /**
     * @Desc:更新单条数据，可以指定数据源
     * @Date 2017年5月19日下午5:34:30
     * @author liuwww
     * @param entity
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public <T> int update(Entity<T> entity, JdbcTemplate jdbcTemplate) throws SysException;

    /**
     * @Desc:更新多条数据
     * @Date 2017年5月19日下午5:37:55
     * @author liuwww
     * @param entityList
     * @return
     * @throws SysException
     */
    public <T> int update(List<? extends Entity<T>> entityList) throws SysException;

    /**
     * @Desc:更新多条数据，可以指定数据源
     * @Date 2017年5月19日下午5:37:55
     * @author liuwww
     * @param entityList
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public <T> int update(List<? extends Entity<T>> entityList, JdbcTemplate jdbcTemplate) throws SysException;

    /**
     * @Desc: 根据条件统一更新多条数据
     * @Date 2017年5月11日下午5:28:08
     * @author liuwww
     * @param tableName
     * @param ValMap 要修改的字段和值
     * @param paramMap 条件，参数中每个有效的参数都将做为条件
     * @return
     * @throws SysException
     */
    public int updateRows(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap)
            throws SysException;

    /**
     * @Desc: 根据条件统一更新多条数据，可以指定数据源
     * @Date 2017年5月11日下午5:28:08
     * @author liuwww
     * @param tableName
     * @param ValMap 要修改的字段和值
     * @param paramMap 条件，参数中每个有效的参数都将做为条件
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public int updateRows(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap,
            JdbcTemplate jdbcTemplate) throws SysException;

    /**
     * @Desc:根据id删除单条数据
     * @Date 2017年5月11日下午5:24:09
     * @author liuwww
     * @param tableName
     * @param idMap 含有id值的map
     * @return
     * @throws SysException
     */
    public int delete(String tableName, Map<String, Object> idMap) throws SysException;

    /**
     * @Desc:根据id删除单条数据
     * @Date 2017年5月11日下午5:24:09
     * @author liuwww
     * @param tableName
     * @param idMap 含有id值的map
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public int delete(String tableName, Map<String, Object> idMap, JdbcTemplate jdbcTemplate) throws SysException;

    /**
     * @Desc:根据id删除多条数据
     * @Date 2017年5月11日下午5:24:09
     * @author liuwww
     * @param tableName
     * @param idMapList 含有id值的mapList
     * @return
     * @throws SysException
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
     * @throws SysException
     */
    public int delete(String tableName, List<Map<String, Object>> idMapList, JdbcTemplate jdbcTemplate);

    /**
     * @Desc:根据条件删除数据
     * @Date 2017年5月11日下午5:25:34
     * @author liuwww
     * @param tableName
     * @param paramMap 参数中每个有效的参数都将做为条件
     * @return
     * @throws SysException
     */
    public int deleteRows(String tableName, Map<String, Object> paramMap) throws SysException;

    /**
     * @Desc:根据条件删除数据,可以指定数据源
     * @Date 2017年5月11日下午5:25:34
     * @author liuwww
     * @param tableName
     * @param paramMap 参数中每个有效的参数都将做为条件
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public int deleteRows(String tableName, Map<String, Object> paramMap, JdbcTemplate jdbcTemplate)
            throws SysException;

    /**
     * @Desc:删除一条数据
     * @Date 2017年5月19日下午5:43:31
     * @author liuwww
     * @param entity
     * @return
     * @throws SysException
     */
    public <T> int delete(Entity<T> entity) throws SysException;

    /**
     * @Desc:删除一条数据,可以指定数据源
     * @Date 2017年5月19日下午5:43:31
     * @author liuwww
     * @param entity
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public <T> int delete(Entity<T> entity, JdbcTemplate jdbcTemplate) throws SysException;

    /**
     * @Desc:删除多条数据
     * @Date 2017年5月19日下午5:43:47
     * @author liuwww
     * @param enityList
     * @return
     * @throws SysException
     */
    public <T> int delete(List<? extends Entity<T>> enityList) throws SysException;

    /**
     * @Desc:删除多条数据，可以指定数据源
     * @Date 2017年5月19日下午5:43:47
     * @author liuwww
     * @param enityList
     * @param jdbcTemplate
     * @return
     * @throws SysException
     */
    public <T> int delete(List<? extends Entity<T>> enityList, JdbcTemplate jdbcTemplate) throws SysException;

    /**
     * @Desc:删除给定id的表数据
     * @Date 2018年3月5日下午1:43:35
     * @author liuwww
     * @param table
     * @param id
     * @return
     * @throws SysException
     */
    public int delete(String table, Serializable id) throws SysException;

    /**
     * @Desc:删除给定id的表数据
     * @Date 2018年3月5日下午1:43:35
     * @author liuwww
     * @param table
     * @param ids
     * @return
     * @throws SysException
     */
    public int delete(String table, Serializable[] ids) throws SysException;

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
