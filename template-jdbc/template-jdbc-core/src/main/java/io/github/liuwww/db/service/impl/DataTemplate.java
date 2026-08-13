package io.github.liuwww.db.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.github.liuwww.common.Idgen.IdGenerator;
import io.github.liuwww.common.Idgen.IdGenerators;
import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.common.execption.DbException;
import io.github.liuwww.common.util.EntryUtil;
import io.github.liuwww.common.util.StringUtil;
import io.github.liuwww.db.config.DatabaseConfigCenter;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.service.IDataTemplate;
import io.github.liuwww.db.sql.Column;
import io.github.liuwww.db.sql.Row;
import io.github.liuwww.db.sql.RowUtil;
import io.github.liuwww.db.sql.DefaultSqlBean;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.SqlBean;
import io.github.liuwww.db.sql.SqlBeanUtil;
import io.github.liuwww.db.sql.TableDefaultValue;
import io.github.liuwww.db.transaction.TransactionCallback;
import io.github.liuwww.db.transaction.TransactionManager;
import io.github.liuwww.db.transaction.impl.JdbcTransactionManager;
import io.github.liuwww.db.update.DefaultDeleteBean;
import io.github.liuwww.db.update.DefaultUpdateBean;
import io.github.liuwww.db.update.DeleteBean;
import io.github.liuwww.db.update.UpdateBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据操作模板实现类
 * <p>
 * 实现了 {@link IDataTemplate} 接口，提供统一的数据库增删改操作。
 * 支持 Map 和实体对象两种操作方式，支持自动生成主键和数据库自增主键。
 * </p>
 * <p>
 * 本类直接通过 {@link DatabaseExecutor} 执行 SQL，无需中间 DAO 层。
 * DatabaseExecutor 已封装了底层执行（JDBC 或 Spring JdbcTemplate），
 * 自增主键能力也通过其 default 方法提供。
 * </p>
 *
 * @author liuwww
 */
public class DataTemplate implements IDataTemplate {
    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(DataTemplate.class);

    /** ID 生成器 */
    private volatile IdGenerator idGenerator;

    /** 数据库配置中心 */
    private DatabaseConfigCenter configCenter;

    /** 事务管理器，用于事务感知的 executor 获取 */
    private TransactionManager transactionManager;

    /**
     * 创建默认的数据模板实例
     */
    public DataTemplate() {
    }

    /**
     * 创建数据模板实例（指定 ID 生成器）
     *
     * @param idGenerator ID 生成器
     */
    public DataTemplate(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * 创建数据模板实例（指定配置中心和 ID 生成器）
     *
     * @param configCenter 数据库配置中心
     * @param idGenerator  ID 生成器
     */
    public DataTemplate(DatabaseConfigCenter configCenter, IdGenerator idGenerator) {
        this.configCenter = configCenter;
        this.idGenerator = idGenerator;
    }

    /**
     * 创建数据模板实例（指定配置中心、ID 生成器和事务管理器）
     *
     * @param configCenter       数据库配置中心
     * @param idGenerator        ID 生成器
     * @param transactionManager 事务管理器
     */
    public DataTemplate(DatabaseConfigCenter configCenter, IdGenerator idGenerator,
            TransactionManager transactionManager) {
        this.configCenter = configCenter;
        this.idGenerator = idGenerator;
        this.transactionManager = transactionManager;
    }

    /**
     * 设置事务管理器
     *
     * @param transactionManager 事务管理器
     */
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Row insert(String tableName, Map<String, Object> fieldVals) throws DbException {
        return insert(tableName, fieldVals, null);
    }

    @Override
    public <T> T insert(TableEntity<T> entity) throws DbException {
        return insert(entity, null);
    }

    @Override
    public <T> List<T> insert(List<? extends TableEntity<T>> entityList) throws DbException {
        return insert(entityList, null);
    }

    @Override
    public List<Row> insert(String tableName, List<Map<String, Object>> fList) throws DbException {
        return insert(tableName, fList, null);
    }

    @Override
    public int update(String tableName, Map<String, Object> fieldVals) throws DbException {
        return update(tableName, fieldVals, null);
    }

    @Override
    public int delete(String tableName, Map<String, Object> idMap) throws DbException {
        return delete(tableName, idMap, null);
    }

    @Override
    public int updateRows(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap)
            throws DbException {
        return updateRows(tableName, valMap, paramMap, null);
    }

    @Override
    public int deleteRows(String tableName, Map<String, Object> paramMap) throws DbException {
        return deleteRows(tableName, paramMap, null);
    }

    private void checkRow(Row row) {
        if (!row.getIsEffective()) {
            if (logger.isErrorEnabled()) {
                logger.error("非法更新sql row:{}", row);
            }
            throw new DbException("找不到对应的table:" + row.getTableName() + ",或参数异常！");
        }
    }

    @Override
    public <T> int update(TableEntity<T> entity) throws DbException {
        return update(entity, null);
    }

    @Override
    public <T> int update(List<? extends TableEntity<T>> entityList) throws DbException {
        return update(entityList, null);
    }

    @Override
    public <T> int delete(TableEntity<T> entity) throws DbException {
        return delete(entity, null);
    }

    @Override
    public <T> int delete(List<? extends TableEntity<T>> enityList) throws DbException {
        return delete(enityList, null);
    }

    @Override
    public int update(String tableName, List<Map<String, Object>> fieldMapList) throws DbException {
        int count = 0;
        for (Map<String, Object> m : fieldMapList) {
            count += update(tableName, m);
        }
        return count;
    }

    @Override
    public int delete(String table, Serializable id) throws DbException {
        return delete(table, id, null);
    }

    public Row insert(String tableName, Map<String, Object> fieldVals, DatabaseExecutor databaseExecutor)
            throws DbException {
        TableMetaData tmd = getTableMetaData(tableName);
        Row row = RowUtil.getInsertRow(tableName, fieldVals, tmd);
        checkRow(row);

        IdGenerator idGenerator = getIdGenerator();
        String id = row.getIdValue();
        Column idColunm = tmd.getIdColumn();
        boolean needCreateId = StringUtil.isBlank(id) && idColunm != null;
        boolean isCreatedByDs = idGenerator.isCreatedByDatabase(tmd);
        if (needCreateId && !isCreatedByDs) {
            id = idGenerator.nextKey(tmd);
            if (StringUtil.isBlank(id)) {
                throw new DbException("table[" + tableName + "]的id不可为空");
            }
            String idName = tmd.getIdColumn().getColumnName();
            row.setIdValue(id);
            row.setIdName(idName);
            row.getRowValueMap().put(idName, id);
            // 将生成的ID设置回原始传入的fieldVals map中
            if (fieldVals != null) {
                fieldVals.put("id", id);
                fieldVals.put(idName, id);
            }
        } else if (idColunm != null && StringUtil.isNotBlank(id)) {
            row.getRowValueMap().put(idColunm.getColumnName(), id);
        }
        SqlBean bean = SqlBeanUtil.getInsertSqlBean(row);
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        bean.setDatabaseExecutor(databaseExecutor);
        if (needCreateId && isCreatedByDs) {
            id = bean.getDatabaseExecutor().insertWithAutoGenKey(bean.getSql(), bean.getParams());
            row.setIdValue(id);
            row.setIdName(tmd.getIdColumn().getColumnName());
            // 将数据库生成的ID设置回原始传入的fieldVals map中
            if (fieldVals != null) {
                fieldVals.put("id", id);
                fieldVals.put(tmd.getIdColumn().getColumnName(), id);
            }
        } else {
            bean.getDatabaseExecutor().update(bean.getSql(), bean.getParams());

        }
        return row;
    }

    public List<Row> insert(String tableName, List<Map<String, Object>> fList, DatabaseExecutor databaseExecutor)
            throws DbException {
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        List<Row> list = new ArrayList<Row>(fList.size());
        TableMetaData tmd = getTableMetaData(tableName);
        Column idColumn = tmd.getIdColumn();
        IdGenerator generator = getIdGenerator();
        boolean isCreatedByDs = generator.isCreatedByDatabase(tmd);
        if (!isCreatedByDs || idColumn == null) {
            List<Object[]> ps = getInsertMapParamList(fList, tmd, list);
            String sql = SqlBeanUtil.getInsertSql(tmd, !isCreatedByDs);
            databaseExecutor.batchUpdate(sql, ps);
        } else {
            List<Map<String, Object>> withIdList = new ArrayList<>();
            List<Map<String, Object>> notWithIdList = new ArrayList<>();
            for (Map<String, Object> m : fList) {
                Object id = m.get(idColumn.getName());
                if (id == null) {
                    id = m.get(idColumn.getColumnName());
                }
                if (id != null && StringUtil.isNotBlank(id.toString())) {
                    withIdList.add(m);
                } else {
                    notWithIdList.add(m);
                }
            }
            List<Row> withIdRowList = new ArrayList<>(withIdList.size());
            if (!withIdList.isEmpty()) {
                String sql = SqlBeanUtil.getInsertSql(tmd, true);
                List<Object[]> ps = getInsertMapParamList(withIdList, tmd, withIdRowList);
                databaseExecutor.batchUpdate(sql, ps);
            }
            List<Row> notWithIdRowList = new ArrayList<>(notWithIdList.size());
            if (!notWithIdList.isEmpty()) {
                String sql = SqlBeanUtil.getInsertSql(tmd, false);
                List<Object[]> ps = getInsertMapParamList(notWithIdList, tmd, notWithIdRowList);
                String[] ids = databaseExecutor.batchInsertWithAutoGenKey(sql, ps);
                for (int i = 0; i < ids.length; i++) {
                    notWithIdRowList.get(i).setIdValue(ids[i]);
                }
            }
            list.addAll(notWithIdRowList);
            list.addAll(withIdRowList);
        }

        return list;
    }

    private List<Object[]> getInsertMapParamList(List<Map<String, Object>> fList, TableMetaData tmd, List<Row> list) {

        List<Column> clist = tmd.getColumnList();
        List<Object[]> ps = new ArrayList<Object[]>(fList.size());
        TableDefaultValue defaultValue = RowUtil.getTableDefaultValue(tmd);
        Map<String, Object> dmap = null;
        if (defaultValue != null) {
            dmap = defaultValue.getNewDefaultValue(tmd);
        }
        Column idColumn = tmd.getIdColumn();
        IdGenerator idGenerator = getIdGenerator();
        String tableName = tmd.getTableName();
        boolean isCreateByDs = idGenerator.isCreatedByDatabase(tmd);
        int len = isCreateByDs ? clist.size() - 1 : clist.size();
        for (Map<String, Object> e : fList) {
            Object[] os = new Object[len];

            Row r = new Row();
            r.setDbType(tmd.getDbType());
            r.setAdditionalMap(dmap);
            if (idColumn != null) {
                r.setIdName(idColumn.getName());
            }
            r.setTableName(tmd.getTableName());
            Map<String, Object> rowValMap = new HashMap<String, Object>(len);
            int i = 0;
            for (Column c : clist) {
                Object val = e.get(c.getName());
                if (val == null) {
                    val = e.get(c.getColumnName());
                }
                if (c == idColumn && isCreateByDs) {
                    if (val != null && StringUtil.isNotBlank(val.toString())) {
                        r.setIdValue(val.toString());
                    }
                    continue;
                } else if (c == idColumn && !isCreateByDs) {
                    if (val == null || StringUtil.isBlank(val.toString())) {
                        val = idGenerator.nextKey(tmd);
                    }
                    r.setIdValue(val.toString());
                } else {
                    if (val == null && dmap != null) {
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
    public <T> T insert(TableEntity<T> entity, DatabaseExecutor databaseExecutor) throws DbException {

        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        TableMetaData tmd = getTableMetaData(entity.tableName());
        String tableName = tmd.getTableName();
        Column idColumn = tmd.getIdColumn();
        IdGenerator generator = getIdGenerator();
        Row row = RowUtil.getInsertRow(entity, tmd);
        String id = row.getIdValue();
        boolean needCreateId = StringUtil.isBlank(id) && idColumn != null;
        boolean isCreatedByDs = generator.isCreatedByDatabase(tmd);
        Map<String, Object> addMap = row.getAdditionalMap();
        if (needCreateId && !isCreatedByDs) {
            String idName = idColumn.getColumnName();
            id = generator.nextKey(tmd);
            if (StringUtil.isBlank(id)) {
                throw new DbException("table[" + tableName + "]的id不可为空");
            }
            row.setIdValue(id);
            row.setIdName(idName);
            row.getRowValueMap().put(idName, id);
            addMap.put(idName, id);
        } else if (idColumn != null && StringUtil.isNotBlank(id)) {
            row.getRowValueMap().put(idColumn.getColumnName(), id);
        }

        checkRow(row);
        SqlBean bean = SqlBeanUtil.getInsertSqlBean(row);
        bean.setTableMetaData(tmd);
        bean.setTables(new String[] { tableName });
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        bean.setDatabaseExecutor(databaseExecutor);
        if (needCreateId && isCreatedByDs) {
            id = bean.getDatabaseExecutor().insertWithAutoGenKey(bean.getSql(), bean.getParams());
            row.setIdName(idColumn.getColumnName());
            row.setIdValue(id);
            addMap.put(row.getIdName(), id);
        } else {
            bean.getDatabaseExecutor().update(bean.getSql(), bean.getParams());
        }
        for (String key : addMap.keySet()) {
            Column column = tmd.getColumn(key);
            String name = column.getName();
            if (EntryUtil.getField(entity.getClass(), name) != null) {
                EntryUtil.setFieldValue(entity, name, addMap.get(key));
            }
        }
        return (T) entity;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> insert(List<? extends TableEntity<T>> entityList, DatabaseExecutor databaseExecutor)
            throws DbException {

        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        Map<String, List<TableEntity<T>>> map = new HashMap<String, List<TableEntity<T>>>(2);
        for (TableEntity<T> entity : entityList) {
            String tableName = entity.tableName();
            List<TableEntity<T>> list = map.get(tableName);
            if (list == null) {
                list = new ArrayList<TableEntity<T>>(entityList.size());
                map.put(tableName, list);
            }
            list.add(entity);
        }
        IdGenerator generator = getIdGenerator();

        for (Entry<String, List<TableEntity<T>>> entry : map.entrySet()) {
            String tableName = entry.getKey();
            List<TableEntity<T>> list = entry.getValue();
            TableMetaData tmd = getTableMetaData(tableName);
            TableDefaultValue defaultValue = RowUtil.getTableDefaultValue(tmd);
            Map<String, Object> dmap = null;
            if (defaultValue != null) {
                dmap = defaultValue.getNewDefaultValue(tmd);
            }
            InsertData insertData = new InsertData(list, tmd, generator, false);
            insertData.init();
            String sql = insertData.sqlNotWithId;
            List<Object[]> ps = insertData.psNotWithIdList;
            if (sql != null && ps != null && !ps.isEmpty()) {
                String[] ids = databaseExecutor.batchInsertWithAutoGenKey(sql, ps);
                Column idColumn = tmd.getIdColumn();
                List<Object> elist = insertData.notWithIdEntityList;
                if (idColumn != null) {
                    String idName = idColumn.getName();
                    for (int i = 0, len = elist.size(); i < len; i++) {
                        Object en = elist.get(i);
                        if (EntryUtil.hasField(en, idName)) {
                            EntryUtil.setFieldValue(en, idName, ids[i]);
                        }
                    }
                }
                setDefaultVal(elist, tmd, dmap);
            }

            sql = insertData.sqlWithId;
            ps = insertData.psWithIdList;
            if (sql != null && ps != null && !ps.isEmpty()) {
                databaseExecutor.batchUpdate(sql, ps);
                setDefaultVal(insertData.withIdEntityList, tmd, dmap);
            }
        }

        return (List<T>) entityList;
    }

    private <T> void setDefaultVal(List<Object> entityList, TableMetaData tmd, Map<String, Object> dmap) {
        if (dmap != null) {
            Class<?> clazz = entityList.get(0).getClass();
            for (Entry<String, Object> en : dmap.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                Column c = tmd.getColumn(key);
                String name = c.getName();
                if (EntryUtil.hasField(clazz, name)) {
                    for (int i = 0, len = entityList.size(); i < len; i++) {
                        EntryUtil.setFieldValue(entityList.get(i), name, val);
                    }
                }
            }
        }
    }

    class InsertData {
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

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public InsertData(List list, TableMetaData tmd, IdGenerator generator, boolean isMap) {
            super();
            this.entityList = list;
            this.tmd = tmd;
            this.generator = generator;
            this.isMap = isMap;
        }

        void init() {
            String tableName = tmd.getTableName();
            Column idColumn = tmd.getIdColumn();
            boolean isAutoInc = generator.isCreatedByDatabase(tmd) && idColumn != null;
            if (isAutoInc) {
                sqlNotWithId = SqlBeanUtil.getInsertSql(tmd, false);
                psNotWithIdList = new ArrayList<>(size);
            } else {
                sqlWithId = SqlBeanUtil.getInsertSql(tmd, true);
                psWithIdList = new ArrayList<>(size);
            }
            List<Column> clist = tmd.getColumnList();
            TableDefaultValue defaultValue = RowUtil.getTableDefaultValue(tmd);
            Map<String, Object> dmap = null;
            if (defaultValue != null) {
                dmap = defaultValue.getNewDefaultValue(tmd);
            }
            int columnSize = clist.size();
            for (Object e : entityList) {
                List<Object> pList = new ArrayList<Object>(columnSize);
                for (Column c : clist) {
                    Object val = EntryUtil.getFieldValue(e, c.getName());
                    if (isMap && val == null) {
                        val = EntryUtil.getFieldValue(e, c.getColumnName());
                    }
                    if (c == idColumn) {
                        if (val == null || StringUtil.isBlank(val.toString())) {
                            if (isAutoInc) {
                                continue;
                            } else {
                                val = idGenerator.nextKey(tmd);
                                if (EntryUtil.hasField(e, idColumn.getName())) {
                                    EntryUtil.setFieldValue(e, idColumn.getName(), val);
                                }
                            }
                        }
                    } else {
                        if (val == null && dmap != null) {
                            val = dmap.get(c.getColumnName());
                        }
                    }

                    pList.add(val);
                }

                if (pList.size() == columnSize) {
                    if (sqlWithId == null) {
                        sqlWithId = SqlBeanUtil.getInsertSql(tmd, true);
                    }
                    if (psWithIdList == null) {
                        psWithIdList = new ArrayList<>(size);
                    }
                    if (withIdEntityList == null) {
                        withIdEntityList = new ArrayList<>();
                    }
                    psWithIdList.add(pList.toArray());
                    withIdEntityList.add(e);
                } else {
                    if (sqlNotWithId == null) {
                        sqlNotWithId = SqlBeanUtil.getInsertSql(tmd, false);
                    }
                    if (psNotWithIdList == null) {
                        psNotWithIdList = new ArrayList<>(size);
                    }
                    if (notWithIdEntityList == null) {
                        notWithIdEntityList = new ArrayList<>();
                    }
                    psNotWithIdList.add(pList.toArray());
                    notWithIdEntityList.add(e);
                }
            }
        }
    }

    public int update(String tableName, Map<String, Object> fieldVals, DatabaseExecutor databaseExecutor)
            throws DbException {
        TableMetaData tmd = getTableMetaData(tableName);
        Row row = RowUtil.getUpdateRow(tableName, fieldVals, tmd);
        checkRow(row);
        SqlBean bean = SqlBeanUtil.getUpdateSqlBean(row);
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        bean.setDatabaseExecutor(databaseExecutor);
        return executeUpdate(bean);
    }

    public int update(String tableName, List<Map<String, Object>> fieldMapList, DatabaseExecutor databaseExecutor)
            throws DbException {
        int count = 0;
        for (Map<String, Object> m : fieldMapList) {
            count += update(tableName, m, databaseExecutor);
        }
        return count;
    }

    public <T> int update(TableEntity<T> entity, DatabaseExecutor databaseExecutor) throws DbException {
        TableMetaData tmd = getTableMetaData(entity.tableName());
        Row row = RowUtil.getUpdateRow(entity, tmd);
        checkRow(row);
        SqlBean bean = SqlBeanUtil.getUpdateSqlBean(row);
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        bean.setDatabaseExecutor(databaseExecutor);
        return executeUpdate(bean);
    }

    public <T> int update(List<? extends TableEntity<T>> entityList, DatabaseExecutor databaseExecutor)
            throws DbException {
        int count = 0;
        for (TableEntity<T> e : entityList) {
            count += update(e, databaseExecutor);
        }
        return count;
    }

    public int updateRows(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap,
            DatabaseExecutor databaseExecutor) throws DbException {
        TableMetaData tmd = getTableMetaData(tableName);
        SqlBean bean = SqlBeanUtil.getUpdateRowsSqlBean(tableName, valMap, paramMap, tmd);
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        bean.setDatabaseExecutor(databaseExecutor);
        return executeUpdate(bean);
    }

    /**
     * 基于主键删除单条记录
     * <p>
     * 该方法通过主键删除单条记录，调用 {@link RowUtil#getDeleteRow(String, Map, TableMetaData)}，
     * 只处理主键字段，非主键字段会被忽略。适用于根据主键精确删除的场景。
     * </p>
     * 
     * @param tableName        表名
     * @param idMap            主键字段值映射（必须包含主键字段）
     * @param databaseExecutor 数据库执行器
     * @return 删除的行数
     * @throws DbException 当删除失败时抛出
     */
    public int delete(String tableName, Map<String, Object> idMap, DatabaseExecutor databaseExecutor)
            throws DbException {
        TableMetaData tmd = getTableMetaData(tableName);
        Row row = RowUtil.getDeleteRow(tableName, idMap, tmd);
        checkRow(row);
        SqlBean bean = SqlBeanUtil.getDeleteSqlBean(row);
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        bean.setDatabaseExecutor(databaseExecutor);
        return executeUpdate(bean);
    }

    /**
     * 基于条件批量删除记录
     * <p>
     * 该方法通过任意条件批量删除记录，调用 {@link RowUtil#getDeleteRows(String, Map, TableMetaData)}，
     * 支持任意字段作为查询条件。适用于根据业务条件批量删除的场景。
     * </p>
     * 
     * @param tableName        表名
     * @param paramMap         条件字段值映射（可以是任意字段）
     * @param databaseExecutor 数据库执行器
     * @return 删除的行数
     * @throws DbException 当删除失败时抛出
     */
    public int deleteRows(String tableName, Map<String, Object> paramMap, DatabaseExecutor databaseExecutor)
            throws DbException {
        TableMetaData tmd = getTableMetaData(tableName);
        Row row = RowUtil.getDeleteRows(tableName, paramMap, tmd);
        checkRow(row);
        SqlBean bean = SqlBeanUtil.getDeleteSqlBean(row);
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        bean.setDatabaseExecutor(databaseExecutor);
        return executeUpdate(bean);
    }

    public <T> int delete(TableEntity<T> entity, DatabaseExecutor databaseExecutor) throws DbException {
        TableMetaData tmd = getTableMetaData(entity.tableName());
        Row row = RowUtil.getDeleteRow(entity, tmd);
        checkRow(row);
        SqlBean bean = SqlBeanUtil.getDeleteSqlBean(row);
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        bean.setDatabaseExecutor(databaseExecutor);
        return executeUpdate(bean);
    }

    public <T> int delete(List<? extends TableEntity<T>> enityList, DatabaseExecutor databaseExecutor)
            throws DbException {
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        Map<String, List<TableEntity<T>>> map = new HashMap<String, List<TableEntity<T>>>(2);

        for (TableEntity<T> entity : enityList) {
            String tableName = entity.tableName();
            List<TableEntity<T>> list = map.get(tableName);
            if (list == null) {
                list = new ArrayList<TableEntity<T>>(enityList.size());
                map.put(tableName, list);
            }
            list.add(entity);
        }
        int total = 0;
        for (Entry<String, List<TableEntity<T>>> entry : map.entrySet()) {
            String tableName = entry.getKey();
            List<TableEntity<T>> l = entry.getValue();
            TableMetaData tmd = getTableMetaData(tableName);
            String sql = SqlBeanUtil.getDeleteSql(tmd);
            List<Object[]> ps = getIdPs(l, tmd);
            int[] rs = executeBatchUpdate(sql, ps, databaseExecutor);
            total += getTotal(rs);
        }
        return total;
    }

    private <T> List<Object[]> getIdPs(List<TableEntity<T>> l, TableMetaData tmd) {

        if (tmd.isUnionKey()) {
            Column[] idColumns = tmd.getIdColumns();
            List<Object[]> list = new ArrayList<Object[]>(l.size());
            for (TableEntity<T> e : l) {
                Object[] ids = new Object[idColumns.length];
                for (int i = 0; i < ids.length; i++) {
                    Object val = EntryUtil.getFieldValue(e, idColumns[i].getName());
                    if (val == null || (val instanceof String && StringUtil.isBlank((String) val))) {
                        throw new DbException("表[" + tmd.getTableName() + "]联合主键字段["
                                + idColumns[i].getName() + "]的值不能为空");
                    }
                    ids[i] = val;
                }
                list.add(ids);
            }
            return list;
        } else {
            Column idColumn = tmd.getIdColumn();
            if (idColumn == null) {
                throw new DbException("表[" + tmd.getTableName() + "]没有设置主键");
            }
            String name = idColumn.getName();
            List<Object[]> list = new ArrayList<Object[]>(l.size());
            for (TableEntity<T> e : l) {
                Object val = EntryUtil.getFieldValue(e, name);
                if (val == null || (val instanceof String && StringUtil.isBlank((String) val))) {
                    throw new DbException("表[" + tmd.getTableName() + "]主键字段["
                            + name + "]的值不能为空");
                }
                list.add(new Object[] { val });
            }
            return list;
        }
    }

    private int getTotal(int[] rs) {
        int t = 0;
        for (int i : rs) {
            t += i;
        }
        return t;
    }

    public int delete(String tableName, Serializable id, DatabaseExecutor databaseExecutor) {
        TableMetaData tmd = getTableMetaData(tableName);
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        SqlBean bean = new DefaultSqlBean(SqlBeanUtil.getDeleteSql(tmd), new Object[] { id }, null, tmd.getDbType(),
                databaseExecutor);
        return executeUpdate(bean);
    }

    @Override
    public int delete(String tableName, List<Map<String, Object>> idMapList) {
        return delete(tableName, idMapList, (DatabaseExecutor) null);

    }

    public int delete(String tableName, List<Map<String, Object>> idMapList, DatabaseExecutor databaseExecutor) {
        TableMetaData tmd = getTableMetaData(tableName);
        List<Object> idList = new ArrayList<Object>(idMapList.size());
        Column idColumn = tmd.getIdColumn();
        for (Map<String, Object> m : idMapList) {
            Object val = m.get(idColumn.getName());
            if (val == null) {
                val = m.get(idColumn.getColumnName());
            }
            if (val != null && StringUtil.isNotBlank(val.toString())) {
                idList.add(val);
            }
        }
        return delete(tableName, idList.toArray(new Serializable[0]), databaseExecutor);

    }

    @Override
    public int delete(String table, Serializable[] ids) throws DbException {
        return delete(table, ids, (DatabaseExecutor) null);
    }

    public int delete(String tableName, Serializable[] ids, DatabaseExecutor databaseExecutor) {
        if (databaseExecutor == null) {
            databaseExecutor = getDatabaseExecutor();
        }
        TableMetaData tmd = getTableMetaData(tableName);
        String sql = SqlBeanUtil.getDeleteSql(tmd);
        List<Object[]> plist = new ArrayList<Object[]>(ids.length);
        for (Serializable id : ids) {
            plist.add(new Object[] { id });
        }
        int[] rs = executeBatchUpdate(sql, plist, databaseExecutor);

        return getTotal(rs);
    }

    /**
     * 获取表元数据
     *
     * @param tableName 表名
     * @return 表元数据
     * @throws DbException 当无法获取表元数据时抛出
     */
    private TableMetaData getTableMetaData(String tableName) throws DbException {
        if (configCenter != null) {
            TableMetaData tmd = configCenter.getTableMetaData(tableName);
            if (tmd == null) {
                throw new DbException("表[" + tableName + "]不存在或未加载元数据");
            }
            return tmd;
        }
        throw new DbException("DatabaseConfigCenter 未初始化，请通过构造函数注入");
    }

    /**
     * 获取数据库执行器。
     * <p>
     * 若当前线程处于事务中，返回绑定事务连接的执行器，确保所有操作在同一个事务内执行；
     * 否则返回配置中心的共享执行器。
     * </p>
     *
     * @return 数据库执行器
     * @throws DbException 当无法获取数据库执行器时抛出
     */
    private DatabaseExecutor getDatabaseExecutor() throws DbException {
        if (configCenter != null) {
            // 事务感知：若在事务中，返回绑定事务连接的 executor
            if (transactionManager != null && transactionManager.isInTransaction()) {
                if (transactionManager instanceof JdbcTransactionManager) {
                    java.sql.Connection txConn = ((JdbcTransactionManager) transactionManager).getCurrentConnection();
                    if (txConn != null) {
                        return new io.github.liuwww.db.executor.impl.JdbcDatabaseExecutor(txConn);
                    }
                }
            }
            return configCenter.getDatabaseExecutor();
        }
        throw new DbException("DatabaseConfigCenter 未初始化，请通过构造函数注入");
    }

    /**
     * 执行更新 SqlBean
     * <p>
     * 辅助方法，从 SqlBean 提取 sql/params/executor 执行更新。
     * </p>
     */
    private int executeUpdate(SqlBean bean) throws DbException {
        DatabaseExecutor executor = bean.getDatabaseExecutor();
        if (executor == null) {
            throw new DbException("SqlBean 未设置 DatabaseExecutor");
        }
        return executor.update(bean.getSql(), bean.getParams());
    }

    /**
     * 执行批量更新
     * <p>
     * 辅助方法，使用指定的 executor 和批量参数执行批量更新。
     * </p>
     */
    private int[] executeBatchUpdate(String sql, List<Object[]> batchArgs, DatabaseExecutor executor)
            throws DbException {
        if (executor == null) {
            throw new DbException("DatabaseExecutor 不能为空");
        }
        return executor.batchUpdate(sql, batchArgs);
    }

    public IdGenerator getIdGenerator() {
        if (idGenerator == null) {
            idGenerator = IdGenerators.getDefault();
        }

        return idGenerator;
    }

    @Override
    public UpdateBean createUpdateBean(String tableName) {
        return new DefaultUpdateBean(tableName, this);
    }

    @Override
    public DeleteBean createDeleteBean(String tableName) {
        return new DefaultDeleteBean(tableName, this);
    }

    @Override
    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        if (transactionManager == null) {
            throw new DbException("事务管理器未初始化，请通过构造函数或 setTransactionManager 注入");
        }
        return transactionManager.executeInTransaction(callback);
    }

}
