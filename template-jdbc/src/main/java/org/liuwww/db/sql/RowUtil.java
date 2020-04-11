package org.liuwww.db.sql;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.liuwww.db.context.DbContext;
import org.liuwww.db.context.TableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.liuwww.common.entity.TableEntity;
import org.liuwww.common.execption.DbException;
import org.liuwww.common.util.BeanUtil;
import org.liuwww.common.util.DateUtil;
import org.liuwww.common.util.EntryUtil;
import org.liuwww.common.util.StringUtil;

public class RowUtil
{
    private static Logger logger = LoggerFactory.getLogger(RowUtil.class);

    private static enum DataOpeType {
        INSERT, UPDATE
    }

    public static TableDefaultValue getTableDefaultValue(TableMetaData tmd)
    {
        TableDefaultValue tableDefaultValue = (TableDefaultValue) BeanUtil
                .getBean(tmd.getTableName().toUpperCase() + TableDefaultValue.BEAN_NAME_SUFFIX);
        if (tableDefaultValue == null)
        {
            tableDefaultValue = (TableDefaultValue) BeanUtil.getBean(TableDefaultValue.BEAN_NAME_SUFFIX);
        }
        return tableDefaultValue;
    }

    private static Row defaultValue(Row row, TableMetaData tmd, DataOpeType type)
    {
        if (!row.getIsEffective())
        {
            return row;
        }
        TableDefaultValue tableDefaultValue = (TableDefaultValue) BeanUtil
                .getBean(row.getTableName().toUpperCase() + TableDefaultValue.BEAN_NAME_SUFFIX);
        if (tableDefaultValue == null)
        {
            tableDefaultValue = (TableDefaultValue) BeanUtil.getBean(TableDefaultValue.BEAN_NAME_SUFFIX);
        }
        if (tableDefaultValue != null)
        {
            Map<String, Object> map = null;
            if (type.equals(DataOpeType.INSERT))
            {
                map = tableDefaultValue.getNewDefaultValue(tmd);
            }
            else
            {
                map = tableDefaultValue.getUpdateDefaultValue(tmd);
            }
            Map<String, Object> valuMap = row.getRowValueMap();
            Map<String, Object> addMap = new HashMap<String, Object>();
            for (String key : map.keySet())
            {
                Object val = map.get(key);
                if (!valuMap.containsKey(key))
                {
                    valuMap.put(key, val);
                    addMap.put(key, val);
                }
            }
            row.setAdditionalMap(addMap);
        }
        else
        {
            row.setAdditionalMap(Collections.<String, Object> emptyMap());
        }
        convertInValidVal(row, tmd);
        return row;
    }

    public static void convertInValidVal(Row row, TableMetaData tmd)
    {
        Map<String, Object> valueVal = row.getRowValueMap();
        for (Entry<String, Object> entry : valueVal.entrySet())
        {
            String key = entry.getKey();
            Column column = tmd.getColumnByColumn(key);
            if (Types.TIMESTAMP == column.getDataType() || Types.DATE == column.getDataType())
            {
                Object val = entry.getValue();
                Timestamp tt = null;
                if (!(val instanceof Timestamp))
                {

                    if (val instanceof java.util.Date)
                    {
                        tt = new Timestamp(((Date) val).getTime());
                    }
                    else if (val instanceof String)
                    {
                        if (StringUtils.isBlank(val.toString()))
                        {
                            tt = null;
                        }
                        else
                        {
                            tt = new Timestamp(DateUtil.parse(val.toString()).getTime());
                        }
                    }
                    else if (val instanceof Long)
                    {
                        tt = new Timestamp((Long) val);
                    }
                }
                else
                {
                    tt = (Timestamp) val;
                }
                valueVal.put(key, tt);
            }
        }
    }

    public static Row getInsertRow(String tableName, Map<String, Object> fieldVals, TableMetaData tmd)
    {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        if (tmd == null)
        {
            tmd = DbContext.getTableMetaData(tableName, null);
        }
        if (tmd == null)
        {
            throw new DbException("表[" + tableName + "]不存在或没有加载！");
        }
        row.setDbType(tmd.getDbType());
        row.setTableName(tmd.getTableName());
        Column idColumn = tmd.getIdColumn();
        for (Entry<String, Object> entry : fieldVals.entrySet())
        {
            Object val = entry.getValue();
            String key = entry.getKey();
            if (val == null)
            {
                continue;
            }
            Column column = tmd.getColumn(key);
            if (column != null)
            {
                if (column.equals(idColumn))
                {
                    if (StringUtils.isNotBlank(val.toString()))
                    {
                        row.setIdValue(val.toString());
                        row.setIdName(idColumn.getColumnName());
                    }
                    // else
                    // {
                    // String id = SnowflakeIdGeneratorUtil.nextStringId();
                    // row.setIdValue(id);
                    // }
                    // row.setIdName(key);
                    // rowValueMap.put(column.getColumnName(),
                    // row.getIdValue());
                }
                else
                {
                    if (column.isNumberColumn() && StringUtil.isBlank(val.toString()))
                    {
                        continue;
                    }
                    rowValueMap.put(column.getColumnName(), val);
                }
            }
        }
        // if (row.getIdName() == null)
        // {
        // String id = SnowflakeIdGeneratorUtil.nextStringId();
        // row.setIdName(idColumn.getName());
        // row.setIdValue(id);
        // rowValueMap.put(idColumn.getColumnName(), id);
        // }

        if (rowValueMap.size() > 0)
        {
            row.setRowValueMap(rowValueMap);
        }
        else
        {
            row.setIsEffective(false);
        }
        return defaultValue(row, tmd, DataOpeType.INSERT);
    }

    public static Row getUpdateRow(String tableName, Map<String, Object> fieldVals, TableMetaData tmd)
    {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        if (tmd == null)
        {
            tmd = DbContext.getTableMetaData(tableName, null);
        }

        if (tmd == null)
        {
            throw new DbException("表[" + tableName + "]不存在或没有加载！");
        }
        row.setDbType(tmd.getDbType());
        row.setTableName(tmd.getTableName());
        Column idColumn = tmd.getIdColumn();
        if (idColumn == null)
        {
            throw new DbException("表[" + tableName + "]没有主键字段！");
        }
        for (String key : fieldVals.keySet())
        {
            Object val = fieldVals.get(key);
            if (val == null)
            {
                continue;
            }
            Column column = tmd.getColumn(key);
            if (column != null)
            {
                if (column.equals(idColumn))
                {

                    row.setIdValue(val.toString());
                    row.setIdName(column.getColumnName());
                }
                else
                {
                    if (column.isNumberColumn() && StringUtil.isBlank(val.toString()))
                    {
                        continue;
                    }
                    rowValueMap.put(column.getColumnName(), val);
                }
            }
        }

        if (rowValueMap.size() > 0 && StringUtils.isNotBlank(row.getIdValue()))
        {
            row.setRowValueMap(rowValueMap);
        }
        else
        {
            row.setIsEffective(false);
        }
        return defaultValue(row, tmd, DataOpeType.UPDATE);
    }

    public static Row getDeleteRow(String tableName, Map<String, Object> fieldVals, TableMetaData tmd)
    {
        Row row = new Row();
        if (tmd == null)
        {
            tmd = DbContext.getTableMetaData(tableName, null);
        }
        if (tmd == null)
        {
            throw new DbException("表[" + tableName + "]不存在或没有加载！");
        }
        row.setDbType(tmd.getDbType());
        Map<String, Object> rowValueMap = new HashMap<String, Object>(2);
        row.setTableName(tmd.getTableName());
        Column idColumn = tmd.getIdColumn();
        if (idColumn == null)
        {
            throw new DbException("表[" + tableName + "]没有主键！");
        }
        for (String key : fieldVals.keySet())
        {
            Column column = tmd.getColumn(key);
            if (column != null)
            {
                if (column.equals(idColumn))
                {
                    Object val = fieldVals.get(key);
                    rowValueMap.put(column.getColumnName(), val);

                    row.setIdValue(val.toString());
                    row.setIdName(column.getColumnName());
                    row.setRowValueMap(rowValueMap);
                    break;
                }

            }
        }

        if (row.getIdName() == null)
        {
            row.setIsEffective(false);
        }
        return row;
    }

    /**
     * @Desc:获取安装条件删除多条数据的row
     * @Date 2017年5月12日上午10:05:05
     * @author liuwww
     * @param tableName
     * @param fieldVals
     * @return
     */
    public static Row getDeleteRows(String tableName, Map<String, Object> fieldVals, TableMetaData tmd)
    {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        if (tmd != null)
        {
            row.setTableName(tmd.getTableName());
            row.setDbType(tmd.getDbType());
            for (String key : fieldVals.keySet())
            {
                Column column = tmd.getColumn(key);
                if (column != null)
                {
                    Object val = fieldVals.get(key);
                    rowValueMap.put(column.getColumnName(), val);
                }
            }
        }
        else
        {
            row.setTableName(tableName);
        }

        if (rowValueMap.size() == 0)
        {
            row.setIsEffective(false);
        }
        else
        {
            row.setRowValueMap(rowValueMap);
        }
        return row;
    }

    public static <T> Row getInsertRow(TableEntity<T> entity, TableMetaData tmd)
    {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        if (tmd == null)
        {
            tmd = DbContext.getTableMetaData(entity.tableName(), null);
        }
        if (tmd == null)
        {
            throw new DbException("表[" + entity.tableName() + "不存在或没有加载！");
        }
        row.setDbType(tmd.getDbType());
        row.setTableName(tmd.getTableName());
        Column idColumn = tmd.getIdColumn();
        if (idColumn == null)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("表[{}]没有主键字段！", entity.tableName());
            }
            // throw new DbException("表[" + entity.tableName() + "]没有主键字段！");
        }
        for (Column column : tmd.getColumnList())
        {
            Field field = EntryUtil.getField(entity.getClass(), column.getName());
            if (field == null)
            {
                continue;
            }

            Object val = EntryUtil.getFieldValue(entity, column.getName());
            if (val == null)
            {
                continue;
            }
            if (column.equals(idColumn))
            {

                if (val != null && StringUtils.isNotBlank(val.toString()))
                {
                    row.setIdValue(val.toString());
                    row.setIdName(column.getName());
                    rowValueMap.put(column.getColumnName(), row.getIdValue());
                }
                else
                {
                    // String id = SnowflakeIdGeneratorUtil.nextStringId();
                    // row.setIdValue(id);
                }
            }
            else
            {
                if (column.isNumberColumn() && StringUtil.isBlank(val.toString()))
                {
                    continue;
                }
                rowValueMap.put(column.getColumnName(), val);
            }
        }
        // if (row.getIdName() == null)
        // {
        // String id = SnowflakeIdGeneratorUtil.nextStringId();
        // row.setIdName(idColumn.getName());
        // row.setIdValue(id);
        // rowValueMap.put(idColumn.getColumnName(), id);
        // }

        if (rowValueMap.size() > 0)
        {
            row.setRowValueMap(rowValueMap);
        }
        else
        {
            row.setIsEffective(false);
        }
        return defaultValue(row, tmd, DataOpeType.INSERT);
    }

    public static <T> Row getUpdateRow(TableEntity<T> entity, TableMetaData tmd)
    {
        if (tmd == null)
        {
            tmd = DbContext.getTableMetaData(entity.tableName(), null);
        }
        if (tmd == null)
        {
            throw new DbException("表[" + entity.tableName() + "]不存在或没有加载！");
        }
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();

        row.setTableName(tmd.getTableName());
        Column idColumn = tmd.getIdColumn();
        if (idColumn == null)
        {
            throw new DbException("表[" + entity.tableName() + "]没有主键！");
        }
        row.setIdName(idColumn.getColumnName());
        row.setDbType(tmd.getDbType());
        for (Column column : tmd.getColumnList())
        {
            Field field = EntryUtil.getField(entity.getClass(), column.getName());
            if (field == null)
            {
                continue;
            }

            Object val = EntryUtil.getFieldValue(entity, column.getName());
            if (val == null)
            {
                continue;
            }
            if (column.equals(idColumn))
            {
                if (val != null && StringUtils.isNotBlank(val.toString()))
                {
                    row.setIdValue(val.toString());
                }
            }
            else
            {
                if (column.isNumberColumn() && StringUtil.isBlank(val.toString()))
                {
                    continue;
                }
                rowValueMap.put(column.getColumnName(), val);
            }
        }

        if (StringUtils.isBlank(row.getIdValue()) || rowValueMap.size() == 0)
        {
            row.setIsEffective(false);
        }
        else
        {
            row.setRowValueMap(rowValueMap);
        }
        return defaultValue(row, tmd, DataOpeType.UPDATE);
    }

    public static <T> Row getDeleteRow(TableEntity<T> entity, TableMetaData tmd)
    {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        if (tmd != null)
        {
            row.setTableName(tmd.getTableName());
            row.setDbType(tmd.getDbType());
            Column idColumn = tmd.getIdColumn();
            if (idColumn != null)
            {
                Field field = EntryUtil.getField(entity.getClass(), idColumn.getName());
                if (field != null)
                {
                    Object val = EntryUtil.getFieldValue(entity, idColumn.getName());
                    rowValueMap.put(idColumn.getColumnName(), val);
                    row.setIdName(idColumn.getColumnName());
                    row.setIdValue(val.toString());
                }
            }
            else
            {
                for (Column c : tmd.getColumnList())
                {
                    String field = c.getName();
                    if (EntryUtil.hasField(entity, field))
                    {
                        Object val = EntryUtil.getFieldValue(entity, c.getName());
                        rowValueMap.put(c.getColumnName(), val);
                    }
                }
            }

        }
        else
        {

            row.setTableName(entity.tableName());
        }

        if (StringUtils.isBlank(row.getIdValue()))
        {
            row.setIsEffective(false);
        }
        else
        {
            row.setRowValueMap(rowValueMap);
        }
        return row;
    }

    public static void setRowValMap(Row row, Map<String, Object> rowValMap)
    {
        row.setRowValueMap(rowValMap);
    }
}
