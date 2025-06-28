package org.liuwww.db.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.liuwww.common.Idgen.IdGenerator;
import org.liuwww.common.Idgen.SnowflakeIdGeneratorUtil;
import org.liuwww.common.entity.TableEntity;
import org.liuwww.common.execption.DbException;
import org.liuwww.common.util.EntryUtil;
import org.liuwww.common.util.StringUtil;
import org.liuwww.db.context.DbContext;
import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.dao.IDataDao;
import org.liuwww.db.dao.impl.DataDao;
import org.liuwww.db.service.IDataTemplate;
import org.liuwww.db.sql.Column;
import org.liuwww.db.sql.Row;
import org.liuwww.db.sql.RowUtil;
import org.liuwww.db.sql.SqlBean;
import org.liuwww.db.sql.SqlBeanUtil;
import org.liuwww.db.sql.TableDefaultValue;
import org.liuwww.db.update.DefaultDeleteBean;
import org.liuwww.db.update.DefaultUpdateBean;
import org.liuwww.db.update.DeleteBean;
import org.liuwww.db.update.UpdateBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fastjson.JSON;

public class DataTemplate implements IDataTemplate
{
    private Logger logger = LoggerFactory.getLogger(DataTemplate.class);

    private IDataDao dataDao;

    private IdGenerator idGenerator;

    public DataTemplate()
    {
        this.dataDao = new DataDao();
    }

    public DataTemplate(IdGenerator idGenerator)
    {
        this.dataDao = new DataDao();
        this.idGenerator = idGenerator;
    }

    public DataTemplate(IDataDao dataDao, IdGenerator idGenerator)
    {
        this.dataDao = dataDao;
        this.idGenerator = idGenerator;
    }

    @Override
    public Row insert(String tableName, Map<String, Object> fieldVals) throws DbException
    {
        return insert(tableName, fieldVals, null);
    }

    @Override
    public <T> T insert(TableEntity<T> entity) throws DbException
    {
        return insert(entity, null);
    }

    @Override
    public <T> List<T> insert(List<? extends TableEntity<T>> entityList) throws DbException
    {
        return insert(entityList, null);
    }

    @Override
    public List<Row> insert(String tableName, List<Map<String, Object>> fList) throws DbException
    {
        return insert(tableName, fList, null);
    }

    @Override
    public int update(String tableName, Map<String, Object> fieldVals) throws DbException
    {
        return update(tableName, fieldVals, null);
    }

    @Override
    public int delete(String tableName, Map<String, Object> idMap) throws DbException
    {
        return delete(tableName, idMap, null);
    }

    @Override
    public int updateRows(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap) throws DbException
    {
        return updateRows(tableName, valMap, paramMap, null);
    }

    @Override
    public int deleteRows(String tableName, Map<String, Object> paramMap) throws DbException
    {
        return deleteRows(tableName, paramMap, null);
    }

    private void checkRow(Row row)
    {
        if (!row.getIsEffective())
        {
            if (logger.isErrorEnabled())
            {
                logger.error("非法更新sql row:{}", JSON.toJSON(row));
            }
            throw new DbException("找不到对应的table:" + row.getTableName() + ",或参数异常！");
        }
    }

    @Override
    public <T> int update(TableEntity<T> entity) throws DbException
    {
        return update(entity, null);
    }

    @Override
    public <T> int update(List<? extends TableEntity<T>> entityList) throws DbException
    {
        return update(entityList, null);
    }

    @Override
    public <T> int delete(TableEntity<T> entity) throws DbException
    {
        return delete(entity, null);
    }

    @Override
    public <T> int delete(List<? extends TableEntity<T>> enityList) throws DbException
    {
        return delete(enityList, null);
    }

    @Override
    public int update(String tableName, List<Map<String, Object>> fieldMapList) throws DbException
    {
        int count = 0;
        for (Map<String, Object> m : fieldMapList)
        {
            count += update(tableName, m);
        }
        return count;
    }

    @Override
    public int delete(String table, Serializable id) throws DbException
    {
        return delete(table, id, null);
    }

    @Override
    public Row insert(String tableName, Map<String, Object> fieldVals, JdbcTemplate jdbcTemplate) throws DbException
    {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
        Row row = RowUtil.getInsertRow(tableName, fieldVals, tmd);
        checkRow(row);

        IdGenerator idGenerator = getIdGenerator();
        String id = row.getIdValue();
        Column idColunm = tmd.getIdColumn();
        boolean needCreateId = StringUtil.isBlank(id) && idColunm != null;
        boolean isCreatedByDs = idGenerator.isCreatedByDatabase(tableName, tmd);
        if (needCreateId && !isCreatedByDs)
        {
            id = idGenerator.nextKey(tmd.getTableName(), tmd);
            if (StringUtil.isBlank(id))
            {
                throw new DbException("table[" + tableName + "]的id不可为空");
            }
            String idName = tmd.getIdColumn().getColumnName();
            row.setIdValue(id);
            row.setIdName(idName);
            row.getRowValueMap().put(idName, id);
        }
        else if (idColunm != null && StringUtil.isNotBlank(id))
        {
            row.getRowValueMap().put(idColunm.getColumnName(), id);
        }
        SqlBean bean = SqlBeanUtil.getInsertSqlBean(row);
        SqlBeanUtil.checkSqlBeanJdbcTemplate(tableName, jdbcTemplate, bean);
        if (needCreateId && isCreatedByDs)
        {
            id = dataDao.insert4AutoInc(bean);
            row.setIdValue(id);
            row.setIdName(tmd.getIdColumn().getColumnName());
        }
        else
        {
            dataDao.executeUpdate(bean);

        }
        return row;
    }

    @Override
    public List<Row> insert(String tableName, List<Map<String, Object>> fList, JdbcTemplate jdbcTemplate)
            throws DbException
    {
        if (jdbcTemplate == null)
        {
            jdbcTemplate = DbContext.getJdbcTemplateForTable(tableName);
        }
        List<Row> list = new ArrayList<Row>(fList.size());
        TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
        Column idColumn = tmd.getIdColumn();
        IdGenerator generator = getIdGenerator();
        boolean isCreatedByDs = generator.isCreatedByDatabase(tableName, tmd);
        if (!isCreatedByDs || idColumn == null)
        {
            List<Object[]> ps = getInsertMapParamList(fList, tmd, list);
            String sql = SqlBeanUtil.getInsertSql(tmd, !isCreatedByDs);
            dataDao.executeBatchUpdate(sql, ps, jdbcTemplate);
        }
        else
        {
            List<Map<String, Object>> withIdList = new ArrayList<>();
            List<Map<String, Object>> notWithIdList = new ArrayList<>();
            for (Map<String, Object> m : fList)
            {
                Object id = m.get(idColumn.getName());
                if (id == null)
                {
                    id = m.get(idColumn.getColumnName());
                }
                if (id != null && StringUtil.isNotBlank(id.toString()))
                {
                    withIdList.add(m);
                }
                else
                {
                    notWithIdList.add(m);
                }
            }
            List<Row> withIdRowList = new ArrayList<>(withIdList.size());
            if (!withIdList.isEmpty())
            {
                String sql = SqlBeanUtil.getInsertSql(tmd, true);
                List<Object[]> ps = getInsertMapParamList(withIdList, tmd, withIdRowList);
                dataDao.executeBatchUpdate(sql, ps, jdbcTemplate);
            }
            List<Row> notWithIdRowList = new ArrayList<>(notWithIdList.size());
            if (!notWithIdList.isEmpty())
            {
                String sql = SqlBeanUtil.getInsertSql(tmd, false);
                List<Object[]> ps = getInsertMapParamList(notWithIdList, tmd, notWithIdRowList);
                String[] ids = dataDao.insert4AutoInc(sql, ps, jdbcTemplate);
                for (int i = 0; i < ids.length; i++)
                {
                    notWithIdRowList.get(i).setIdValue(ids[i]);
                }
            }
            list.addAll(notWithIdRowList);
            list.addAll(withIdRowList);
        }

        return list;
    }

    private List<Object[]> getInsertMapParamList(List<Map<String, Object>> fList, TableMetaData tmd, List<Row> list)
    {

        List<Column> clist = tmd.getColumnList();
        List<Object[]> ps = new ArrayList<Object[]>(fList.size());
        TableDefaultValue defaultValue = RowUtil.getTableDefaultValue(tmd);
        Map<String, Object> dmap = null;
        if (defaultValue != null)
        {
            dmap = defaultValue.getNewDefaultValue(tmd);
        }
        Column idColumn = tmd.getIdColumn();
        IdGenerator idGenerator = getIdGenerator();
        String tableName = tmd.getTableName();
        boolean isCreateByDs = idGenerator.isCreatedByDatabase(tableName, tmd);
        int len = isCreateByDs ? clist.size() - 1 : clist.size();
        for (Map<String, Object> e : fList)
        {
            Object[] os = new Object[len];

            Row r = new Row();
            r.setDbType(tmd.getDbType());
            r.setAdditionalMap(dmap);
            if (idColumn != null)
            {
                r.setIdName(idColumn.getName());
            }
            r.setTableName(tmd.getTableName());
            Map<String, Object> rowValMap = new HashMap<String, Object>(len);
            int i = 0;
            for (Column c : clist)
            {
                Object val = e.get(c.getName());
                if (val == null)
                {
                    val = e.get(c.getColumnName());
                }
                if (c == idColumn && isCreateByDs)
                {
                    if (val != null && StringUtil.isNotBlank(val.toString()))
                    {
                        r.setIdValue(val.toString());
                    }
                    continue;
                }
                else if (c == idColumn && !isCreateByDs)
                {
                    if (val == null || StringUtil.isBlank(val.toString()))
                    {
                        val = idGenerator.nextKey(tableName, tmd);
                    }
                    r.setIdValue(val.toString());
                }
                else
                {
                    if (val == null && dmap != null)
                    {
                        val = dmap.get(c.getColumnName());
                    }

                }
                os[i++] = val;
                // if (val != null)
                {
                    rowValMap.put(c.getColumnName(), val);
                }
            }
            RowUtil.setRowValMap(r, rowValMap);
            r.setAdditionalMap(dmap);
            list.add(r);
            ps.add(os);
        }
        return ps;

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T insert(TableEntity<T> entity, JdbcTemplate jdbcTemplate) throws DbException
    {

        if (jdbcTemplate == null)
        {
            jdbcTemplate = DbContext.getJdbcTemplateForTable(entity.tableName());
        }
        TableMetaData tmd = DbContext.getTableMetaData(entity.tableName(), jdbcTemplate);
        String tableName = tmd.getTableName();
        Column idColumn = tmd.getIdColumn();
        IdGenerator generator = getIdGenerator();
        Row row = RowUtil.getInsertRow(entity, tmd);
        String id = row.getIdValue();
        boolean needCreateId = StringUtil.isBlank(id) && idColumn != null;
        boolean isCreatedByDs = generator.isCreatedByDatabase(tableName, tmd);
        Map<String, Object> addMap = row.getAdditionalMap();
        if (needCreateId && !isCreatedByDs)
        {
            String idName = idColumn.getColumnName();
            id = generator.nextKey(tableName, tmd);
            if (StringUtil.isBlank(id))
            {
                throw new DbException("table[" + tableName + "]的id不可为空");
            }
            row.setIdValue(id);
            row.setIdName(idName);
            row.getRowValueMap().put(idName, id);
            addMap.put(idName, id);
        }
        else if (idColumn != null && StringUtil.isNotBlank(id))
        {
            row.getRowValueMap().put(idColumn.getColumnName(), id);
        }

        checkRow(row);
        SqlBean bean = SqlBeanUtil.getInsertSqlBean(row);
        SqlBeanUtil.checkSqlBeanJdbcTemplate(tableName, jdbcTemplate, bean);
		bean.setTableMetaData(tmd);
		bean.setTables(new String[] { tableName });
        if (needCreateId && isCreatedByDs)
        {
            id = dataDao.insert4AutoInc(bean);
            row.setIdName(idColumn.getColumnName());
            row.setIdValue(id);
            addMap.put(row.getIdName(), id);
        }
        else
        {
            dataDao.executeUpdate(bean);
        }
        for (String key : addMap.keySet())
        {
            Column column = tmd.getColumn(key);
            String name = column.getName();
            if (EntryUtil.getField(entity.getClass(), name) != null)
            {
                EntryUtil.setFieldValue(entity, name, addMap.get(key));
            }
        }
        return (T) entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> insert(List<? extends TableEntity<T>> entityList, JdbcTemplate jdbcTemplate) throws DbException
    {

        Map<String, List<TableEntity<T>>> map = new HashMap<String, List<TableEntity<T>>>(2);
        for (TableEntity<T> entity : entityList)
        {
            String tableName = entity.tableName();
            List<TableEntity<T>> list = map.get(tableName);
            if (list == null)
            {
                list = new ArrayList<TableEntity<T>>(entityList.size());
                map.put(tableName, list);
            }
            list.add(entity);
        }
        IdGenerator generator = getIdGenerator();

        for (Entry<String, List<TableEntity<T>>> entry : map.entrySet())
        {
            String tableName = entry.getKey();
            List<TableEntity<T>> list = entry.getValue();
            TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
            if (jdbcTemplate == null)
            {
                jdbcTemplate = DbContext.getJdbcTemplateForTable(tableName);
            }
            TableDefaultValue defaultValue = RowUtil.getTableDefaultValue(tmd);
            Map<String, Object> dmap = null;
            if (defaultValue != null)
            {
                dmap = defaultValue.getNewDefaultValue(tmd);
            }
            InsertData insertData = new InsertData(list, tmd, generator, false);
            insertData.init();
            String sql = insertData.sqlNotWithId;
            List<Object[]> ps = insertData.psNotWithIdList;
            if (sql != null && ps != null && !ps.isEmpty())
            {
				String[] ids = dataDao.insert4AutoInc(sql, ps, jdbcTemplate, tmd);
                Column idColumn = tmd.getIdColumn();
                List<Object> elist = insertData.notWithIdEntityList;
                if (idColumn != null)
                {
                    String idName = idColumn.getName();
                    for (int i = 0, len = elist.size(); i < len; i++)
                    {
                        Object en = elist.get(i);
                        if (EntryUtil.hasField(en, idName))
                        {
                            EntryUtil.setFieldValue(en, idName, ids[i]);
                        }
                    }
                }
                setDefaultVal(elist, tmd, dmap);
            }

            sql = insertData.sqlWithId;
            ps = insertData.psWithIdList;
            if (sql != null && ps != null && !ps.isEmpty())
            {
                dataDao.executeBatchUpdate(sql, ps, jdbcTemplate);
                setDefaultVal(insertData.withIdEntityList, tmd, dmap);
            }
        }

        return (List<T>) entityList;
    }

    private <T> void setDefaultVal(List<Object> entityList, TableMetaData tmd, Map<String, Object> dmap)
    {
        if (dmap != null)
        {
            Class<?> clazz = entityList.get(0).getClass();
            for (Entry<String, Object> en : dmap.entrySet())
            {
                String key = en.getKey();
                Object val = en.getValue();
                Column c = tmd.getColumn(key);
                String name = c.getName();
                if (EntryUtil.hasField(clazz, name))
                {
                    for (int i = 0, len = entityList.size(); i < len; i++)
                    {
                        EntryUtil.setFieldValue(entityList.get(i), name, val);
                    }
                }
            }
        }
    }

    class InsertData
    {
        List<Object> entityList;

        TableMetaData tmd;

        IdGenerator generator;

        int size;

        boolean isMap;

        String sqlWithId;

        String sqlNotWithId;

        List<Object[]> psWithIdList;

        List<Object[]> psNotWithIdList;

        List<Object> withIdEntityList;

        List<Object> notWithIdEntityList;

        @SuppressWarnings(
        { "rawtypes", "unchecked" })
        public InsertData(List list, TableMetaData tmd, IdGenerator generator, boolean isMap)
        {
            super();
            this.entityList = list;
            this.tmd = tmd;
            this.generator = generator;
            this.isMap = isMap;
        }

        void init()
        {
            String tableName = tmd.getTableName();
            Column idColumn = tmd.getIdColumn();
            boolean isAutoInc = generator.isCreatedByDatabase(tableName, tmd) && idColumn != null;
            if (isAutoInc)
            {
                sqlNotWithId = SqlBeanUtil.getInsertSql(tmd, false);
                psNotWithIdList = new ArrayList<>(size);
            }
            else
            {
                sqlWithId = SqlBeanUtil.getInsertSql(tmd, true);
                psWithIdList = new ArrayList<>(size);
            }
            List<Column> clist = tmd.getColumnList();
            TableDefaultValue defaultValue = RowUtil.getTableDefaultValue(tmd);
            Map<String, Object> dmap = null;
            if (defaultValue != null)
            {
                dmap = defaultValue.getNewDefaultValue(tmd);
            }
            int columnSize = clist.size();
            for (Object e : entityList)
            {
                List<Object> pList = new ArrayList<Object>(columnSize);
                for (Column c : clist)
                {
                    Object val = EntryUtil.getFieldValue(e, c.getName());
                    if (isMap && val == null)
                    {
                        val = EntryUtil.getFieldValue(e, c.getColumnName());
                    }
                    if (c == idColumn)
                    {
                        if (val == null || StringUtil.isBlank(val.toString()))
                        {
                            if (isAutoInc)
                            {
                                continue;
                            }
                            else
                            {
                                val = idGenerator.nextKey(tableName, tmd);
                                if (EntryUtil.hasField(e, idColumn.getName()))
                                {
                                    EntryUtil.setFieldValue(e, idColumn.getName(), val);
                                }
                            }
                        }
                    }
                    else
                    {
                        if (val == null && dmap != null)
                        {
                            val = dmap.get(c.getColumnName());
                        }
                    }

                    pList.add(val);
                }

                if (pList.size() == columnSize)
                {
                    if (sqlWithId == null)
                    {
                        sqlWithId = SqlBeanUtil.getInsertSql(tmd, true);
                    }
                    if (psWithIdList == null)
                    {
                        psWithIdList = new ArrayList<>(size);
                    }
                    if (withIdEntityList == null)
                    {
                        withIdEntityList = new ArrayList<>();
                    }
                    psWithIdList.add(pList.toArray());
                    withIdEntityList.add(e);
                }
                else
                {
                    if (sqlNotWithId == null)
                    {
                        sqlNotWithId = SqlBeanUtil.getInsertSql(tmd, false);
                    }
                    if (psNotWithIdList == null)
                    {
                        psNotWithIdList = new ArrayList<>(size);
                    }
                    if (notWithIdEntityList == null)
                    {
                        notWithIdEntityList = new ArrayList<>();
                    }
                    psNotWithIdList.add(pList.toArray());
                    notWithIdEntityList.add(e);
                }
            }
        }
    }

    @Override
    public int update(String tableName, Map<String, Object> fieldVals, JdbcTemplate jdbcTemplate) throws DbException
    {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
        Row row = RowUtil.getUpdateRow(tableName, fieldVals, tmd);
        checkRow(row);
        SqlBean bean = SqlBeanUtil.getUpdateSqlBean(row);
        SqlBeanUtil.checkSqlBeanJdbcTemplate(tableName, jdbcTemplate, bean);
        return dataDao.executeUpdate(bean);
    }

    @Override
    public int update(String tableName, List<Map<String, Object>> fieldMapList, JdbcTemplate jdbcTemplate)
            throws DbException
    {
        int count = 0;
        for (Map<String, Object> m : fieldMapList)
        {
            count += update(tableName, m, jdbcTemplate);
        }
        return count;
    }

    @Override
    public <T> int update(TableEntity<T> entity, JdbcTemplate jdbcTemplate) throws DbException
    {
        TableMetaData tmd = DbContext.getTableMetaData(entity.tableName(), jdbcTemplate);
        Row row = RowUtil.getUpdateRow(entity, tmd);
        checkRow(row);
        SqlBean bean = SqlBeanUtil.getUpdateSqlBean(row);
        SqlBeanUtil.checkSqlBeanJdbcTemplate(entity.tableName(), jdbcTemplate, bean);
        return dataDao.executeUpdate(bean);
    }

    @Override
    public <T> int update(List<? extends TableEntity<T>> entityList, JdbcTemplate jdbcTemplate) throws DbException
    {
        int count = 0;
        for (TableEntity<T> e : entityList)
        {
            count += update(e, jdbcTemplate);
        }
        return count;
    }

    @Override
    public int updateRows(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap,
            JdbcTemplate jdbcTemplate) throws DbException
    {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
        SqlBean bean = SqlBeanUtil.getUpdateRowsSqlBean(tableName, valMap, paramMap, tmd);
        SqlBeanUtil.checkSqlBeanJdbcTemplate(tableName, jdbcTemplate, bean);
        return dataDao.executeUpdate(bean);
    }

    @Override
    public int delete(String tableName, Map<String, Object> idMap, JdbcTemplate jdbcTemplate) throws DbException
    {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
        Row row = RowUtil.getDeleteRow(tableName, idMap, tmd);
        checkRow(row);
        SqlBean bean = SqlBeanUtil.getDeleteSqlBean(row);
        SqlBeanUtil.checkSqlBeanJdbcTemplate(tableName, jdbcTemplate, bean);
        return dataDao.executeUpdate(bean);
    }

    @Override
    public int deleteRows(String tableName, Map<String, Object> paramMap, JdbcTemplate jdbcTemplate) throws DbException
    {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
        Row row = RowUtil.getDeleteRows(tableName, paramMap, tmd);
        checkRow(row);
        SqlBean bean = SqlBeanUtil.getDeleteSqlBean(row);
        SqlBeanUtil.checkSqlBeanJdbcTemplate(tableName, jdbcTemplate, bean);
        return dataDao.executeUpdate(bean);
    }

    @Override
    public <T> int delete(TableEntity<T> entity, JdbcTemplate jdbcTemplate) throws DbException
    {
        TableMetaData tmd = DbContext.getTableMetaData(entity.tableName(), jdbcTemplate);
        Row row = RowUtil.getDeleteRow(entity, tmd);
        checkRow(row);
        SqlBean bean = SqlBeanUtil.getDeleteSqlBean(row);
        SqlBeanUtil.checkSqlBeanJdbcTemplate(entity.tableName(), jdbcTemplate, bean);
        return dataDao.executeUpdate(bean);
    }

    @Override
    public <T> int delete(List<? extends TableEntity<T>> enityList, JdbcTemplate jdbcTemplate) throws DbException
    {
        Map<String, List<TableEntity<T>>> map = new HashMap<String, List<TableEntity<T>>>(2);

        for (TableEntity<T> entity : enityList)
        {
            String tableName = entity.tableName();
            List<TableEntity<T>> list = map.get(tableName);
            if (list == null)
            {
                list = new ArrayList<TableEntity<T>>(enityList.size());
                map.put(tableName, list);
            }
            list.add(entity);
        }
        int total = 0;
        for (Entry<String, List<TableEntity<T>>> entry : map.entrySet())
        {
            String tableName = entry.getKey();
            List<TableEntity<T>> l = entry.getValue();
            TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
            String sql = SqlBeanUtil.getDeleteSql(tmd);
            List<Object[]> ps = getIdPs(l, tmd);
            if (jdbcTemplate == null)
            {
                jdbcTemplate = DbContext.getJdbcTemplateForTable(tableName);
            }
            int[] rs = dataDao.executeBatchUpdate(sql, ps, jdbcTemplate);
            total += getTotal(rs);
        }
        return total;
    }

    private <T> List<Object[]> getIdPs(List<TableEntity<T>> l, TableMetaData tmd)
    {

        if (tmd.isUnionKey())
        {
            Column[] idColumns = tmd.getIdColumns();
            List<Object[]> list = new ArrayList<Object[]>(l.size());
            for (TableEntity<T> e : l)
            {
                Object[] ids = new Object[idColumns.length];
                for (int i = 0; i < ids.length; i++)
                {
                    Object val = EntryUtil.getFieldValue(e, idColumns[i].getName());
                    ids[i] = val;
                }
                // TODO 检查id是否都有值
                list.add(ids);
            }
            return list;
        }
        else
        {
            Column idColumn = tmd.getIdColumn();
            if (idColumn == null)
            {
                throw new DbException("表[" + tmd.getTableName() + "]没有设置主键");
            }
            String name = idColumn.getName();
            List<Object[]> list = new ArrayList<Object[]>(l.size());
            for (TableEntity<T> e : l)
            {
                Object val = EntryUtil.getFieldValue(e, name);
                list.add(new Object[]
                { val });
            }
            return list;
        }
    }

    private int getTotal(int[] rs)
    {
        int t = 0;
        for (int i : rs)
        {
            t += i;
        }
        return t;
    }

    @Override
    public int delete(String tableName, Serializable id, JdbcTemplate jdbcTemplate)
    {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
        String sql = SqlBeanUtil.getDeleteSql(tmd);
        if (jdbcTemplate == null)
        {
            jdbcTemplate = DbContext.getJdbcTemplateForTable(tableName);
        }
        return dataDao.executeUpdate(sql, new Object[]
        { id }, jdbcTemplate);
    }

    @Override
    public int delete(String tableName, List<Map<String, Object>> idMapList)
    {
        return delete(tableName, idMapList, (JdbcTemplate) null);

    }

    @Override
    public int delete(String tableName, List<Map<String, Object>> idMapList, JdbcTemplate jdbcTemplate)
    {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
        List<Object> idList = new ArrayList<Object>(idMapList.size());
        Column idColumn = tmd.getIdColumn();
        for (Map<String, Object> m : idMapList)
        {
            Object val = m.get(idColumn.getName());
            if (val == null)
            {
                val = m.get(idColumn.getColumnName());
            }
            if (val != null && StringUtil.isNotBlank(val.toString()))
            {
                idList.add(val);
            }
        }
        return delete(tableName, idList.toArray(new Serializable[0]), jdbcTemplate);

    }

    @Override
    public int delete(String table, Serializable[] ids) throws DbException
    {
        return delete(table, ids, (JdbcTemplate) null);
    }

    @Override
    public int delete(String tableName, Serializable[] ids, JdbcTemplate jdbcTemplate)
    {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
        String sql = SqlBeanUtil.getDeleteSql(tmd);
        if (jdbcTemplate == null)
        {
            jdbcTemplate = DbContext.getJdbcTemplateForTable(tableName);
        }
        List<Object[]> plist = new ArrayList<Object[]>(ids.length);
        for (Serializable id : ids)
        {
            plist.add(new Object[]
            { id });
        }
        int[] rs = dataDao.executeBatchUpdate(sql, plist, jdbcTemplate);

        return getTotal(rs);
    }

    public IdGenerator getIdGenerator()
    {
        if (idGenerator == null)
        {
            idGenerator = SnowflakeIdGeneratorUtil.getIdGenerator();
        }

        return idGenerator;
    }

    @Override
    public UpdateBean createUpdateBean(String tableName)
    {
        return new DefaultUpdateBean(tableName, this);
    }

    @Override
    public DeleteBean createDeleteBean(String tableName)
    {
        return new DefaultDeleteBean(tableName, this);
    }

}
