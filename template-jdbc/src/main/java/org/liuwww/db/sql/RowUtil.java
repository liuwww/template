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

        boolean isUnionKey = tmd.isUnionKey();

        if (!isUnionKey && idColumn == null)
        {
            throw new DbException("表[" + tableName + "]没有主键！");
        }
        else if (isUnionKey)
        {
            row.setIdNames(tmd.getIdNames());
        }
        else
        {
            row.setIdName(idColumn.getColumnName());
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

                if (!isUnionKey)
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
                            val = null;
                            // continue;
                        }
                        rowValueMap.put(column.getColumnName(), val);
                    }
                }
                else if (isUnionKey)
                {
                    // 联合主键的情况
                    int index = getObjectIndex(tmd.getIdColumns(), column);
                    if (index >= 0)
                    {
                        String[] idValues = row.getIdValues();
                        if (idValues == null)
                        {
                            idValues = new String[tmd.getIdColumns().length];
                            row.setIdValues(idValues);
                        }
                        if (val != null && StringUtils.isNotBlank(val.toString()))
                        {
                            idValues[index] = val.toString();
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
        boolean isUnionKey = tmd.isUnionKey();
        if (!isUnionKey && idColumn == null)
        {
            throw new DbException("表[" + tableName + "]没有主键！");
        }
        else if (isUnionKey)
        {
            row.setIdNames(tmd.getIdNames());
        }
        else
        {
            row.setIdName(idColumn.getColumnName());
        }
        for (String key : fieldVals.keySet())
        {
            Column column = tmd.getColumn(key);
            if (column != null)
            {
                if (!isUnionKey)
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
                else if (isUnionKey)
                {
                    int index = getObjectIndex(tmd.getIdColumns(), column);
                    if (index >= 0)
                    {
                        String[] idValues = row.getIdValues();
                        if (idValues == null)
                        {
                            idValues = new String[tmd.getIdColumns().length];
                            row.setIdValues(idValues);
                        }
                        Object val = fieldVals.get(key);
                        idValues[index] = val.toString();
                    }
                }
            }
        }

        checkUpdateRowValid(row, isUnionKey);
        return row;
    }

    private static void checkUpdateRowValid(Row row, boolean isUnionKey)
    {
        if (row.getIdName() == null && !isUnionKey)
        {
            row.setIsEffective(false);
        }
        else if (isUnionKey)
        {
            String[] idValues = row.getIdValues();
            if (idValues == null || idValues.length == 0)
            {
                row.setIsEffective(false);
            }
            else
            {
                for (String idVal : idValues)
                {
                    if (StringUtil.isBlank(idVal))
                    {
                        row.setIsEffective(false);
                        break;
                    }
                }
            }
        }
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
        boolean isUnionKey = tmd.isUnionKey();

        if (!isUnionKey && idColumn == null)
        {
            throw new DbException("表[" + entity.tableName() + "]没有主键！");
        }
        else if (isUnionKey)
        {
            row.setIdNames(tmd.getIdNames());
        }
        else
        {
            row.setIdName(idColumn.getColumnName());
        }
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
            // 非联合主键情况的处理
            if (!isUnionKey)
            {
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
            else if (isUnionKey)
            {
                // 联合主键的情况
                int index = getObjectIndex(tmd.getIdColumns(), column);
                if (index >= 0)
                {
                    String[] idValues = row.getIdValues();
                    if (idValues == null)
                    {
                        idValues = new String[tmd.getIdColumns().length];
                        row.setIdValues(idValues);
                    }
                    if (val != null && StringUtils.isNotBlank(val.toString()))
                    {
                        idValues[index] = val.toString();
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
        }
        if (checkUpdateSingleRowValid(row, rowValueMap))
        {
            row.setRowValueMap(rowValueMap);
        }
        else
        {
            row.setIsEffective(false);
        }
        return defaultValue(row, tmd, DataOpeType.UPDATE);
    }

    /**
     * 检查更新单行的数据是否是有效数据（id齐全，并且存在非id的更新数据）
     */
    private static boolean checkUpdateSingleRowValid(Row row, Map<String, Object> rowValueMap)
    {
        if (rowValueMap == null || rowValueMap.size() == 0)
        {
            return false;
        }
        boolean idChecked = false;
        if (StringUtil.isNotBlank(row.getIdValue()))
        {
            idChecked = true;
        }
        if (!idChecked)
        {
            // 联合主键更新时，主键的每个值都不可为空
            String[] idValues = row.getIdValues();
            for (String id : idValues)
            {
                if (StringUtil.isBlank(id))
                {
                    return false;
                }
            }
        }

        return true;
    }

    public static <T> Row getDeleteRow(TableEntity<T> entity, TableMetaData tmd)
    {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        boolean isUnionKey = false;
        if (tmd != null)
        {
            row.setTableName(tmd.getTableName());
            row.setDbType(tmd.getDbType());
            isUnionKey = tmd.isUnionKey();
            if (isUnionKey)
            {
                Column[] idColumnList = tmd.getIdColumns();
                row.setIdNames(tmd.getIdNames());
                for (int i = idColumnList.length - 1; i >= 0; i--)
                {
                    Column idColumn = idColumnList[i];
                    Field field = EntryUtil.getField(entity.getClass(), idColumn.getName());
                    if (field != null)
                    {
                        Object val = EntryUtil.getFieldValue(entity, idColumn.getName());
                        rowValueMap.put(idColumn.getColumnName(), val);
                    }
                }

            }
            else
            {
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

        }
        else
        {

            row.setTableName(entity.tableName());
        }

        if (StringUtils.isBlank(row.getIdValue()) && !isUnionKey)
        {
            row.setIsEffective(false);
        }
        else if (isUnionKey)
        {
            if (rowValueMap.size() != tmd.getIdColumns().length)
            {
                row.setIsEffective(false);
            }
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

    public static int getObjectIndex(Object[] objs, Object obj)
    {
        if (objs == null)
        {
            return -1;
        }
        for (int i = 0; i < objs.length; i++)
        {
            if (objs[i] == obj)
            {
                return i;
            }
        }
        return -1;
    }

}
